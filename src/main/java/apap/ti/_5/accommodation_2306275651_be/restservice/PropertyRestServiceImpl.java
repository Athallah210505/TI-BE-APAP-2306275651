package apap.ti._5.accommodation_2306275651_be.restservice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti._5.accommodation_2306275651_be.model.Property;
import apap.ti._5.accommodation_2306275651_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306275651_be.restdto.request.property.CreatePropertyRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.property.UpdatePropertyRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.CreateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.roomtype.CreateRoomTypeRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.property.PropertyResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.property.RoomTypeInfoDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.room.RoomResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.roomtype.RoomTypeResponseDTO;

@Service
@Transactional
public class PropertyRestServiceImpl implements PropertyRestService {
    
    @Autowired
    private PropertyRepository propertyRepository;
    
    @Autowired
    private RoomTypeRestService roomTypeRestService;
    
    @Autowired
    private RoomRestService roomRestService;
    
    @Override
    public PropertyResponseDTO createProperty(CreatePropertyRequestDTO createPropertyRequestDTO) {
        
        // ✅ Validasi duplikasi property-roomtype-floor dalam request
        Set<String> roomTypeCombinations = new HashSet<>();
        for (CreateRoomRequestDTO roomType : createPropertyRequestDTO.getRoomTypes()) {
            String combination = roomType.getRoomTypeName() + "-" + roomType.getFloor();
            if (!roomTypeCombinations.add(combination)) {
                throw new RuntimeException("Duplikasi kombinasi tipe kamar–lantai tidak diperbolehkan: " + 
                                         roomType.getRoomTypeName() + " di lantai " + roomType.getFloor());
            }
        }
        
        // ✅ Hitung total rooms dari semua room types
        int calculatedTotalRoom = createPropertyRequestDTO.getRoomTypes().stream()
                .mapToInt(CreateRoomRequestDTO::getUnit)
                .sum();
        
        // ✅ Validasi total room match
        if (calculatedTotalRoom != createPropertyRequestDTO.getTotalRoom().intValue()) {
            throw new RuntimeException("Total room tidak sesuai. Expected: " + calculatedTotalRoom + 
                                     ", Got: " + createPropertyRequestDTO.getTotalRoom());
        }
        
        // ✅ Generate property ID sesuai format baru
        String propertyID = generatePropertyID(createPropertyRequestDTO);
        
        // ✅ Create Property
        Property property = Property.builder()
                .propertyID(propertyID)
                .propertyName(createPropertyRequestDTO.getPropertyName())
                .type(createPropertyRequestDTO.getType())
                .address(createPropertyRequestDTO.getAddress())
                .province(createPropertyRequestDTO.getProvince())
                .description(createPropertyRequestDTO.getDescription())
                .totalRoom(createPropertyRequestDTO.getTotalRoom())
                .activeStatus(1) // Default active
                .income(0) // Default 0
                .ownerName(createPropertyRequestDTO.getOwnerName())
                .ownerID(UUID.fromString(createPropertyRequestDTO.getOwnerID()))
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
        
        Property savedProperty = propertyRepository.save(property);
        
        // ✅ List untuk menyimpan room type info dengan room IDs
        List<RoomTypeInfoDTO> roomTypeInfoList = new ArrayList<>();
        
        // ✅ Create Room Types dan Rooms
        for (CreateRoomRequestDTO roomTypeData : createPropertyRequestDTO.getRoomTypes()) {
            
            // Create RoomType
            CreateRoomTypeRequestDTO roomTypeDTO = CreateRoomTypeRequestDTO.builder()
                    .name(roomTypeData.getRoomTypeName())
                    .price(roomTypeData.getPrice())
                    .description(roomTypeData.getRoomTypeDescription())
                    .capacity(roomTypeData.getCapacity())
                    .facility(roomTypeData.getFacility())
                    .floor(roomTypeData.getFloor())
                    .propertyID(propertyID)
                    .unitCount(roomTypeData.getUnit())
                    .build();
            
            RoomTypeResponseDTO createdRoomType = roomTypeRestService.createRoomType(roomTypeDTO);
            
            // ✅ List untuk menyimpan room IDs yang dibuat
            List<String> roomIDs = new ArrayList<>();
            
            // ✅ Create Rooms for this room type
            for (int i = 1; i <= roomTypeData.getUnit(); i++) {
                CreateRoomRequestDTO createRoomDTO = CreateRoomRequestDTO.builder()
                        .name(generateRoomName(propertyID, roomTypeData.getFloor(), i))
                        .roomTypeID(createdRoomType.getRoomTypeID())
                        .availabilityStatus(1) // Available
                        .activeRoom(1) // Active
                        .build();
                
                RoomResponseDTO createdRoom = roomRestService.createRoom(createRoomDTO);
                roomIDs.add(createdRoom.getRoomID());
            }
            
            // ✅ Build room type info dengan room IDs (gunakan class terpisah)
            RoomTypeInfoDTO roomTypeInfo = RoomTypeInfoDTO.builder()
                    .roomTypeID(createdRoomType.getRoomTypeID())
                    .roomTypeName(createdRoomType.getName())
                    .floor(createdRoomType.getFloor())
                    .capacity(createdRoomType.getCapacity())
                    .price(createdRoomType.getPrice())
                    .facility(createdRoomType.getFacility())
                    .description(createdRoomType.getDescription())
                    .roomIDs(roomIDs)
                    .build();
            
            roomTypeInfoList.add(roomTypeInfo);
        }
        
        // ✅ Convert to response DTO dengan room type info
        return convertToPropertyResponseDTO(savedProperty, roomTypeInfoList);
    }
    
