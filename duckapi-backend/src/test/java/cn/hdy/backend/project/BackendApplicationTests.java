package cn.hdy.backend.project;

import cn.hdy.backend.project.common.ErrorCode;
import cn.hdy.backend.project.exception.ThrowUtils;
import cn.hdy.backend.project.service.InterfaceInfoService;
import cn.hdy.common.project.model.entity.InterfaceInfo;
import cn.hdy.sdk.client.project.client.ApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class BackendApplicationTests {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    @Test
    void contextLoads() {
        Object value = redisTemplate.opsForValue().get("key");
        System.out.println("==============================");
        System.out.println(value);
        System.out.println("==============================");
        redisTemplate.opsForValue().set("key", "this is a key");
        value = redisTemplate.opsForValue().get("key");
        System.out.println("==============================");
        System.out.println(value);
        System.out.println("==============================");
    }

}
