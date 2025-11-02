package apap.ti._5.accommodation_2306275651_be.restdto.request.booking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingStatusRequestDTO {
    
    @NotNull(message = "Status is required")
    @Min(value = 0, message = "Status must be between 0-4")
    @Max(value = 4, message = "Status must be between 0-4")
    private Integer status;
    
    private Integer refund;
    private Integer extraPay;

    
    @NotBlank(message = "Booking ID is required")
    private String bookingID;
}