package apap.ti._5.accommodation_2306275651_be.restservice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti._5.accommodation_2306275651_be.model.Room;
import apap.ti._5.accommodation_2306275651_be.model.RoomType;
import apap.ti._5.accommodation_2306275651_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306275651_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.CreateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.UpdateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.room.RoomResponseDTO;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomRestServiceImpl implements RoomRestService {
    
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Override
    public RoomResponseDTO createRoom(CreateRoomRequestDTO dto) {
        // Find room type
        RoomType roomType = roomTypeRepository.findById(dto.getRoomTypeID())
                .orElseThrow(() -> new RuntimeException("Room type not found with id: " + dto.getRoomTypeID()));
        
        // ✅ Generate room ID jika tidak ada name atau empty
         String roomID = generateRoomID(roomType);
    
    System.out.println("🏠 Creating Room:");
    System.out.println("   Room ID: " + roomID);
    System.out.println("   Room Type ID: " + dto.getRoomTypeID());
    System.out.println("   Room Type Name: " + roomType.getName());
    
    // Manual conversion DTO to Entity
    Room room = Room.builder()
            .roomID(roomID) // ✅ Use generated ID
            .name(roomID)   // ✅ Name sama dengan ID (HOT-4000-001-201)
            .roomType(roomType) // ✅ Set relationship
            .availabilityStatus(dto.getAvailabilityStatus())
            .activeRoom(dto.getActiveRoom())
            .maintenanceStart(dto.getMaintenanceStart())
            .maintenanceEnd(dto.getMaintenanceEnd())
            .build();
    
    Room savedRoom = roomRepository.save(room);
    System.out.println("   ✅ Room Saved to DB: " + savedRoom.getRoomID());
    
    return convertToResponseDTO(savedRoom);
}

    @Override
    public RoomResponseDTO getRoomById(String roomID) {
        Room room = roomRepository.findById(roomID)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomID));
        return convertToResponseDTO(room);
    }

    @Override
    public Room getRoomEntityById(String roomID) {
        return roomRepository.findById(roomID)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomID));
    }

    @Override
    public List<RoomResponseDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomResponseDTO> getRoomsByRoomType(String roomTypeID) {
        return roomRepository.findByRoomType_RoomTypeID(roomTypeID).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomResponseDTO> getAvailableRooms() {
        return roomRepository.findByAvailabilityStatus(1).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

   @Override
public RoomResponseDTO updateRoom(String roomID, UpdateRoomRequestDTO dto) {
    Room room = roomRepository.findById(roomID)
            .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomID));
    
    // ✅ Update fields (hanya yang tidak null)
    if (dto.getName() != null) {
        room.setName(dto.getName());
    }
    if (dto.getAvailabilityStatus() != null) {
        room.setAvailabilityStatus(dto.getAvailabilityStatus());
    }
    if (dto.getActiveRoom() != null) {
        room.setActiveRoom(dto.getActiveRoom());
    }
    
    // ✅ Update maintenance schedule (REPLACE yang lama)
    if (dto.getMaintenanceStart() != null) {
        room.setMaintenanceStart(dto.getMaintenanceStart());
    }
    if (dto.getMaintenanceEnd() != null) {
        room.setMaintenanceEnd(dto.getMaintenanceEnd());
    }
    
    Room updatedRoom = roomRepository.save(room);
    return convertToResponseDTO(updatedRoom);
}

    @Override
    public void deleteRoom(String roomID) {
        if (!roomRepository.existsById(roomID)) {
            throw new RuntimeException("Room not found with id: " + roomID);
        }
        roomRepository.deleteById(roomID);
    }
    
    // ✅ Implement method untuk check booking conflict
    @Override
    public boolean hasBookingConflict(String roomID, String startDate, String endDate) {
        // TODO: Implement booking check ketika ada model Booking
        // List<Booking> conflicts = bookingRepository.findConflictingBookings(roomID, startDate, endDate);
        // return !conflicts.isEmpty();
        return false; // Sementara return false
    }
    
 private String generateRoomID(RoomType roomType) {
    String propertyID = roomType.getProperty().getPropertyID();
    Integer floor = roomType.getFloor();
    
    System.out.println("🔑 Generating Room ID:");
    System.out.println("   Property ID: " + propertyID);
    System.out.println("   Floor: " + floor);
    System.out.println("   Room Type ID: " + roomType.getRoomTypeID());
    
    // ✅ Hitung SEMUA rooms di floor tersebut (cross room types)
    List<Room> existingRoomsOnFloor = roomRepository.findByPropertyIDAndFloor(
        propertyID, 
        floor
    );
    
    System.out.println("   🔍 Existing Rooms on Floor " + floor + ":");
    for (Room r : existingRoomsOnFloor) {
        System.out.println("      - " + r.getRoomID() + " (" + r.getRoomType().getName() + ")");
    }
    
    Integer unitNumber = existingRoomsOnFloor.size() + 1;
    
    System.out.println("   📊 Total Existing Rooms: " + existingRoomsOnFloor.size());
    System.out.println("   ➕ New Unit Number: " + unitNumber);
    
    // ✅ Format: propertyID-floorunit
    String floorUnit = String.format("%d%02d", floor, unitNumber);
    String roomID = propertyID + "-" + floorUnit;
    
    System.out.println("   ✅ Generated Room ID: " + roomID);
    System.out.println();
    
    return roomID;
}

    // Helper method untuk konversi Entity -> DTO
    private RoomResponseDTO convertToResponseDTO(Room room) {
        return RoomResponseDTO.builder()
                .roomID(room.getRoomID())
                .name(room.getName())
                .availabilityStatus(room.getAvailabilityStatus())
                .availabilityStatusName(room.getAvailabilityStatus() == 1 ? "Available" : "Unavailable")
                .activeRoom(room.getActiveRoom())
                .activeRoomName(room.getActiveRoom() == 1 ? "Active" : "Non-Active")
                .maintenanceStart(room.getMaintenanceStart())
                .maintenanceEnd(room.getMaintenanceEnd())
                .roomTypeID(room.getRoomType() != null ? room.getRoomType().getRoomTypeID() : null)
                .roomTypeName(room.getRoomType() != null ? room.getRoomType().getName() : null)
                .capacity(room.getRoomType() != null ? room.getRoomType().getCapacity() : null)
                .createdDate(room.getCreatedDate())
                .updatedDate(room.getUpdatedDate())
                .build();
    }
  @Override
    public List<RoomResponseDTO> getRoomsByPropertyAndFloor(String propertyID, Integer floor) {
    // Get all room types for this property
    List<RoomType> roomTypes = roomTypeRepository.findByProperty_PropertyID(propertyID);
    
    // Filter room types by floor dan ambil semua rooms
    List<Room> rooms = new ArrayList<>();
    for (RoomType roomType : roomTypes) {
        if (floor != null && roomType.getFloor() == floor) {
            List<Room> roomsForType = roomRepository.findByRoomType_RoomTypeID(roomType.getRoomTypeID());
            rooms.addAll(roomsForType);
        }
    }
    
    return rooms.stream()
            .map(this::convertToResponseDTO)  // ✅ BENAR - gunakan method yang ada
            .collect(Collectors.toList());
}
}