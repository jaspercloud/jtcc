package com.jaspercloud.tcc.client.coordinator;

import com.jaspercloud.tcc.client.invoker.TccMethodInvoker;
import com.jaspercloud.tcc.core.util.ObjectCache;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TccMethodInvokerFactory {

    @Autowired
    private BeanFactory beanFactory;

    private Map<String, TccMethodInvoker> tccMethodInvokerMap = new ConcurrentHashMap<>();

    public TccMethodInvoker getTccMethodInvoker(String uniqueName) throws Exception {
        String beanName = uniqueName + TccMethodInvoker.class.getSimpleName();
        TccMethodInvoker tccMethodInvoker = ObjectCache.get(tccMethodInvokerMap, beanName, new ObjectCache.Callback<TccMethodInvoker>() {
            @Override
            public TccMethodInvoker call() throws Exception {
                TccMethodInvoker tccMethodInvoker = beanFactory.getBean(beanName, TccMethodInvoker.class);
                return tccMethodInvoker;
            }
        });
        return tccMethodInvoker;
    }
}
