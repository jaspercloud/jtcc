package com.japsercloud.tcc.server.compensate;

import com.japsercloud.tcc.server.config.TccServerConfigProperties;
import com.japsercloud.tcc.server.repository.TccTransactionInfo;
import com.japsercloud.tcc.server.repository.TccTransactionRepository;
import com.japsercloud.tcc.server.support.Lock;
import com.japsercloud.tcc.server.support.RedisLockSupport;
import com.jaspercloud.tcc.core.coordinator.TccTransactionCoordinator;
import com.jaspercloud.tcc.core.util.TccConstants;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TccCompensateService implements InitializingBean, DisposableBean, Runnable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LeaderLatch leaderLatch;

    @Autowired
    private TccTransactionCoordinator tccTransactionCoordinator;

    @Autowired
    private TccTransactionRepository tccTransactionRepository;

    @Autowired
    private RedisLockSupport redisLockSupport;

    @Autowired
    private TccServerConfigProperties config;

    private Thread thread;

    @Override
    public void afterPropertiesSet() throws Exception {
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void destroy() {
        thread.interrupt();
    }

    @Override
    public void run() {
        for (; ; ) {
            try {
                boolean isLeader = leaderLatch.hasLeadership();
                if (isLeader) {
                    Long cursor = 0L;
                    List<TccTransactionInfo> list;
                    do {
                        list = tccTransactionRepository.getTccTransactionList(cursor, config.getScanLimit());
                        if (!list.isEmpty()) {
                            TccTransactionInfo info = list.get(list.size() - 1);
                            cursor = info.getId();
                        }
                        retryCompensateList(list);
                    } while (!list.isEmpty());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                Thread.sleep(config.getCompensateTimeout());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void retryCompensateList(List<TccTransactionInfo> list) {
        for (TccTransactionInfo info : list) {
            try {
                retryCompensate(info);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void retryCompensate(TccTransactionInfo info) throws Exception {
        String tid = info.getTid();
        Lock lock = redisLockSupport.newLock(tid, config.getLockTime());
        if (lock.isLock()) {
            return;
        }
        if (TccConstants.TccStatus.Confirm.equals(info.getTccStatus())) {
            //confirm
            if (System.currentTimeMillis() > info.getCompensateTimeoutMillis()) {
                logger.info("compensate confirm: " + tid);
                tccTransactionCoordinator.confirm(tid);
            }
        } else if (TccConstants.TccStatus.Cancel.equals(info.getTccStatus())) {
            //cancel
            if (System.currentTimeMillis() > info.getCompensateTimeoutMillis()) {
                logger.info("compensate cancel: " + tid);
                tccTransactionCoordinator.cancel(tid);
            }
        } else if (System.currentTimeMillis() > info.getTccTimeoutMillis()) {
            //timeout
            logger.info("compensate timeout: " + tid);
            tccTransactionCoordinator.cancel(tid);
        }
    }
}
