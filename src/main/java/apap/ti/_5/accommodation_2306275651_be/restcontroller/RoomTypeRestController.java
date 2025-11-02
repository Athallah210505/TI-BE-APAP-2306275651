package apap.ti._5.accommodation_2306275651_be.restcontroller;

import apap.ti._5.accommodation_2306275651_be.restdto.request.roomtype.CreateRoomTypeRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.roomtype.UpdateRoomTypeRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.BaseResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.roomtype.RoomTypeResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restservice.RoomTypeRestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
@RequiredArgsConstructor
public class RoomTypeRestController {
    
    private final RoomTypeRestService roomTypeRestService;
    
    @PostMapping
    public ResponseEntity<BaseResponseDTO<RoomTypeResponseDTO>> createRoomType(
            @Valid @RequestBody CreateRoomTypeRequestDTO request) {
        RoomTypeResponseDTO response = roomTypeRestService.createRoomType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponseDTO.success("Room type created successfully", response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<RoomTypeResponseDTO>> getRoomType(@PathVariable String id) {
        RoomTypeResponseDTO response = roomTypeRestService.getRoomTypeById(id);
        return ResponseEntity.ok(BaseResponseDTO.success("Room type retrieved successfully", response));
    }
    
    @GetMapping
    public ResponseEntity<BaseResponseDTO<List<RoomTypeResponseDTO>>> getAllRoomTypes() {
        List<RoomTypeResponseDTO> responses = roomTypeRestService.getAllRoomTypes();
        return ResponseEntity.ok(BaseResponseDTO.success("Room types retrieved successfully", responses));
    }
    
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<BaseResponseDTO<List<RoomTypeResponseDTO>>> getRoomTypesByProperty(
            @PathVariable String propertyId) {
        List<RoomTypeResponseDTO> responses = roomTypeRestService.getRoomTypesByProperty(propertyId);
        return ResponseEntity.ok(BaseResponseDTO.success("Property room types retrieved successfully", responses));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<RoomTypeResponseDTO>> updateRoomType(
            @PathVariable String id,
            @Valid @RequestBody UpdateRoomTypeRequestDTO request) {
        RoomTypeResponseDTO response = roomTypeRestService.updateRoomType(id, request);
        return ResponseEntity.ok(BaseResponseDTO.success("Room type updated successfully", response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<Void>> deleteRoomType(@PathVariable String id) {
        roomTypeRestService.deleteRoomType(id);
        return ResponseEntity.ok(BaseResponseDTO.success("Room type deleted successfully", null));
    }
}