package apap.ti._5.accommodation_2306275651_be.restdto.request.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaintenanceRequestDTO {
    
    @NotBlank(message = "Room ID is required")
    private String roomID;
    
    @NotNull(message = "Maintenance start is required")
    private LocalDateTime maintenanceStart;
    
    @NotNull(message = "Maintenance end is required")
    private LocalDateTime maintenanceEnd;
}