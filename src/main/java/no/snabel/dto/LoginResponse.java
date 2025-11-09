package no.snabel.dto;

public class LoginResponse {
    public String token;
    public Long userId;
    public String username;
    public Long customerId;
    public String role;
    public Long expiresIn; // seconds

    public LoginResponse(String token, Long userId, String username, Long customerId, String role, Long expiresIn) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.customerId = customerId;
        this.role = role;
        this.expiresIn = expiresIn;
    }
}
