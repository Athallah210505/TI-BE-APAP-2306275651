package apap.ti._5.accommodation_2306275651_be.restdto.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone="Asia/Jakarta")
    private Date timestamp;

    public static <T> BaseResponseDTO<T> success(String message, T data) {
        return BaseResponseDTO.<T>builder()
                .status(200)
                .message(message)
                .data(data)

                .timestamp(new Date())
                .build();
    }

    public static <T> BaseResponseDTO<T> error(int status, String message) {
        return BaseResponseDTO.<T>builder()
                .status(status)
                .message(message)
                .timestamp(new Date())
                .build();
    }
}