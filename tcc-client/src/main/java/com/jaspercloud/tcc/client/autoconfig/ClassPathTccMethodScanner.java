package com.jaspercloud.tcc.client.autoconfig;

import com.jaspercloud.tcc.client.annotation.TccMethod;
import com.jaspercloud.tcc.client.invoker.TccMethodInvoker;
import com.jaspercloud.tcc.client.invoker.TccMethodInvokerFactoryBean;
import com.jaspercloud.tcc.client.util.TccMethodCache;
import com.jaspercloud.tcc.core.exception.ReflectException;
import com.jaspercloud.tcc.core.exception.TccException;
import com.jaspercloud.tcc.core.util.ClassCache;
import com.jaspercloud.tcc.core.util.TccConstants;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.*;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.SpringTransactionAnnotationParser;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ClassPathTccMethodScanner extends ClassPathBeanDefinitionScanner {

    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    private TransactionAttributeSource transactionAttributeSource = new AnnotationTransactionAttributeSource(new SpringTransactionAnnotationParser());

    public ClassPathTccMethodScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    public void registerFilters() {
        addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                try {
                    String className = metadataReader.getClassMetadata().getClassName();
                    Class<?> clazz = ClassCache.getClass(className);
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        TccMethod tccMethod = method.getAnnotation(TccMethod.class);
                        if (null != tccMethod) {
                            return true;
                        }
                    }
                    return false;
                } catch (Exception e) {
                    throw new ReflectException(e.getMessage(), e);
                }
            }
        });
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> definitionHolders = new HashSet<>();
        for (String basePackage : basePackages) {
            Set<BeanDefinition> definitionSet = findCandidateComponents(basePackage);
            for (BeanDefinition tccBeanDefinition : definitionSet) {
                String beanClassName = tccBeanDefinition.getBeanClassName();
                try {
                    Class<?> clazz = ClassCache.getClass(beanClassName);
                    Set<BeanDefinitionHolder> holderSet = findTccMethodInvokers(clazz);
                    definitionHolders.addAll(holderSet);
                } catch (Exception e) {
                    throw new BeanCreationException(beanClassName, e);
                }
            }
        }
        return definitionHolders;
    }

    private Set<BeanDefinitionHolder> findTccMethodInvokers(Class<?> clazz) throws Exception {
        Set<BeanDefinitionHolder> definitionHolders = new HashSet<>();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            TccMethod tccMethod = method.getAnnotation(TccMethod.class);
            if (null == tccMethod) {
                continue;
            }
            BeanDefinitionHolder holder = registerTccMethodInvoker(method, tccMethod);
            definitionHolders.add(holder);
        }
        return definitionHolders;
    }

    private BeanDefinitionHolder registerTccMethodInvoker(Method method, TccMethod tccMethod) throws Exception {
        Class<?> declaringClass = method.getDeclaringClass();
        TransactionAttribute transactionAttribute = transactionAttributeSource.getTransactionAttribute(method, declaringClass);
        if (null == transactionAttribute) {
            throw new TccException("not found org.springframework.transaction.annotation.Transactional");
        }
        String transactionManagerName = transactionAttribute.getQualifier();

        Method tryMethod = method;
        Method confirmMethod = declaringClass.getMethod(tccMethod.confirmMethod(), method.getParameterTypes());
        Method cancelMethod = declaringClass.getMethod(tccMethod.cancelMethod(), method.getParameterTypes());
        TccMethodCache.addTccMethod(tryMethod, TccConstants.TccStatus.Try);
        TccMethodCache.addTccMethod(confirmMethod, TccConstants.TccStatus.Confirm);
        TccMethodCache.addTccMethod(cancelMethod, TccConstants.TccStatus.Cancel);

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TccMethodInvokerFactoryBean.class);
        builder.addConstructorArgValue(tccMethod.uniqueName());
        builder.addConstructorArgValue(declaringClass);
        builder.addPropertyValue("tryMethod", tryMethod);
        builder.addPropertyValue("confirmMethod", confirmMethod);
        builder.addPropertyValue("cancelMethod", cancelMethod);
        builder.addPropertyValue("transactionManagerName", transactionManagerName);
        AbstractBeanDefinition definition = builder.getBeanDefinition();
        ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(definition);
        definition.setScope(scopeMetadata.getScopeName());
        String beanName = tccMethod.uniqueName() + TccMethodInvoker.class.getSimpleName();
        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(definition, beanName);
        if (checkCandidate(definitionHolder.getBeanName(), definitionHolder.getBeanDefinition())) {
            registerBeanDefinition(definitionHolder, getRegistry());
            return definitionHolder;
        } else {
            throw new BeanCreationException("containsBeanDefinition");
        }
    }
}
