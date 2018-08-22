package com.github.jenkinsx.quickstarts.springcloud.gateway.prometheus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@SpringBootApplication
public class GatewayApplication {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes().
                route(
                        r -> r.path("/favicon.ico").negate().filters(
                                filters -> filters.filter(new GenericServiceDiscoveryRouter(), ROUTE_TO_URL_FILTER_ORDER + 1)
                        ).uri("no://op")
                ).
                build();
    }

    /**
     * Generic router based on Kubernetes environment variables service discovery mechanism:
     *
     * http://mygateway.com/user/findbyname?name=john
     *  is routed to
     * http://USER_SERVICE_HOST:USER_SERVICE_PORT/findbyname?name=john
     *
     */
    static private class GenericServiceDiscoveryRouter implements GatewayFilter {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            try {
                String targetService = exchange.getRequest().getPath().pathWithinApplication().elements().get(1).value();
                String targetServiceEnved = targetService.toUpperCase().replace('-', '_');
                String targetServiceHost = System.getenv(targetServiceEnved + "_SERVICE_HOST");
                String targetServicePort = System.getenv(targetServiceEnved + "_SERVICE_PORT");
                String targetPath = exchange.getRequest().getPath().pathWithinApplication().subPath(2).value();
                String targetUri = "http://" + targetServiceHost + ":" + targetServicePort + targetPath;
                exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, new URI(targetUri));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            return chain.filter(exchange);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}