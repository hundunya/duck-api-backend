package cn.hdy.gateway.project.config;

import cn.hdy.gateway.project.filter.ApiGatewayFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author 混沌鸭
 **/
@Configuration
public class RouteConfig {

    @Resource
    private CustomApiResponseRewrite customApiResponseRewrite;
    @Resource
    private CustomRedisRateLimiter customRedisRateLimiter;
    @Resource(name = "ipKeyResolver")
    private KeyResolver ipKeyResolver;
    @Resource
    private ApiGatewayFilter apiGatewayFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route("path_route", r -> r.path("/api/**")
                        .filters(f -> {
                            f.modifyResponseBody(String.class, String.class, customApiResponseRewrite);
                            f.filter(apiGatewayFilter, -2);
                            f.requestRateLimiter(config -> {
                                config.setKeyResolver(ipKeyResolver);
                                config.setRateLimiter(customRedisRateLimiter);
                            });
                            return f;
                        })
//                        .uri("https://duck-api-api.hundunya.cn"))
                        .uri("http://localhost:8100"))
                .build();
    }
}
