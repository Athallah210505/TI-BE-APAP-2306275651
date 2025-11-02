package apap.ti._5.accommodation_2306275651_be.restdto.request.booking;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequestDTO {
    

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
    
    @NotBlank(message = "Customer ID is required")
    private String customerID;
}