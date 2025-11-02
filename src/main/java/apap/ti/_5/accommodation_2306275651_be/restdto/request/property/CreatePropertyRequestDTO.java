package apap.ti._5.accommodation_2306275651_be.restdto.request.property;

import java.util.List;

import apap.ti._5.accommodation_2306275651_be.restdto.request.room.CreateRoomRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePropertyRequestDTO {
    
    @NotBlank(message = "Property name is required")
    private String propertyName;
    
    @NotNull(message = "Type is required")
    @Min(value = 1, message = "Type must be between 1-3")
    @Max(value = 3, message = "Type must be between 1-3")
    private Integer type;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    @NotNull(message = "Province is required")
    private Integer province;
    
    private String description;
    
    @NotNull(message = "Total room is required")
    @Min(value = 0, message = "Total room cannot be negative")
    private Integer totalRoom;
    
    @NotBlank(message = "Owner name is required")
    private String ownerName;
    
    @NotBlank(message = "Owner ID is required")
    private String ownerID;

    @NotNull(message = "Room types are required")
    @NotEmpty(message = "Property must have at least 1 room type")
    @Valid
    private List<CreateRoomRequestDTO> roomTypes;
}