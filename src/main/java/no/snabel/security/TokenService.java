package no.snabel.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    @ConfigProperty(name = "snabel.jwt.duration.web", defaultValue = "86400")
    Long webTokenDuration;

    @ConfigProperty(name = "snabel.jwt.duration.app", defaultValue = "2592000")
    Long appTokenDuration;

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "https://snabel.no")
    String issuer;

    public String generateToken(Long userId, String username, Long customerId, String role, String deviceType) {
        long duration = "app".equalsIgnoreCase(deviceType) ? appTokenDuration : webTokenDuration;

        return Jwt.issuer(issuer)
                .upn(username)
                .claim("userId", userId)
                .claim("customerId", customerId)
                .claim("role", role)
                .claim("deviceType", deviceType)
                .groups(Set.of(role))
                .expiresIn(Duration.ofSeconds(duration))
                .sign();
    }

    public Long getTokenDuration(String deviceType) {
        return "app".equalsIgnoreCase(deviceType) ? appTokenDuration : webTokenDuration;
    }
}