    // ✅ Helper method untuk generate property ID
    private String generatePropertyID(CreatePropertyRequestDTO dto) {
        // Get type prefix (HOT, VIL, APT)
        String typePrefix = getTypePrefix(dto.getType());
        
        // 4 karakter terakhir dari UUID Owner
        String ownerSuffix = dto.getOwnerID() != null && dto.getOwnerID().length() >= 4 
            ? dto.getOwnerID().substring(dto.getOwnerID().length() - 4)
            : "0000";
        
        // 3 digit counter properti ke-N (hitung dari database)
        long propertyCount = propertyRepository.count() + 1;
        String counter = String.format("%03d", propertyCount);
        
        // ✅ Format: PREFIX-4chars-3digits
        // Contoh: APT-0000-004 (untuk Apartemen, owner ending 0000, property ke-4)
        return typePrefix + "-" + ownerSuffix + "-" + counter;
    }
    
    // ✅ Helper method untuk generate room name sesuai format: propertyID-floorunit
    private String generateRoomName(String propertyID, Integer floor, Integer unitNumber) {
        // Format: propertyID-floorunit 
        // Contoh: APT-0000-004-101 untuk lantai 1 unit 1
        String floorUnit = String.format("%d%02d", floor, unitNumber);
        return propertyID + "-" + floorUnit;
    }
    
    @Override
    public PropertyResponseDTO updateProperty(String id, UpdatePropertyRequestDTO updatePropertyRequestDTO) {
        Optional<Property> propertyOpt = propertyRepository.findById(id);
        
        if (propertyOpt.isEmpty()) {
            return null;
        }
        
        Property existingProperty = propertyOpt.get();
        
        // Update fields
        existingProperty.setPropertyName(updatePropertyRequestDTO.getPropertyName() != null ? 
                                       updatePropertyRequestDTO.getPropertyName() : existingProperty.getPropertyName());
        
        if (updatePropertyRequestDTO.getType() != null) {
            existingProperty.setType(updatePropertyRequestDTO.getType());
        }
        
        existingProperty.setAddress(updatePropertyRequestDTO.getAddress() != null ? 
                                  updatePropertyRequestDTO.getAddress() : existingProperty.getAddress());
        existingProperty.setProvince(updatePropertyRequestDTO.getProvince() != null ? 
                                   updatePropertyRequestDTO.getProvince() : existingProperty.getProvince());
        existingProperty.setDescription(updatePropertyRequestDTO.getDescription() != null ? 
                                      updatePropertyRequestDTO.getDescription() : existingProperty.getDescription());
        existingProperty.setTotalRoom(updatePropertyRequestDTO.getTotalRoom() != null ? 
                                    updatePropertyRequestDTO.getTotalRoom() : existingProperty.getTotalRoom());
        existingProperty.setActiveStatus(updatePropertyRequestDTO.getActiveStatus() != null ? 
                                       updatePropertyRequestDTO.getActiveStatus() : existingProperty.getActiveStatus());
        existingProperty.setUpdatedDate(LocalDateTime.now());
        
        Property updatedProperty = propertyRepository.save(existingProperty);
        return convertToPropertyResponseDTO(updatedProperty, null);
    }
    
