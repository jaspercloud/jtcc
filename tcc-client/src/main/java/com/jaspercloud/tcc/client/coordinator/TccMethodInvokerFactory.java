package com.jaspercloud.tcc.client.coordinator;

import com.jaspercloud.tcc.client.invoker.TccMethodInvoker;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TccMethodInvokerFactory {

    private Map<String, TccMethodInvoker> tccMethodInvokerMap = new ConcurrentHashMap<>();

    public void addTccMethodInvoker(TccMethodInvoker tccMethodInvoker) {
        tccMethodInvokerMap.put(tccMethodInvoker.getUniqueName(), tccMethodInvoker);
    }

    public TccMethodInvoker getTccMethodInvoker(String uniqueName) {
        TccMethodInvoker tccMethodInvoker = tccMethodInvokerMap.get(uniqueName);
        return tccMethodInvoker;
    }
}
