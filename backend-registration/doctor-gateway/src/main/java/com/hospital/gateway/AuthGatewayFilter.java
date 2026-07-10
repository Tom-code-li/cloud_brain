package com.hospital.gateway;

import com.hospital.common.security.DoctorPrincipal;
import com.hospital.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthGatewayFilter implements GlobalFilter, Ordered {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String X_USER_ID = "X-User-Id";
    private static final String X_DOCTOR_ID = "X-Doctor-Id";
    private static final String X_DEPT_ID = "X-Dept-Id";
    private static final String X_DOCTOR_TYPE = "X-Doctor-Type";
    private static final String X_ROLE_CODE = "X-Role-Code";
    private static final String X_REAL_NAME = "X-Real-Name";

    private final JwtService jwtService;

    public AuthGatewayFilter(
            @Value("${doctor.jwt.secret}") String secret,
            @Value("${doctor.jwt.ttl-seconds}") long ttlSeconds
    ) {
        this.jwtService = new JwtService(secret, ttlSeconds);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if ("/auth/login".equals(path) || HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange);
        }

        try {
            DoctorPrincipal principal = jwtService.parseToken(authorization.substring(BEARER_PREFIX.length()));
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .headers(headers -> {
                        removeTrustedIdentityHeaders(headers);
                        headers.set(X_USER_ID, String.valueOf(principal.userId()));
                        headers.set(X_DOCTOR_ID, String.valueOf(principal.doctorId()));
                        headers.set(X_DEPT_ID, String.valueOf(principal.deptId()));
                        headers.set(X_DOCTOR_TYPE, principal.doctorType());
                        headers.set(X_ROLE_CODE, principal.roleCode());
                        headers.set(X_REAL_NAME, principal.realName());
                    })
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (RuntimeException ex) {
            return unauthorized(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private void removeTrustedIdentityHeaders(HttpHeaders headers) {
        headers.remove(X_USER_ID);
        headers.remove(X_DOCTOR_ID);
        headers.remove(X_DEPT_ID);
        headers.remove(X_DOCTOR_TYPE);
        headers.remove(X_ROLE_CODE);
        headers.remove(X_REAL_NAME);
    }
}