    private String getTypePrefix(Integer type) {
        return switch (type) {
            case 1 -> "HOT";
            case 2 -> "VIL";
            case 3 -> "APT";
            default -> "UNK";
        };
    }
    
    @Override
    public List<PropertyResponseDTO> getAllProperties() {
        List<Property> properties = propertyRepository.findAll();
        return properties.stream()
                .map(p -> convertToPropertyResponseDTO(p, null))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PropertyResponseDTO> getPropertiesByOwner(String ownerId) {
        List<Property> properties = propertyRepository.findByOwnerID(UUID.fromString(ownerId));
        return properties.stream()
                .map(p -> convertToPropertyResponseDTO(p, null))
                .collect(Collectors.toList());
    }
    
    @Override
public PropertyResponseDTO getPropertyById(String id) {
    Optional<Property> propertyOpt = propertyRepository.findById(id);
    if (propertyOpt.isEmpty()) {
        return null;
    }
    
    Property property = propertyOpt.get();
    
    // ✅ Fetch room types untuk property ini
    List<RoomTypeResponseDTO> roomTypes = roomTypeRestService.getRoomTypesByProperty(id);
    
    // ✅ Build room type info dengan rooms
    List<RoomTypeInfoDTO> roomTypeInfoList = new ArrayList<>();
    
    for (RoomTypeResponseDTO roomType : roomTypes) {
        // Get rooms untuk room type ini
        List<RoomResponseDTO> rooms = roomRestService.getRoomsByRoomType(roomType.getRoomTypeID());
        
        // Extract room IDs
        List<String> roomIDs = rooms.stream()
                .map(RoomResponseDTO::getRoomID)
                .collect(Collectors.toList());
        
        // Build room type info
        RoomTypeInfoDTO roomTypeInfo = RoomTypeInfoDTO.builder()
                .roomTypeID(roomType.getRoomTypeID())
                .roomTypeName(roomType.getName())
                .floor(roomType.getFloor())
                .capacity(roomType.getCapacity())
                .price(roomType.getPrice())
                .facility(roomType.getFacility())
                .description(roomType.getDescription())
                .roomIDs(roomIDs)
                .build();
        
        roomTypeInfoList.add(roomTypeInfo);
    }
    
    return convertToPropertyResponseDTO(property, roomTypeInfoList);
}
    
    @Override
    public PropertyResponseDTO deleteProperty(String id) {
        Optional<Property> propertyOpt = propertyRepository.findById(id);
        
        if (propertyOpt.isEmpty()) {
            return null;
        }
        
        Property existingProperty = propertyOpt.get();
        existingProperty.setActiveStatus(0);
        existingProperty.setUpdatedDate(LocalDateTime.now());
        
        Property deletedProperty = propertyRepository.save(existingProperty);
        return convertToPropertyResponseDTO(deletedProperty, null);
    }
    
    // ✅ Update convertToPropertyResponseDTO dengan room type info
    private PropertyResponseDTO convertToPropertyResponseDTO(Property property, List<RoomTypeInfoDTO> roomTypeInfoList) {
        String typeName = getTypeName(property.getType());
        String activeStatusName = property.getActiveStatus() == 1 ? "Active" : "Non-Active";
        
        return PropertyResponseDTO.builder()
                .propertyID(property.getPropertyID())
                .propertyName(property.getPropertyName())
                .type(property.getType())
                .typeName(typeName)
                .address(property.getAddress())
                .province(property.getProvince())
                .description(property.getDescription())
                .totalRoom(property.getTotalRoom())
                .activeStatus(property.getActiveStatus())
                .activeStatusName(activeStatusName)
                .income(property.getIncome())
                .ownerName(property.getOwnerName())
                .ownerID(property.getOwnerID().toString())
                .createdDate(property.getCreatedDate())
                .updatedDate(property.getUpdatedDate())
                .roomTypes(roomTypeInfoList) // ✅ Include room type info
                .build();
    }
    
    private String getTypeName(int type) {
        return switch (type) {
            case 1 -> "Hotel";
            case 2 -> "Villa";
            case 3 -> "Apartemen";
            default -> "Unknown";
        };
    }
}