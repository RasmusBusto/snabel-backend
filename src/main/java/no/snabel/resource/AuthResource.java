package no.snabel.resource;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.snabel.dto.LoginRequest;
import no.snabel.dto.LoginResponse;
import no.snabel.service.AuthService;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @PermitAll
    public Uni<Response> login(LoginRequest request) {
        return authService.login(request)
                .map(loginResponse -> Response.ok(loginResponse).build())
                .onFailure(SecurityException.class)
                .recoverWithItem(e -> Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse(e.getMessage()))
                        .build())
                .onFailure()
                .recoverWithItem(e -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ErrorResponse("An error occurred during login"))
                        .build());
    }

    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
