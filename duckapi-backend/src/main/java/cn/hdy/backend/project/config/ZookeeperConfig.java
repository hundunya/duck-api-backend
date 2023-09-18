package cn.hdy.backend.project.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 混沌鸭
 **/
@Configuration
public class ZookeeperConfig {
    // 重试休眠时间
    private static final int SLEEP_TIME_MS = 1000;
    // 最大重试10次
    private static final int MAX_RETRIES = 10;
    //会话超时时间
    private static final int SESSION_TIMEOUT = 30 * 1000;
    //连接超时时间
    private static final int CONNECTION_TIMEOUT = 3 * 1000;

    @Bean
    public CuratorFramework curatorFramework() throws InterruptedException {
        CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                .connectString("localhost")
                .connectionTimeoutMs(CONNECTION_TIMEOUT)
                .sessionTimeoutMs(SESSION_TIMEOUT)
                .retryPolicy(new ExponentialBackoffRetry(SLEEP_TIME_MS, MAX_RETRIES))
                .build();
        zkClient.start();
        zkClient.blockUntilConnected();
        return zkClient;
    }
}
