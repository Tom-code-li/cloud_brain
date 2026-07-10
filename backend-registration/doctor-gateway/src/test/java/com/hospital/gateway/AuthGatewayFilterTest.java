package com.hospital.gateway;

import com.hospital.common.security.DoctorPrincipal;
import com.hospital.common.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AuthGatewayFilterTest {
    private static final String SECRET = "01234567890123456789012345678901";
    private static final long TTL_SECONDS = 3600;

    private final AuthGatewayFilter filter = new AuthGatewayFilter(SECRET, TTL_SECONDS);
    private final JwtService jwtService = new JwtService(SECRET, TTL_SECONDS);

    @Test
    void missingTokenOnProtectedPathReturnsUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/outpatient/visits")
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(chain.capturedRequest()).isNull();
    }

    @Test
    void authLoginBypassesWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/auth/login")
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
        assertThat(chain.capturedRequest()).isSameAs(exchange.getRequest());
    }

    @Test
    void optionsPreflightBypassesWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.options("/outpatient/visits")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
        assertThat(chain.capturedRequest()).isSameAs(exchange.getRequest());
    }

    @Test
    void validTokenForwardsTrustedHeadersFromJwt() {
        DoctorPrincipal principal = new DoctorPrincipal(
                11L, 22L, 33L, "OUTPATIENT", "OUTPATIENT_DOCTOR", "Doctor Li"
        );
        String token = jwtService.createToken(principal);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/outpatient/visits")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        HttpHeaders headers = chain.capturedRequest().getHeaders();
        assertThat(headers.get("X-User-Id")).containsExactly("11");
        assertThat(headers.get("X-Doctor-Id")).containsExactly("22");
        assertThat(headers.get("X-Dept-Id")).containsExactly("33");
        assertThat(headers.get("X-Doctor-Type")).containsExactly("OUTPATIENT");
        assertThat(headers.get("X-Role-Code")).containsExactly("OUTPATIENT_DOCTOR");
        assertThat(headers.get("X-Real-Name")).containsExactly("Doctor Li");
    }

    @Test
    void spoofedInboundIdentityHeadersAreReplacedWithJwtValues() {
        DoctorPrincipal principal = new DoctorPrincipal(
                11L, 22L, 33L, "OUTPATIENT", "OUTPATIENT_DOCTOR", "Doctor Li"
        );
        String token = jwtService.createToken(principal);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/outpatient/visits")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header("X-User-Id", "spoofed-user", "another-user")
                        .header("X-Doctor-Id", "spoofed-doctor")
                        .header("X-Dept-Id", "spoofed-dept")
                        .header("X-Doctor-Type", "spoofed-type")
                        .header("X-Role-Code", "spoofed-role")
                        .header("X-Real-Name", "spoofed-name")
        );
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        HttpHeaders headers = chain.capturedRequest().getHeaders();
        assertThat(headers.get("X-User-Id")).containsExactly("11");
        assertThat(headers.get("X-Doctor-Id")).containsExactly("22");
        assertThat(headers.get("X-Dept-Id")).containsExactly("33");
        assertThat(headers.get("X-Doctor-Type")).containsExactly("OUTPATIENT");
        assertThat(headers.get("X-Role-Code")).containsExactly("OUTPATIENT_DOCTOR");
        assertThat(headers.get("X-Real-Name")).containsExactly("Doctor Li");
    }

    private static final class CapturingChain implements GatewayFilterChain {
        private final AtomicReference<ServerHttpRequest> capturedRequest = new AtomicReference<>();

        @Override
        public Mono<Void> filter(org.springframework.web.server.ServerWebExchange exchange) {
            capturedRequest.set(exchange.getRequest());
            return Mono.empty();
        }

        private ServerHttpRequest capturedRequest() {
            return capturedRequest.get();
        }
    }
}
