package edu.cit.rentuma.techloan.features.auth.dto;

public class AuthResponse {
    private boolean success;
    private UserResponse user;
    private String token;
    private String refreshToken;
    private String message;
    private String timestamp;

    public AuthResponse() {}

    public AuthResponse(boolean success, UserResponse user, String token,
                        String refreshToken, String message, String timestamp) {
        this.success = success;
        this.user = user;
        this.token = token;
        this.refreshToken = refreshToken;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static AuthResponseBuilder builder() { return new AuthResponseBuilder(); }

    public boolean isSuccess()                          { return success; }
    public void setSuccess(boolean success)             { this.success = success; }

    public UserResponse getUser()                       { return user; }
    public void setUser(UserResponse user)              { this.user = user; }

    public String getToken()                            { return token; }
    public void setToken(String token)                  { this.token = token; }

    public String getRefreshToken()                     { return refreshToken; }
    public void setRefreshToken(String refreshToken)    { this.refreshToken = refreshToken; }

    public String getMessage()                          { return message; }
    public void setMessage(String message)              { this.message = message; }

    public String getTimestamp()                        { return timestamp; }
    public void setTimestamp(String timestamp)          { this.timestamp = timestamp; }

    public static class AuthResponseBuilder {
        private boolean success;
        private UserResponse user;
        private String token;
        private String refreshToken;
        private String message;
        private String timestamp;

        public AuthResponseBuilder success(boolean v)          { this.success = v; return this; }
        public AuthResponseBuilder user(UserResponse v)        { this.user = v; return this; }
        public AuthResponseBuilder token(String v)             { this.token = v; return this; }
        public AuthResponseBuilder refreshToken(String v)      { this.refreshToken = v; return this; }
        public AuthResponseBuilder message(String v)           { this.message = v; return this; }
        public AuthResponseBuilder timestamp(String v)         { this.timestamp = v; return this; }

        public AuthResponse build() {
            return new AuthResponse(success, user, token, refreshToken, message, timestamp);
        }
    }
}
