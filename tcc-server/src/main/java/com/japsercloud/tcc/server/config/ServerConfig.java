package com.japsercloud.tcc.server.config;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.japsercloud.tcc.server.coordinator.RedisServerTccTransactionCoordinator;
import com.japsercloud.tcc.server.support.RedisLockSupport;
import com.jaspercloud.tcc.core.coordinator.TccTransactionCoordinator;
import com.jaspercloud.tcc.core.coordinator.TccTransactionProcessor;
import com.jaspercloud.tcc.core.dubbo.DubboBeanCenter;
import com.jaspercloud.tcc.core.dubbo.DubboConfigCustomizer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@EnableConfigurationProperties(TccServerConfigProperties.class)
@Configuration
public class ServerConfig {

    @Autowired
    private TccServerConfigProperties config;

    @Bean
    public CuratorFramework curatorFramework() {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(config.getZookeeperAddress())
                .retryPolicy(new RetryNTimes(1, 1000))
                .connectionTimeoutMs(5000);
        CuratorFramework client = builder.build();
        client.start();
        return client;
    }

    @Bean
    public LeaderLatch leaderLatch(CuratorFramework curatorFramework) throws Exception {
        LeaderLatch leaderLatch = new LeaderLatch(curatorFramework, config.getZookeeperPath(), UUID.randomUUID().toString(), LeaderLatch.CloseMode.NOTIFY_LEADER);
        leaderLatch.start();
        return leaderLatch;
    }

    @Bean
    public Executor coordinatorExecutor() {
        ThreadGroup group = new ThreadGroup("coordinatorThreadGroup");
        AtomicInteger integer = new AtomicInteger();
        ExecutorService threadPool = Executors.newFixedThreadPool(config.getCoordinatorThreads(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(group, r);
                thread.setName("coordinatorThread-" + integer.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });
        return threadPool;
    }

    @Bean
    public RedisLockSupport redisLockSupport(StringRedisTemplate redisTemplate) {
        RedisLockSupport redisLockSupport = new RedisLockSupport(redisTemplate);
        return redisLockSupport;
    }

    @Configuration
    public static class DubboBeanCenterConfig {

        @Bean
        public DubboBeanCenter dubboBeanCenter() {
            DubboBeanCenter dubboBeanCenter = new DubboBeanCenter();
            dubboBeanCenter.addServiceConfig(TccTransactionCoordinator.class, RedisServerTccTransactionCoordinator.class);
            dubboBeanCenter.addReferenceConfig(TccTransactionCoordinator.class);
            dubboBeanCenter.addReferenceConfig(TccTransactionProcessor.class).setCustomizer(new DubboConfigCustomizer<ReferenceBean>() {
                @Override
                public void customize(ReferenceBean config) {
                    config.setCluster("tcc");
                }
            });
            return dubboBeanCenter;
        }
    }
}
