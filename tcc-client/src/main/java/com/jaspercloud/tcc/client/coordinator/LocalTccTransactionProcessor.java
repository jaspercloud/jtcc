package com.jaspercloud.tcc.client.coordinator;

import com.jaspercloud.tcc.client.invoker.TccMethodInvoker;
import com.jaspercloud.tcc.core.coordinator.TccMethodData;
import com.jaspercloud.tcc.core.coordinator.TccTransactionProcessor;
import com.jaspercloud.tcc.core.util.TccConstants;
import com.jaspercloud.tcc.core.util.TccContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocalTccTransactionProcessor implements TccTransactionProcessor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TccMethodInvokerFactory tccMethodInvokerFactory;

    private void invoke(TccMethodData data, TccConstants.TccStatus tccStatus) throws Exception {
        try {
            try {
                String uniqueName = data.getUniqueName();
                String tid = data.getTid();
                Object[] args = data.getArgs();
                TccMethodInvoker invoker = tccMethodInvokerFactory.getTccMethodInvoker(uniqueName);
                TccContext tccContext = TccContext.create(tid, false);
                tccContext.setUniqueName(uniqueName);
                tccContext.setTccStatus(tccStatus);
                invoker.invoke(tccStatus, args);
            } finally {
                TccContext.clean();
            }
        } catch (Exception e) {
            logger.error(String.format("exception: tid=%s, uniqueName=%s, invoke=%s, msg=%s",
                    data.getTid(), data.getUniqueName(), tccStatus.toString(), e.getMessage()));
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void confirm(TccMethodData data) throws Exception {
        logger.info(String.format("confirm: tid=%s, uniqueName=%s", data.getTid(), data.getUniqueName()));
        invoke(data, TccConstants.TccStatus.Confirm);
    }

    @Override
    public void cancel(TccMethodData data) throws Exception {
        logger.info(String.format("cancel: tid=%s, uniqueName=%s", data.getTid(), data.getUniqueName()));
        invoke(data, TccConstants.TccStatus.Cancel);
    }
}
