package cz.cvut.api.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration @Slf4j
public class GlobalInterceptor implements GlobalFilter {
    private long startTime;
    private long stopTime;

    private final String gatewayRequestUrl = "org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRequestUrl";
    private final String gatewayClientResponse = "org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayClientResponse";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        startTime = System.nanoTime();
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    stopTime = System.nanoTime();
                    double duration = (double) (stopTime - startTime) / 1_000_000;
                    String httpMethod = exchange.getAttribute(gatewayClientResponse).toString().split("\\{")[0];
                    log.info(httpMethod + " request for " + exchange.getAttribute(gatewayRequestUrl) + " was completed in " + duration + " miliseconds.");
                }));
    }
}