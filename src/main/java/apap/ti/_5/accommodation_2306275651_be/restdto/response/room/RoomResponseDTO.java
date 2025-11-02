package apap.ti._5.accommodation_2306275651_be.restdto.response.room;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDTO {
    private String roomID;
    private String name;
    private Integer availabilityStatus;
    private String availabilityStatusName;
    private Integer activeRoom;
    private String activeRoomName;
    private LocalDateTime maintenanceStart;
    private LocalDateTime maintenanceEnd;
    private Integer capacity;
    private Integer price;
    private Integer floor;
    private String roomTypeID;
    private String roomTypeName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}