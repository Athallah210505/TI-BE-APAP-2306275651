package apap.ti._5.accommodation_2306275651_be.restservice;

import java.util.List;

import apap.ti._5.accommodation_2306275651_be.model.Room;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.CreateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.UpdateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.room.RoomResponseDTO;

public interface RoomRestService {
    RoomResponseDTO createRoom(CreateRoomRequestDTO dto);
    RoomResponseDTO getRoomById(String roomID);
    List<RoomResponseDTO> getAllRooms();
    List<RoomResponseDTO> getRoomsByRoomType(String roomTypeID);
    List<RoomResponseDTO> getAvailableRooms();
    RoomResponseDTO updateRoom(String roomID, UpdateRoomRequestDTO dto);
    void deleteRoom(String roomID);
    boolean hasBookingConflict(String roomID, String startDate, String endDate);
    List<RoomResponseDTO> getRoomsByPropertyAndFloor(String propertyID, Integer floor);
    Room getRoomEntityById(String roomID);
    
}