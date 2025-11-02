package apap.ti._5.accommodation_2306275651_be.restcontroller;

import apap.ti._5.accommodation_2306275651_be.restdto.request.room.CreateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.UpdateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.BaseResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.room.RoomResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restservice.RoomRestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomRestController {
    
    private final RoomRestService roomRestService;
    
    @PostMapping
    public ResponseEntity<BaseResponseDTO<RoomResponseDTO>> createRoom(
            @Valid @RequestBody CreateRoomRequestDTO request) {
        RoomResponseDTO response = roomRestService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponseDTO.success("Room created successfully", response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<RoomResponseDTO>> getRoom(@PathVariable String id) {
        RoomResponseDTO response = roomRestService.getRoomById(id);
        return ResponseEntity.ok(BaseResponseDTO.success("Room retrieved successfully", response));
    }
    
    @GetMapping
    public ResponseEntity<BaseResponseDTO<List<RoomResponseDTO>>> getAllRooms() {
        List<RoomResponseDTO> responses = roomRestService.getAllRooms();
        return ResponseEntity.ok(BaseResponseDTO.success("Rooms retrieved successfully", responses));
    }
    
    @GetMapping("/room-type/{roomTypeId}")
    public ResponseEntity<BaseResponseDTO<List<RoomResponseDTO>>> getRoomsByRoomType(
            @PathVariable String roomTypeId) {
        List<RoomResponseDTO> responses = roomRestService.getRoomsByRoomType(roomTypeId);
        return ResponseEntity.ok(BaseResponseDTO.success("Room type rooms retrieved successfully", responses));
    }
    
    @GetMapping("/available")
    public ResponseEntity<BaseResponseDTO<List<RoomResponseDTO>>> getAvailableRooms() {
        List<RoomResponseDTO> responses = roomRestService.getAvailableRooms();
        return ResponseEntity.ok(BaseResponseDTO.success("Available rooms retrieved successfully", responses));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<RoomResponseDTO>> updateRoom(
            @PathVariable String id,
            @Valid @RequestBody UpdateRoomRequestDTO request) {
        RoomResponseDTO response = roomRestService.updateRoom(id, request);
        return ResponseEntity.ok(BaseResponseDTO.success("Room updated successfully", response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<Void>> deleteRoom(@PathVariable String id) {
        roomRestService.deleteRoom(id);
        return ResponseEntity.ok(BaseResponseDTO.success("Room deleted successfully", null));
    }
}