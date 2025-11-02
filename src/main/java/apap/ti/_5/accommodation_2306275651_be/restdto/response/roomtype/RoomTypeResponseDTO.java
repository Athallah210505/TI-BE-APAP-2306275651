package apap.ti._5.accommodation_2306275651_be.restdto.response.roomtype;

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
public class RoomTypeResponseDTO {
    private String roomTypeID;
    private String name;
    private Integer price;
    private String description;
    private Integer capacity;
    private String facility;
    private Integer floor;
    private String propertyID;
    private String propertyName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<String> roomIDs;
}