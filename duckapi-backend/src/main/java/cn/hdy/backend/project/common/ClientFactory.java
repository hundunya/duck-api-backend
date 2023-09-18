package cn.hdy.backend.project.common;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author 混沌鸭
 **/
public class ClientFactory {
    //连接地址
    private static final String CONNECTION_STRING = "127.0.0.1:2181";
    //等待事件的基础单位，单位毫秒
    private static final int BASE_SLEEP_TIME = 1000;
    //最大重试次数
    private static final int MAX_RETRIES = 3;
    private static volatile CuratorFramework zkClient;

    public static CuratorFramework getClient() { //单例
        if (zkClient == null) {
            synchronized (ClientFactory.class) {
                if (zkClient == null) {
                    createSimple();
                }
            }
        }
        return zkClient;
    }

    public static void createSimple() {
        //重试策略: 第一次重试等待1秒，第二次重试等待2秒，第三次重试等待4秒
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.newClient(CONNECTION_STRING, retryPolicy);
        zkClient.start();
    }

    public static void createWithOptions(int connectionTimeoutMs, int sessionTimeoutMs) {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(CONNECTION_STRING)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs) //连接超时
                .sessionTimeoutMs(sessionTimeoutMs) //会话超时
                .build();
        zkClient.start();
    }
}
