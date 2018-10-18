package com.jaspercloud.tcc.core.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.jaspercloud.tcc.core.util.TccConstants;
import com.jaspercloud.tcc.core.util.TccContext;

@Activate(group = {Constants.PROVIDER})
public class TccProviderFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try {
            RpcContext context = RpcContext.getContext();
            String tid = context.getAttachment(TccConstants.TID_KEY);
            if (null != tid) {
                TccContext.create(tid, false);
            }
            Result result = invoker.invoke(invocation);
            return result;
        } finally {
            TccContext.clean();
        }
    }
}
