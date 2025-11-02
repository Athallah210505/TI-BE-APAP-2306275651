package apap.ti._5.accommodation_2306275651_be.restdto.request.room;

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
public class UpdateRoomRequestDTO {
    
    @NotBlank(message = "Room name is required")
    private String name;
    
    @NotNull(message = "Availability status is required")
    @Min(value = 0, message = "Availability status must be 0 or 1")
    @Max(value = 1, message = "Availability status must be 0 or 1")
    private Integer availabilityStatus;
    
    @NotNull(message = "Active room status is required")
    @Min(value = 0, message = "Active room must be 0 or 1")
    @Max(value = 1, message = "Active room must be 0 or 1")
    private Integer activeRoom;
    
    private LocalDateTime maintenanceStart;
    private LocalDateTime maintenanceEnd;
}