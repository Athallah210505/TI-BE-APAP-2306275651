package apap.ti._5.accommodation_2306275651_be.restservice;

import java.util.List;

import apap.ti._5.accommodation_2306275651_be.restdto.request.roomtype.CreateRoomTypeRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.roomtype.UpdateRoomTypeRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.roomtype.RoomTypeResponseDTO;

public interface RoomTypeRestService {
    RoomTypeResponseDTO createRoomType(CreateRoomTypeRequestDTO dto);
    RoomTypeResponseDTO getRoomTypeById(String roomTypeID);
    List<RoomTypeResponseDTO> getAllRoomTypes();
    List<RoomTypeResponseDTO> getRoomTypesByProperty(String propertyID);
    RoomTypeResponseDTO updateRoomType(String roomTypeID, UpdateRoomTypeRequestDTO dto);
    void deleteRoomType(String roomTypeID);
    boolean isDuplicateRoomTypeFloor(String propertyID, String roomTypeName, Integer floor);
}