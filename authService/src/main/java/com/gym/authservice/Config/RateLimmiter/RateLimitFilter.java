package com.gym.authservice.Config.RateLimmiter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

//@Component
//@RequiredArgsConstructor
//public class RateLimitFilter implements GlobalFilter, Ordered {
//    private final TokenBucketRateLimiter rateLimiter;
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        String key = resolveKey(exchange);
//        return rateLimiter.allowRequest(key)
//                .flatMap(allowed-> {
//                    if(!allowed){
//                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
//                        return exchange.getResponse().setComplete();
//                    }
//                    return chain.filter(exchange);
//                });
//    }
//
//    private String resolveKey(ServerWebExchange exchange) {
//        return Objects.requireNonNull(exchange.getRequest()
//                        .getRemoteAddress())
//                .getAddress()
//                .getHostAddress();
//    }
//
//    @Override
//    public int getOrder() {
//        return -100;
//    }
//}
