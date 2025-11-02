package apap.ti._5.accommodation_2306275651_be.restservice;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti._5.accommodation_2306275651_be.model.Property;
import apap.ti._5.accommodation_2306275651_be.model.RoomType;
import apap.ti._5.accommodation_2306275651_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306275651_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306275651_be.restdto.request.roomtype.CreateRoomTypeRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.roomtype.UpdateRoomTypeRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.roomtype.RoomTypeResponseDTO;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomTypeRestServiceImpl implements RoomTypeRestService {
    
    private final RoomTypeRepository roomTypeRepository;
    private final PropertyRepository propertyRepository;

    @Override
    public RoomTypeResponseDTO createRoomType(CreateRoomTypeRequestDTO dto) {
        // Find property
        Property property = propertyRepository.findById(dto.getPropertyID())
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + dto.getPropertyID()));
        
        // ✅ Generate room type ID
        String roomTypeID = generateRoomTypeID(dto);
        
        // Manual conversion DTO to Entity
        RoomType roomType = RoomType.builder()
                .roomTypeID(roomTypeID) // ✅ Set generated ID
                .name(dto.getName())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .capacity(dto.getCapacity())
                .facility(dto.getFacility())
                .floor(dto.getFloor())
                .property(property)
                .build();
        
        RoomType savedRoomType = roomTypeRepository.save(roomType);
        return convertToResponseDTO(savedRoomType);
    }

    @Override
    public RoomTypeResponseDTO getRoomTypeById(String roomTypeID) {
        RoomType roomType = roomTypeRepository.findById(roomTypeID)
                .orElseThrow(() -> new RuntimeException("Room type not found with id: " + roomTypeID));
        return convertToResponseDTO(roomType);
    }

    @Override
    public List<RoomTypeResponseDTO> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomTypeResponseDTO> getRoomTypesByProperty(String propertyID) {
        return roomTypeRepository.findByProperty_PropertyID(propertyID).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoomTypeResponseDTO updateRoomType(String roomTypeID, UpdateRoomTypeRequestDTO dto) {
        RoomType roomType = roomTypeRepository.findById(roomTypeID)
                .orElseThrow(() -> new RuntimeException("Room type not found with id: " + roomTypeID));
        
        // Update fields
        roomType.setName(dto.getName());
        roomType.setPrice(dto.getPrice());
        roomType.setDescription(dto.getDescription());
        roomType.setCapacity(dto.getCapacity());
        roomType.setFacility(dto.getFacility());
        roomType.setFloor(dto.getFloor());
        
        RoomType updatedRoomType = roomTypeRepository.save(roomType);
        return convertToResponseDTO(updatedRoomType);
    }

    @Override
    public void deleteRoomType(String roomTypeID) {
        if (!roomTypeRepository.existsById(roomTypeID)) {
            throw new RuntimeException("Room type not found with id: " + roomTypeID);
        }
        roomTypeRepository.deleteById(roomTypeID);
    }
    

    @Override
    public boolean isDuplicateRoomTypeFloor(String propertyID, String roomTypeName, Integer floor) {
        if (floor == null) {
            return false;
        }
        List<RoomType> existingRoomTypes = roomTypeRepository.findByProperty_PropertyID(propertyID);
        
        return existingRoomTypes.stream()
                .anyMatch(rt -> rt.getName().equals(roomTypeName) && rt.getFloor() == floor);
    }

    private String generateRoomTypeID(CreateRoomTypeRequestDTO dto) {
    // PropertyID format: PREFIX-4chars-3digits (contoh: APT-0000-004)
    // Split by dash dan ambil bagian terakhir (counter)
    String propertyID = dto.getPropertyID();
    String[] parts = propertyID.split("-");
    String propertyCounter = parts.length >= 3 ? parts[2] : "001";
    
    // Nama tipe kamar (replace space dengan underscore)
    String roomTypeName = dto.getName().replace(" ", "_");
    
    // Nomor lantai kamar
    String floor = String.format("%d", dto.getFloor());
    
    // ✅ Format: 003–Single_Room–2 (menggunakan em dash –)
    return propertyCounter + "–" + roomTypeName + "–" + floor;
}

    // Helper method untuk konversi Entity -> DTO
    private RoomTypeResponseDTO convertToResponseDTO(RoomType roomType) {
        return RoomTypeResponseDTO.builder()
                .roomTypeID(roomType.getRoomTypeID())
                .name(roomType.getName())
                .price(roomType.getPrice())
                .description(roomType.getDescription())
                .capacity(roomType.getCapacity())
                .facility(roomType.getFacility())
                .floor(roomType.getFloor())
                .propertyID(roomType.getProperty() != null ? roomType.getProperty().getPropertyID() : null)
                .propertyName(roomType.getProperty() != null ? roomType.getProperty().getPropertyName() : null)
                .createdDate(roomType.getCreatedDate())
                .updatedDate(roomType.getUpdatedDate())
                .build();
    }
}