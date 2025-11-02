package apap.ti._5.accommodation_2306275651_be.restdto.request.booking;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
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
public class UpdateBookingRequestDTO {
    
     private String roomID;
     
    @NotNull(message = "Check-in date is required")
    private LocalDateTime checkInDate;
    
    @NotNull(message = "Check-out date is required")
    private LocalDateTime checkOutDate;
    
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;
    
    @NotBlank(message = "Customer phone is required")
    private String customerPhone;
    
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    
    private Boolean isBreakfast;
    
    @NotNull(message = "Status is required")
    @Min(value = 0, message = "Status must be between 0-4")
    @Max(value = 4, message = "Status must be between 0-4")
    private Integer status;
    
    private Integer refund;
    private Integer extraPay;
}