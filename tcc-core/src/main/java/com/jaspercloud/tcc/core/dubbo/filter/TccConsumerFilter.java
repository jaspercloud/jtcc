package com.jaspercloud.tcc.core.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.jaspercloud.tcc.core.util.TccConstants;
import com.jaspercloud.tcc.core.util.TccContext;

@Activate(group = {Constants.CONSUMER})
public class TccConsumerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        TccContext tccContext = TccContext.get();
        if (null != tccContext) {
            RpcContext context = RpcContext.getContext();
            context.setAttachment(TccConstants.TID_KEY, tccContext.getTid());
        }
        Result result = invoker.invoke(invocation);
        return result;
    }
}
