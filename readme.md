# Spring Cloud Gateway application with Prometheus support

This is a template application for Spring Cloud gateway application instrumented to
expose Prometheus metrics.

## Default routing

This gateway application demonstrates generic router based on Kubernetes environment variables service discovery mechanism.
For example...

    http://mygateway.com/user/findbyname?name=john

...is routed to...

    http://USER_SERVICE_HOST:USER_SERVICE_PORT/findbyname?name=john
    
In particular if you would like to invoke `/hello/world` REST endpoint of application `myapp` managed by Jenkins X, call the following
gateway URL - `http://examle.com/myapp/hello/world`.

## Endpoints used by Kubernetes

This quickstart exposes the following endpoints important for Kubernetes deployments:
- `/actuator/health` - Spring Boot endpoint indicating health. Used by Kubernetes as readiness probe.
- `/actuator/metrics` - Prometheus metrics. Invoked periodically and collected by Prometheus Kubernetes scraper.