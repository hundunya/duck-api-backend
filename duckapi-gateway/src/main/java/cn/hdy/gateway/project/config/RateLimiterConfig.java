package cn.hdy.gateway.project.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author 混沌鸭
 **/
@Configuration
public class RateLimiterConfig {

    @Bean
    KeyResolver ipKeyResolver() {
        return exchange -> {
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            String hostAddress = "空地址";
            if (remoteAddress != null) {
                InetAddress address = remoteAddress.getAddress();
                hostAddress = address.getHostAddress();
            }
            return Mono.just(hostAddress);
        };
    }
}
