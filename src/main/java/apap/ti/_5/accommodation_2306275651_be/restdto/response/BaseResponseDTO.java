package apap.ti._5.accommodation_2306275651_be.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponseDTO<T> {
    private int status;
    private String message;
    private T data;
    private Long timestamp;

    public static <T> BaseResponseDTO<T> success(String message, T data) {
        return BaseResponseDTO.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> BaseResponseDTO<T> error(int status, String message) {
        return BaseResponseDTO.<T>builder()
                .status(status)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}