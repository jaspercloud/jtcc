package com.japsercloud.tcc.server.dubbo.cluster;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.integration.RegistryDirectory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import com.alibaba.dubbo.rpc.cluster.support.FailoverClusterInvoker;
import com.jaspercloud.tcc.core.coordinator.TccMethodData;
import com.jaspercloud.tcc.core.util.ClassCache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TccCluster implements Cluster {

    private static Field providerUrlField;

    static {
        try {
            Class<?> clazz = ClassCache.getClass(RegistryDirectory.class.getName() + "$InvokerDelegate");
            Field field = clazz.getDeclaredField("providerUrl");
            field.setAccessible(true);
            providerUrlField = field;
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
        return new AbstractClusterInvoker<T>(directory) {
            @Override
            protected Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException {
                List<Invoker<T>> appInvokers = selectAppInvokes(invocation, invokers);
                FailoverClusterInvoker invoker = new FailoverClusterInvoker(directory) {
                    @Override
                    public Result doInvoke(Invocation invocation, List list, LoadBalance loadbalance) throws RpcException {
                        return super.doInvoke(invocation, appInvokers, loadbalance);
                    }
                };
                Result result = invoker.invoke(invocation);
                return result;
            }
        };
    }

    private <T> List<Invoker<T>> selectAppInvokes(Invocation invocation, List<Invoker<T>> invokers) throws RpcException {
        List<Invoker<T>> list = new ArrayList<>();
        try {
            Object[] arguments = invocation.getArguments();
            TccMethodData tccMethodData = (TccMethodData) arguments[0];
            for (Invoker<T> invoker : invokers) {
                URL providerUrl = (URL) providerUrlField.get(invoker);
                String app = providerUrl.getParameter(Constants.APPLICATION_KEY);
                if (tccMethodData.getApplicationName().equals(app)) {
                    list.add(invoker);
                }
            }
            return list;
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }
}
