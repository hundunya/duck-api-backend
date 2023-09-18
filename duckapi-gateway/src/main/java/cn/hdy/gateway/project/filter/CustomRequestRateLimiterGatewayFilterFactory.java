package cn.hdy.gateway.project.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author 混沌鸭
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
@Component
@SuppressWarnings(value = {"rawtypes", "unchecked"})
public class CustomRequestRateLimiterGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomRequestRateLimiterGatewayFilterFactory.Config> {

    private final RateLimiter defaultRateLimiter;
    private final KeyResolver defaultKeyResolver;
    private boolean denyEmptyKey = true;
    private String emptyKeyStatusCode;

    public CustomRequestRateLimiterGatewayFilterFactory(RateLimiter defaultRateLimiter, KeyResolver defaultKeyResolver) {
        super(Config.class);
        this.emptyKeyStatusCode = HttpStatus.FORBIDDEN.name();
        this.defaultRateLimiter = defaultRateLimiter;
        this.defaultKeyResolver = defaultKeyResolver;
    }

    @Override
    public GatewayFilter apply(Config config) {
        KeyResolver resolver = getOrDefault(config.getKeyResolver(), defaultKeyResolver);
        RateLimiter<Object> limiter = getOrDefault(config.getRateLimiter(), defaultRateLimiter);
        return (exchange, chain) -> resolver.resolve(exchange).flatMap(key -> {
            String routeId = config.getRouteId();
            if (routeId == null) {
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                if (route != null) {
                    routeId = route.getId();
                }
            }

            String finalRouteId = routeId;
            return limiter.isAllowed(routeId, key).flatMap(response -> {

                for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                    exchange.getResponse().getHeaders().add(header.getKey(), header.getValue());
                }

                if (response.isAllowed()) {
                    return chain.filter(exchange);
                }

                log.warn("已限流: {}", finalRouteId);
                ServerHttpResponse httpResponse = exchange.getResponse();
                httpResponse.setStatusCode(config.getStatusCode());
                if (!httpResponse.getHeaders().containsKey("Content-Type")) {
                    httpResponse.getHeaders().add("Content-Type", "application/json");
                }
                DataBuffer buffer = httpResponse.bufferFactory().wrap("当前访问人数过多，请稍候重试".getBytes(StandardCharsets.UTF_8));
                return httpResponse.writeWith(Mono.just(buffer));
            });
        });
    }

    private <T> T getOrDefault(T configValue, T defaultValue) {
        return configValue != null ? configValue : defaultValue;
    }

    @Data
    public static class Config implements HasRouteId {
        private KeyResolver keyResolver;
        private RateLimiter rateLimiter;
        private HttpStatus statusCode;
        private String routeId;

        public Config() {
            this.statusCode = HttpStatus.TOO_MANY_REQUESTS;
        }

        @Override
        public void setRouteId(String routeId) {
            this.routeId = routeId;
        }

        @Override
        public String getRouteId() {
            return this.routeId;
        }
    }
}
