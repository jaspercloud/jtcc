package com.japsercloud.tcc.server.coordinator;

import com.japsercloud.tcc.server.config.TccServerConfigProperties;
import com.japsercloud.tcc.server.repository.TccTransactionRepository;
import com.japsercloud.tcc.server.support.Lock;
import com.japsercloud.tcc.server.support.RedisLockSupport;
import com.jaspercloud.tcc.core.coordinator.TccMethodData;
import com.jaspercloud.tcc.core.coordinator.TccTransactionCoordinator;
import com.jaspercloud.tcc.core.coordinator.TccTransactionProcessor;
import com.jaspercloud.tcc.core.exception.TccException;
import com.jaspercloud.tcc.core.util.TccConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Executor;

@Service
public class RedisServerTccTransactionCoordinator implements TccTransactionCoordinator, InitializingBean {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TccTransactionProcessor tccTransactionProcessor;

    @Autowired
    private TccTransactionRepository tccTransactionRepository;

    @Autowired
    private RedisLockSupport redisLockSupport;

    @Autowired
    private TccServerConfigProperties config;

    @Qualifier("coordinatorExecutor")
    @Autowired
    private Executor executor;

    @Override
    public void afterPropertiesSet() {
    }

    @Override
    public String createTid(long timeout) throws Exception {
        String tid = tccTransactionRepository.createTid(timeout);
        logger.info("createTid: " + tid);
        return tid;
    }

    @Override
    public void joinTccMethod(TccMethodData data) throws Exception {
        String tid = data.getTid();
        String uniqueName = data.getUniqueName();
        Lock lock = redisLockSupport.newLock(tid, config.getLockTime());
        try {
            lock.lock();
            if (!tccTransactionRepository.hasTid(tid)) {
                throw new TccException(String.format("miss joinTccMethod %s tid: %s", uniqueName, tid));
            }
            logger.info(String.format("joinTccMethod %s tid: %s", uniqueName, tid));
            tccTransactionRepository.saveTccMethod(data);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void confirm(String tid) throws Exception {
        Lock lock = redisLockSupport.newLock(tid, config.getLockTime());
        try {
            lock.lock();
            if (tccTransactionRepository.hasTid(tid)) {
                logger.info("update confirmStatus tid: " + tid);
                tccTransactionRepository.updateTccTransactionStatus(tid, TccConstants.TccStatus.Confirm);
            } else {
                logger.info("miss confirm tid: " + tid);
            }
        } finally {
            lock.unlock();
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lock();
                    Map<Long, TccMethodData> dataMap = tccTransactionRepository.getTccMethodList(tid);
                    for (Map.Entry<Long, TccMethodData> entry : dataMap.entrySet()) {
                        TccMethodData data = entry.getValue();
                        logger.info(String.format("confirm: tid=%s, uniqueName=%s", data.getTid(), data.getUniqueName()));
                        tccTransactionProcessor.confirm(entry.getValue());
                        tccTransactionRepository.deleteTccMethod(tid, entry.getKey());
                    }
                    logger.info(String.format("deleteTccTransaction: tid=%s", tid));
                    tccTransactionRepository.deleteTccTransaction(tid);
                } catch (Exception e) {
                    String msg = String.format("confirm tid=%s, msg=%s", tid, e.getMessage());
                    logger.error(msg, e);
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    @Override
    public void cancel(String tid) throws Exception {
        Lock lock = redisLockSupport.newLock(tid, config.getLockTime());
        try {
            lock.lock();
            if (tccTransactionRepository.hasTid(tid)) {
                logger.info("update cancelStatus tid: " + tid);
                tccTransactionRepository.updateTccTransactionStatus(tid, TccConstants.TccStatus.Cancel);
            } else {
                logger.info("miss cancel tid: " + tid);
            }
        } finally {
            lock.unlock();
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lock();
                    Map<Long, TccMethodData> dataMap = tccTransactionRepository.getTccMethodList(tid);
                    for (Map.Entry<Long, TccMethodData> entry : dataMap.entrySet()) {
                        TccMethodData data = entry.getValue();
                        logger.info(String.format("cancel: tid=%s, uniqueName=%s", data.getTid(), data.getUniqueName()));
                        tccTransactionProcessor.cancel(entry.getValue());
                        tccTransactionRepository.deleteTccMethod(tid, entry.getKey());
                    }
                    logger.info(String.format("deleteTccTransaction: tid=%s", tid));
                    tccTransactionRepository.deleteTccTransaction(tid);
                } catch (Exception e) {
                    String msg = String.format("cancel tid=%s, msg=%s", tid, e.getMessage());
                    logger.error(msg, e);
                } finally {
                    lock.unlock();
                }
            }
        });
    }
}
