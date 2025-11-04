package apap.ti._5.accommodation_2306275651_be.restservice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti._5.accommodation_2306275651_be.model.Booking;
import apap.ti._5.accommodation_2306275651_be.model.Room;
import apap.ti._5.accommodation_2306275651_be.model.RoomType;
import apap.ti._5.accommodation_2306275651_be.repository.BookingRepository;
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
    private final BookingRepository bookingRepository;

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
    
    // ✅ REPLACE maintenance schedule (bukan append)
    // Jika ada maintenance baru, replace yang lama
    if (dto.getMaintenanceStart() != null || dto.getMaintenanceEnd() != null) {
        
        // Log old maintenance (jika ada)
        if (room.getMaintenanceStart() != null && room.getMaintenanceEnd() != null) {
            System.out.println("⚠️ Replacing existing maintenance schedule:");
            System.out.println("   Old: " + room.getMaintenanceStart() + " - " + room.getMaintenanceEnd());
            System.out.println("   New: " + dto.getMaintenanceStart() + " - " + dto.getMaintenanceEnd());
        }
        
        // ✅ Set maintenance baru (REPLACE)
        room.setMaintenanceStart(dto.getMaintenanceStart());
        room.setMaintenanceEnd(dto.getMaintenanceEnd());
        
        // ✅ Set availability ke 0 (unavailable) saat ada maintenance
        if (dto.getMaintenanceStart() != null && dto.getMaintenanceEnd() != null) {
            room.setAvailabilityStatus(0);
        }
    }
    
    room.setUpdatedDate(LocalDateTime.now());
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
        try {
            // ✅ Parse string dates ke LocalDateTime
            LocalDateTime maintenanceStart = LocalDateTime.parse(startDate);
            LocalDateTime maintenanceEnd = LocalDateTime.parse(endDate);
            
            System.out.println("🔍 Checking booking conflicts:");
            System.out.println("   Room ID: " + roomID);
            System.out.println("   Maintenance Start: " + maintenanceStart);
            System.out.println("   Maintenance End: " + maintenanceEnd);
            
            // ✅ Cari booking yang conflict
            List<Booking> conflicts = bookingRepository.findConflictingBookings(
                roomID, 
                maintenanceStart, 
                maintenanceEnd
            );
            
            if (!conflicts.isEmpty()) {
                System.out.println("   ❌ Found " + conflicts.size() + " conflicting booking(s):");
                for (Booking b : conflicts) {
                    System.out.println("      - Booking ID: " + b.getBookingID() + 
                                     " (Check-in: " + b.getCheckInDate() + 
                                     ", Check-out: " + b.getCheckOutDate() + ")");
                }
                return true;
            }
            
            System.out.println("   ✅ No booking conflicts found");
            return false;
            
        } catch (Exception ex) {
            System.err.println("   ⚠️ Error checking booking conflict: " + ex.getMessage());
            // ✅ Jika error parsing atau query, anggap tidak ada conflict (safe)
            return false;
        }
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
    
    public RoomResponseDTO convertToResponseDTO(Room room) {
    return RoomResponseDTO.builder()
            .roomID(room.getRoomID())
            .name(room.getName())
            .availabilityStatus(room.getAvailabilityStatus())
            .availabilityStatusName(room.getAvailabilityStatus() == 1 ? "Available" : "Unavailable")
            .activeRoom(room.getActiveRoom())
            .activeRoomName(room.getActiveRoom() == 1 ? "Active" : "Inactive")
            .maintenanceStart(room.getMaintenanceStart())  // ✅ Include
            .maintenanceEnd(room.getMaintenanceEnd())      // ✅ Include
            .capacity(room.getRoomType().getCapacity())
            .price(room.getRoomType().getPrice())
            .floor(room.getRoomType().getFloor())
            .roomTypeID(room.getRoomType().getRoomTypeID())
            .roomTypeName(room.getRoomType().getName())
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