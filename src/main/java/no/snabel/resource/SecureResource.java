package no.snabel.resource;

import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

public abstract class SecureResource {

    @Inject
    JsonWebToken jwt;

    protected Long getCustomerId() {
        return jwt.getClaim("customerId");
    }

    protected Long getUserId() {
        return jwt.getClaim("userId");
    }

    protected String getRole() {
        return jwt.getClaim("role");
    }

    protected String getUsername() {
        return jwt.getName();
    }
}
