package no.snabel.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.snabel.dto.LoginRequest;
import no.snabel.dto.LoginResponse;
import no.snabel.model.User;
import no.snabel.security.TokenService;

import java.time.LocalDateTime;

@ApplicationScoped
public class AuthService {

    @Inject
    TokenService tokenService;

    public Uni<LoginResponse> login(LoginRequest request) {
        return User.findByUsername(request.username)
                .onItem().ifNull().failWith(() -> new SecurityException("Invalid username or password"))
                .onItem().transformToUni(user -> {
                    if (!user.active) {
                        return Uni.createFrom().failure(new SecurityException("User account is not active"));
                    }

                    if (!BcryptUtil.matches(request.password, user.passwordHash)) {
                        return Uni.createFrom().failure(new SecurityException("Invalid username or password"));
                    }

                    String deviceType = request.deviceType != null ? request.deviceType : "web";
                    String token = tokenService.generateToken(
                            user.id,
                            user.username,
                            user.customer.id,
                            user.role,
                            deviceType
                    );

                    Long expiresIn = tokenService.getTokenDuration(deviceType);

                    // Update last login
                    user.lastLogin = LocalDateTime.now();
                    return user.persistAndFlush()
                            .map(u -> new LoginResponse(
                                    token,
                                    user.id,
                                    user.username,
                                    user.customer.id,
                                    user.role,
                                    expiresIn
                            ));
                });
    }

    public Uni<User> registerUser(User user, String plainPassword) {
        user.passwordHash = BcryptUtil.bcryptHash(plainPassword);
        user.createdAt = LocalDateTime.now();
        user.updatedAt = LocalDateTime.now();
        return user.persistAndFlush();
    }
}
