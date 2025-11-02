package apap.ti._5.accommodation_2306275651_be.restdto.response.property;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponseDTO {
    private String propertyID;
    private String propertyName;
    private Integer type;
    private String typeName;
    private String address;
    private Integer province;
    private String description;
    private Integer totalRoom;
    private Integer activeStatus;
    private String activeStatusName;
    private Integer income;
    private String ownerName;
    private String ownerID;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    
    // ✅ Import class terpisah, bukan inner class
    private List<RoomTypeInfoDTO> roomTypes;
}