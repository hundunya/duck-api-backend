package cn.hdy.gateway.project.config;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author 混沌鸭
 **/
@Component
public class CustomApiResponseRewrite implements RewriteFunction<String, String > {

    @Override
    public Publisher<String> apply(ServerWebExchange serverWebExchange, String body) {
        return Mono.just(body);
    }
}
