package apap.ti._5.accommodation_2306275651_be.restdto.response.property;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeInfoDTO {
    private String roomTypeID;
    private String roomTypeName;
    private Integer floor;
    private Integer capacity;
    private Integer price;
    private String facility;
    private String description;
    private List<String> roomIDs; // List ID kamar yang dibuat
}