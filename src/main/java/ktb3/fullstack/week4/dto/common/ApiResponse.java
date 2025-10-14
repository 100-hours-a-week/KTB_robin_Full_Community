package ktb3.fullstack.week4.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"message", "data"})
@AllArgsConstructor
public class ApiResponse<T> {

    @JsonProperty("message")
    private final String message;

    @JsonProperty("data")
    private T data;

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(message, data);
    }

    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null);
    }
}
