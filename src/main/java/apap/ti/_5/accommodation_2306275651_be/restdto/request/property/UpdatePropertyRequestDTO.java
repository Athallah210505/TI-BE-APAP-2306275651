package apap.ti._5.accommodation_2306275651_be.restdto.request.property;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePropertyRequestDTO {
    
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
    
    @NotNull(message = "Active status is required")
    @Min(value = 0, message = "Active status must be 0 or 1")
    @Max(value = 1, message = "Active status must be 0 or 1")
    private Integer activeStatus;
}