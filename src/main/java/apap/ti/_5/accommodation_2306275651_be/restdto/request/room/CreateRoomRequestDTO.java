package apap.ti._5.accommodation_2306275651_be.restdto.request.room;

import java.time.LocalDateTime;

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
public class CreateRoomRequestDTO {
    
    // ✅ Fields untuk Room (existing)
    private String name; // Optional, auto-generated if not provided
    private String roomTypeID; // Optional untuk create property flow
    private Integer availabilityStatus;
    private Integer activeRoom;
    private LocalDateTime maintenanceStart;
    private LocalDateTime maintenanceEnd;
    
    // ✅ Fields untuk Room Type (tambahan untuk create property flow)
    @NotBlank(message = "Room type name is required")
    private String roomTypeName; // Single Room, Double Room, dll
    
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Integer price;
    
    @NotNull(message = "Floor is required")
    @Min(value = 0, message = "Floor cannot be negative")
    private Integer floor;
    
    @NotNull(message = "Unit count is required")
    @Min(value = 1, message = "Unit count must be at least 1")
    private Integer unit; // Banyak kamar yang akan dibuat
    
    @NotBlank(message = "Facility is required")
    private String facility;
    
    @NotBlank(message = "Room type description is required")
    private String roomTypeDescription;
}