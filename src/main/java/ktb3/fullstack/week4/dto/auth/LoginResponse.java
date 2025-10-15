package ktb3.fullstack.week4.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("access_expired_in")
    private long accessExpiredIn;

    @JsonProperty("refresh_expired_in")
    private long refreshExpiredIn;
}
