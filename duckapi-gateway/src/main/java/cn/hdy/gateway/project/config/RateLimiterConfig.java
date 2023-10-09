package cn.hdy.gateway.project.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * 限流配置
 *
 * @author 混沌鸭
 **/
@Configuration
public class RateLimiterConfig {

    @Bean(name = "pathKeyResolver")
    KeyResolver pathKeyResolver() {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            RequestPath path = request.getPath();
            String pathValue = path.value();
            return Mono.just(pathValue);
        };
    }
}
