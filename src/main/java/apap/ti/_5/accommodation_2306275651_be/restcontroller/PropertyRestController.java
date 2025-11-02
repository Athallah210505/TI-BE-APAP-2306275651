package apap.ti._5.accommodation_2306275651_be.restcontroller;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import apap.ti._5.accommodation_2306275651_be.restdto.request.property.CreatePropertyRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.property.UpdatePropertyRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.CreateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.UpdateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.roomtype.CreateRoomTypeRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.BaseResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.property.PropertyResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.room.RoomResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.roomtype.RoomTypeResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306275651_be.restservice.RoomRestService;
import apap.ti._5.accommodation_2306275651_be.restservice.RoomTypeRestService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PropertyRestController {
    
    @Autowired
    private PropertyRestService propertyRestService;

    @Autowired
    private RoomTypeRestService roomTypeRestService;
    
    @Autowired
    private RoomRestService roomRestService;

    public static final String BASE_URL = "/property";
    public static final String VIEW_PROPERTY = BASE_URL + "/{id}";
    public static final String CREATE_PROPERTY = BASE_URL + "/create";
    public static final String UPDATE_PROPERTY = BASE_URL + "/update/{id}";
    public static final String DELETE_PROPERTY = BASE_URL + "/delete/{id}";
    public static final String PROPERTY_BY_OWNER = BASE_URL + "/owner/{ownerId}";
    
    @GetMapping(BASE_URL)
    public ResponseEntity<BaseResponseDTO<List<PropertyResponseDTO>>> getAllProperties(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        
        var baseResponseDTO = new BaseResponseDTO<List<PropertyResponseDTO>>();
        
        List<PropertyResponseDTO> listProperty;
        
        if (search != null || type != null || status != null) {
            
            listProperty = propertyRestService.getAllProperties();
        } else {
            listProperty = propertyRestService.getAllProperties();
        }
        
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(listProperty);
        baseResponseDTO.setMessage("Data Property Berhasil Ditemukan");
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }
    
    @GetMapping(VIEW_PROPERTY)
public ResponseEntity<BaseResponseDTO<PropertyResponseDTO>> getProperty(
        @PathVariable String id,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate) {
    
    var baseResponseDTO = new BaseResponseDTO<PropertyResponseDTO>();
    
    try {
        PropertyResponseDTO property = propertyRestService.getPropertyById(id);
        
        if (property == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Property Tidak Ditemukan");

            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        
        // ✅ Jika ada filter tanggal, update availability status
        if (startDate != null && endDate != null) {
            // TODO: Implementasi check booking conflicts untuk update availability
            // Sementara, property sudah include room types & rooms dari service
        }
        
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(property);
        baseResponseDTO.setMessage("Detail Property Berhasil Ditemukan");
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
        
    } catch (Exception ex) {
        baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
    
    @GetMapping(PROPERTY_BY_OWNER)
    public ResponseEntity<BaseResponseDTO<List<PropertyResponseDTO>>> getPropertiesByOwner(
            @PathVariable String ownerId) {
        var baseResponseDTO = new BaseResponseDTO<List<PropertyResponseDTO>>();
        
        List<PropertyResponseDTO> listProperty = propertyRestService.getPropertiesByOwner(ownerId);
        
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(listProperty);
        baseResponseDTO.setMessage("Data Property Owner Berhasil Ditemukan");
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }
    
    @PostMapping(CREATE_PROPERTY)
    public ResponseEntity<BaseResponseDTO<PropertyResponseDTO>> createProperty(
        @Valid @RequestBody CreatePropertyRequestDTO createPropertyRequestDTO,
        BindingResult bindingResult) {
    
    var baseResponseDTO = new BaseResponseDTO<PropertyResponseDTO>();
    
    if (bindingResult.hasFieldErrors()) {
        StringBuilder errorMessages = new StringBuilder();
        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            errorMessages.append(error.getDefaultMessage()).append("; ");
        }
        
        baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
        baseResponseDTO.setMessage(errorMessages.toString());

        return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
    }
    
    try {
        // ✅ Validasi tambahan: minimal 1 room type
        if (createPropertyRequestDTO.getRoomTypes() == null || createPropertyRequestDTO.getRoomTypes().isEmpty()) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("Setiap properti wajib memiliki minimal 1 tipe kamar");
    
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        // ✅ Validasi: setiap room type minimal 1 unit
        for (CreateRoomRequestDTO roomType : createPropertyRequestDTO.getRoomTypes()) {
            if (roomType.getUnit() == null || roomType.getUnit() < 1) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("Setiap tipe kamar wajib memiliki minimal 1 kamar");
        
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
            
            // ✅ Validasi nama tipe kamar sesuai mapping
            if (!isValidRoomTypeName(createPropertyRequestDTO.getType(), roomType.getRoomTypeName())) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("Nama tipe kamar tidak sesuai dengan tipe properti: " + roomType.getRoomTypeName());
        
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
        }
        
        PropertyResponseDTO property = propertyRestService.createProperty(createPropertyRequestDTO);
        
        if (property == null) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("❌ Konfirmasi: Property Gagal Dibuat");
    
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        baseResponseDTO.setStatus(HttpStatus.CREATED.value());
        baseResponseDTO.setData(property);
        baseResponseDTO.setMessage("Konfirmasi: Property '" + property.getPropertyName() + 
                                  "' beserta " + createPropertyRequestDTO.getRoomTypes().size() + 
                                  " tipe kamar berhasil ditambahkan");

        return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);
        
    } catch (Exception ex) {
        baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        baseResponseDTO.setMessage("Konfirmasi: Property gagal dibuat. Error: " + ex.getMessage());

        return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

// ✅ Helper method untuk validasi nama tipe kamar sesuai mapping
private boolean isValidRoomTypeName(Integer propertyType, String roomTypeName) {
    return switch (propertyType) {
        case 1 -> List.of("Single Room", "Double Room", "Deluxe Room", "Superior Room", "Suite", "Family Room")
                .contains(roomTypeName);
        case 2 -> List.of("Luxury", "Beachfront", "Mountside", "Eco-friendly", "Romantic")
                .contains(roomTypeName);
        case 3 -> List.of("Studio", "1BR", "2BR", "3BR", "Penthouse")
                .contains(roomTypeName);
        default -> false;
    };
}
    
    @PutMapping(UPDATE_PROPERTY)
    public ResponseEntity<BaseResponseDTO<PropertyResponseDTO>> updateProperty(
            @PathVariable String id,
            @Valid @RequestBody UpdatePropertyRequestDTO updatePropertyRequestDTO,
            BindingResult bindingResult) {
        
        var baseResponseDTO = new BaseResponseDTO<PropertyResponseDTO>();
        
        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }
            
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        try {
            PropertyResponseDTO property = propertyRestService.updateProperty(id, updatePropertyRequestDTO);
            
            if (property == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Property Tidak Ditemukan");
                    return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(property);
            baseResponseDTO.setMessage("Data Property Berhasil Diupdate");
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping(DELETE_PROPERTY)
    public ResponseEntity<BaseResponseDTO<PropertyResponseDTO>> deleteProperty(@PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<PropertyResponseDTO>();
        
        try {
            // ✅ Cek dulu apakah property ada
            PropertyResponseDTO existingProperty = propertyRestService.getPropertyById(id);
            
            if (existingProperty == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Property Tidak Ditemukan");
          
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }
            
            // ✅ Lakukan soft delete
            PropertyResponseDTO deletedProperty = propertyRestService.deleteProperty(id);
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(deletedProperty);
            baseResponseDTO.setMessage("Property Berhasil Dihapus (Soft Delete)");
         
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
         
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
@GetMapping("/property/updateroom/{idProperty}")
    public ResponseEntity<BaseResponseDTO<PropertyResponseDTO>> getAddRoomTypeForm(
            @PathVariable String idProperty) {
        var baseResponseDTO = new BaseResponseDTO<PropertyResponseDTO>();
        
        PropertyResponseDTO property = propertyRestService.getPropertyById(idProperty);
        
        if (property == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Property Tidak Ditemukan");
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        
        // ✅ Cek apakah property active
        if (property.getActiveStatus() != 1) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("Tidak dapat menambah tipe kamar pada property yang tidak aktif");
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(property);
        baseResponseDTO.setMessage("Form Add Room Type Siap Digunakan");
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping("/property/updateroom/{propertyID}")  // ✅ BENAR - tambahkan {propertyID}
public ResponseEntity<BaseResponseDTO<String>> addRoomTypeWithRooms(
        @PathVariable String propertyID,  // ✅ Sekarang match dengan mapping
        @Valid @RequestBody List<CreateRoomTypeRequestDTO> roomTypesRequest,
        BindingResult bindingResult) {
    
    var baseResponseDTO = new BaseResponseDTO<String>();
    
    if (bindingResult.hasFieldErrors()) {
        StringBuilder errorMessages = new StringBuilder();
        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            errorMessages.append(error.getDefaultMessage()).append("; ");
        }
        
        baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
        baseResponseDTO.setMessage(errorMessages.toString());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
    }
    
    try {
        // ✅ Validasi property exists dan active
        PropertyResponseDTO property = propertyRestService.getPropertyById(propertyID);
        
        if (property == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Property tidak ditemukan");

            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        
        if (property.getActiveStatus() != 1) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("Tidak dapat menambah tipe kamar pada property yang tidak aktif");

            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        // ✅ Validasi minimal 1 room type
        if (roomTypesRequest == null || roomTypesRequest.isEmpty()) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("Minimal harus ada 1 tipe kamar");

            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        // ✅ Validasi duplikasi dalam request
        Set<String> roomTypeCombinations = new HashSet<>();
        for (CreateRoomTypeRequestDTO roomType : roomTypesRequest) {
            // Validasi minimal 1 unit
            if (roomType.getUnitCount() == null || roomType.getUnitCount() < 1) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("Setiap tipe kamar wajib memiliki minimal 1 unit kamar");
    
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
            
            // Validasi nama tipe kamar sesuai dengan tipe property
            if (!isValidRoomTypeName(property.getType(), roomType.getName())) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("Nama tipe kamar tidak sesuai dengan tipe properti: " + roomType.getName());
    
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
            
            // Cek duplikasi dalam request
            String combination = roomType.getName() + "-" + roomType.getFloor();
            if (!roomTypeCombinations.add(combination)) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("Duplikasi kombinasi tipe kamar–lantai dalam form: " + 
                                         roomType.getName() + " di lantai " + roomType.getFloor());
    
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
        }
        
        // ✅ Cek duplikasi dengan existing room types di property
        List<RoomTypeResponseDTO> existingRoomTypes = roomTypeRestService.getRoomTypesByProperty(propertyID);
        
        for (CreateRoomTypeRequestDTO newRoomType : roomTypesRequest) {
            for (RoomTypeResponseDTO existing : existingRoomTypes) {
                if (existing.getName().equals(newRoomType.getName()) && 
                    existing.getFloor().equals(newRoomType.getFloor())) {
                    baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                    baseResponseDTO.setMessage("Duplikasi kombinasi property-tipe kamar-lantai: " + 
                                             newRoomType.getName() + " di lantai " + newRoomType.getFloor() + 
                                             " sudah ada pada property ini");
        
                    return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
                }
            }
        }
        
        // ✅ Semua validasi passed, create room types dan rooms
        int totalRoomTypesCreated = 0;
        int totalRoomsCreated = 0;
        
        for (CreateRoomTypeRequestDTO roomTypeDTO : roomTypesRequest) {
            // ✅ Set propertyID
            roomTypeDTO.setPropertyID(propertyID);
            
            // Create RoomType (gunakan service yang sudah ada)
            RoomTypeResponseDTO createdRoomType = roomTypeRestService.createRoomType(roomTypeDTO);
            totalRoomTypesCreated++;
            
            // ✅ Create Rooms - hitung dari existing rooms di floor yang sama
            List<RoomResponseDTO> existingRoomsOnFloor = roomRestService.getRoomsByPropertyAndFloor(
                propertyID, 
                roomTypeDTO.getFloor()
            );
            
            int startingUnit = existingRoomsOnFloor.size() + 1;
            
            for (int i = 0; i < roomTypeDTO.getUnitCount(); i++) {
                CreateRoomRequestDTO createRoomDTO = CreateRoomRequestDTO.builder()
                        .name(generateRoomNameForAddRoomType(propertyID, roomTypeDTO.getFloor(), startingUnit + i))
                        .roomTypeID(createdRoomType.getRoomTypeID())
                        .availabilityStatus(1)
                        .activeRoom(1)
                        .build();
                
                roomRestService.createRoom(createRoomDTO);
                totalRoomsCreated++;
            }
        }
        
        // ✅ Update total room di property
        int newTotalRoom = property.getTotalRoom() + totalRoomsCreated;
        UpdatePropertyRequestDTO updatePropertyDTO = UpdatePropertyRequestDTO.builder()
                .propertyName(property.getPropertyName())
                .type(property.getType())
                .address(property.getAddress())
                .province(property.getProvince())
                .description(property.getDescription())
                .totalRoom(newTotalRoom)
                .activeStatus(property.getActiveStatus())
                .build();
        propertyRestService.updateProperty(propertyID, updatePropertyDTO);
        
        baseResponseDTO.setStatus(HttpStatus.CREATED.value());
        baseResponseDTO.setData("Success");
        baseResponseDTO.setMessage("✅ Konfirmasi: " + totalRoomTypesCreated + " tipe kamar dan " + 
                                  totalRoomsCreated + " unit kamar berhasil ditambahkan pada property " + 
                                  property.getPropertyName());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);
        
    } catch (Exception ex) {
        baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        baseResponseDTO.setMessage("❌ Konfirmasi: Gagal menambah tipe kamar. Error: " + ex.getMessage());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

private String generateRoomNameForAddRoomType(String propertyID, Integer floor, Integer unitNumber) {
    String floorUnit = String.format("%d%02d", floor, unitNumber);
    return propertyID + "-" + floorUnit;
}

@PostMapping("/property/maintenance/add")
public ResponseEntity<BaseResponseDTO<RoomResponseDTO>> addMaintenanceSchedule(
        @Valid @RequestBody UpdateRoomRequestDTO updateRoomRequest,
        BindingResult bindingResult) {
    
    var baseResponseDTO = new BaseResponseDTO<RoomResponseDTO>();
    
    if (bindingResult.hasFieldErrors()) {
        StringBuilder errorMessages = new StringBuilder();
        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            errorMessages.append(error.getDefaultMessage()).append("; ");
        }
        
        baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
        baseResponseDTO.setMessage(errorMessages.toString());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
    }
    
    try {
        // ✅ Validasi: Tanggal selesai tidak boleh lebih awal dari tanggal mulai
        if (updateRoomRequest.getMaintenanceEnd() != null && 
            updateRoomRequest.getMaintenanceStart() != null) {
            
            if (updateRoomRequest.getMaintenanceEnd().isBefore(updateRoomRequest.getMaintenanceStart())) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("❌ Konfirmasi: Tanggal selesai perbaikan tidak boleh lebih awal dari tanggal mulai");
    
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
            
            // ✅ Validasi: Tanggal yang sama, waktu selesai tidak boleh lebih awal
            if (updateRoomRequest.getMaintenanceEnd().toLocalDate()
                    .equals(updateRoomRequest.getMaintenanceStart().toLocalDate())) {
                if (updateRoomRequest.getMaintenanceEnd().toLocalTime()
                        .isBefore(updateRoomRequest.getMaintenanceStart().toLocalTime())) {
                    baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                    baseResponseDTO.setMessage("❌ Konfirmasi: Pada hari yang sama, waktu selesai tidak boleh lebih awal dari waktu mulai");
        
                    return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
                }
            }
        }
        
        // ✅ Get room untuk ambil roomID (dari name di request)
        RoomResponseDTO existingRoom = roomRestService.getRoomById(updateRoomRequest.getName());
        
        if (existingRoom == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("❌ Konfirmasi: Kamar tidak ditemukan");

            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        
        // ✅ Validasi: Cek apakah ada booking conflict (TODO: implement setelah ada Booking model)
        boolean hasConflict = roomRestService.hasBookingConflict(
            existingRoom.getRoomID(), 
            updateRoomRequest.getMaintenanceStart().toString(), 
            updateRoomRequest.getMaintenanceEnd().toString()
        );
        if (hasConflict) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("❌ Konfirmasi: Tidak dapat menjadwalkan perbaikan. Sudah ada booking pada tanggal tersebut");
    
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        // ✅ Set availability status menjadi unavailable (0) selama maintenance
        updateRoomRequest.setAvailabilityStatus(0);
        
        // ✅ Update room dengan maintenance schedule (replace yang lama)
        RoomResponseDTO updatedRoom = roomRestService.updateRoom(
            existingRoom.getRoomID(), 
            updateRoomRequest
        );
        
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(updatedRoom);
        baseResponseDTO.setMessage(" Konfirmasi: Jadwal perbaikan untuk kamar " + 
                                  updatedRoom.getName() + " berhasil ditambahkan");
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
        
    } catch (RuntimeException ex) {
        baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
        baseResponseDTO.setMessage(" Konfirmasi: Gagal menambah jadwal perbaikan. " + ex.getMessage());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        
    } catch (Exception ex) {
        baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

}

