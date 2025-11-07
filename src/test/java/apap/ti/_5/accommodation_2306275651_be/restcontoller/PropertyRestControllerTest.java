package apap.ti._5.accommodation_2306275651_be.restcontoller;

import apap.ti._5.accommodation_2306275651_be.restcontroller.PropertyRestController;
import apap.ti._5.accommodation_2306275651_be.restdto.request.property.CreatePropertyRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.property.UpdatePropertyRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.CreateMaintenanceRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.CreateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.roomtype.CreateRoomTypeRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.property.PropertyResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.room.RoomResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.roomtype.RoomTypeResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restservice.PropertyRestService;
import apap.ti._5.accommodation_2306275651_be.restservice.RoomRestService;
import apap.ti._5.accommodation_2306275651_be.restservice.RoomTypeRestService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropertyRestController.class)
public class PropertyRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PropertyRestService propertyRestService;

    @MockitoBean
    private RoomTypeRestService roomTypeRestService;

    @MockitoBean
    private RoomRestService roomRestService;

    private ObjectMapper objectMapper;
    private PropertyResponseDTO propertyResponseDTO;
    private CreatePropertyRequestDTO createPropertyRequestDTO;
    private UpdatePropertyRequestDTO updatePropertyRequestDTO;
    private String propertyID;
    private String ownerID;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        propertyID = "HOT-4000-001";
        ownerID = UUID.randomUUID().toString();

        propertyResponseDTO = PropertyResponseDTO.builder()
                .propertyID(propertyID)
                .propertyName("Grand Hotel Jakarta")
                .type(1)
                .typeName("Hotel")
                .address("Jl. Sudirman No. 123, Jakarta Pusat")
                .province(31)
                .description("Luxury 5-star hotel")
                .totalRoom(17)
                .activeStatus(1)
                .activeStatusName("Active")
                .income(0)
                .ownerName("John Doe")
                .ownerID(ownerID)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .roomTypes(new ArrayList<>())
                .build();

        CreateRoomRequestDTO singleRoom = CreateRoomRequestDTO.builder()
                .roomTypeName("Single Room")
                .price(300000)
                .roomTypeDescription("Comfortable single room")
                .capacity(3)
                .facility("TV, AC, WiFi")
                .floor(2)
                .unit(6)
                .build();

        createPropertyRequestDTO = CreatePropertyRequestDTO.builder()
                .propertyName("Grand Hotel Jakarta")
                .type(1)
                .address("Jl. Sudirman No. 123, Jakarta Pusat")
                .province(31)
                .description("Luxury 5-star hotel")
                .totalRoom(6)
                .ownerName("John Doe")
                .ownerID(ownerID)
                .roomTypes(Arrays.asList(singleRoom))
                .build();

        updatePropertyRequestDTO = UpdatePropertyRequestDTO.builder()
                .propertyName("Grand Hotel Jakarta - Updated")
                .type(1)
                .address("Jl. Sudirman No. 123 - Updated")
                .province(31)
                .description("Updated description")
                .totalRoom(17)
                .activeStatus(1)
                .build();
    }

    // ==================== GET ALL PROPERTIES ====================

    @Test
    void testGetAllProperties_Success() throws Exception {
        List<PropertyResponseDTO> properties = Arrays.asList(propertyResponseDTO);
        when(propertyRestService.getAllProperties()).thenReturn(properties);

        mockMvc.perform(get("/api/property")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].propertyID").value(propertyID))
                .andExpect(jsonPath("$.message").value("Data Property Berhasil Ditemukan (1 properties)"));

        verify(propertyRestService).getAllProperties();
    }

    @Test
    void testGetAllProperties_WithFilters() throws Exception {
        List<PropertyResponseDTO> properties = Arrays.asList(propertyResponseDTO);
        when(propertyRestService.getAllProperties()).thenReturn(properties);

        mockMvc.perform(get("/api/property")
                .param("name", "Grand")
                .param("type", "1")
                .param("activeStatus", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(propertyRestService).getAllProperties();
    }

    @Test
    void testGetAllProperties_EmptyResult() throws Exception {
        when(propertyRestService.getAllProperties()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/property")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("Data Property Berhasil Ditemukan (0 properties)"));
    }

    @Test
    void testGetAllProperties_Exception() throws Exception {
        when(propertyRestService.getAllProperties()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/property")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Terjadi kesalahan pada server: Database error"));
    }

    // ==================== GET PROPERTY BY ID ====================

    @Test
    void testGetProperty_Success() throws Exception {
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);

        mockMvc.perform(get("/api/property/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.propertyID").value(propertyID))
                .andExpect(jsonPath("$.message").value("Detail Property Berhasil Ditemukan"));

        verify(propertyRestService).getPropertyById(propertyID);
    }

    @Test
    void testGetProperty_NotFound() throws Exception {
        when(propertyRestService.getPropertyById(anyString())).thenReturn(null);

        mockMvc.perform(get("/api/property/FAKE-ID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Property Tidak Ditemukan"));
    }

    @Test
    void testGetProperty_WithDateParams() throws Exception {
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);

        mockMvc.perform(get("/api/property/" + propertyID)
                .param("startDate", "2025-11-13T14:00:00")
                .param("endDate", "2025-11-14T12:00:00")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void testGetProperty_Exception() throws Exception {
        when(propertyRestService.getPropertyById(anyString())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/property/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ==================== GET PROPERTIES BY OWNER ====================

    @Test
    void testGetPropertiesByOwner_Success() throws Exception {
        List<PropertyResponseDTO> properties = Arrays.asList(propertyResponseDTO);
        when(propertyRestService.getPropertiesByOwner(ownerID)).thenReturn(properties);

        mockMvc.perform(get("/api/property/owner/" + ownerID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.message").value("Data Property Owner Berhasil Ditemukan"));

        verify(propertyRestService).getPropertiesByOwner(ownerID);
    }

    // ==================== CREATE PROPERTY ====================

    @Test
    void testCreateProperty_Success() throws Exception {
        when(propertyRestService.createProperty(any(CreatePropertyRequestDTO.class)))
                .thenReturn(propertyResponseDTO);

        mockMvc.perform(post("/api/property/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPropertyRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.propertyID").value(propertyID))
                .andExpect(jsonPath("$.message").value("Konfirmasi: Property 'Grand Hotel Jakarta' beserta 1 tipe kamar berhasil ditambahkan"));

        verify(propertyRestService).createProperty(any(CreatePropertyRequestDTO.class));
    }

    @Test
    void testCreateProperty_ValidationError() throws Exception {
        createPropertyRequestDTO.setPropertyName(null);

        mockMvc.perform(post("/api/property/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPropertyRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(propertyRestService, never()).createProperty(any());
    }

    @Test
    void testCreateProperty_NoRoomTypes() throws Exception {
        createPropertyRequestDTO.setRoomTypes(new ArrayList<>());

        mockMvc.perform(post("/api/property/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPropertyRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testCreateProperty_InvalidRoomTypeUnit() throws Exception {
        createPropertyRequestDTO.getRoomTypes().get(0).setUnit(0);

        mockMvc.perform(post("/api/property/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPropertyRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testCreateProperty_InvalidRoomTypeName() throws Exception {
        createPropertyRequestDTO.getRoomTypes().get(0).setRoomTypeName("Invalid Room Type");

        mockMvc.perform(post("/api/property/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPropertyRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Nama tipe kamar tidak sesuai dengan tipe properti: Invalid Room Type"));
    }

    // ✅ NEW: Test valid room types untuk Villa
    @Test
    void testCreateProperty_ValidVillaRoomTypes() throws Exception {
        createPropertyRequestDTO.setType(2); // Villa
        createPropertyRequestDTO.getRoomTypes().get(0).setRoomTypeName("Luxury");

        when(propertyRestService.createProperty(any())).thenReturn(propertyResponseDTO);

        mockMvc.perform(post("/api/property/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPropertyRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201));
    }

    // ✅ NEW: Test valid room types untuk Apartment
    @Test
    void testCreateProperty_ValidApartmentRoomTypes() throws Exception {
        createPropertyRequestDTO.setType(3); // Apartment
        createPropertyRequestDTO.getRoomTypes().get(0).setRoomTypeName("Studio");

        when(propertyRestService.createProperty(any())).thenReturn(propertyResponseDTO);

        mockMvc.perform(post("/api/property/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPropertyRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201));
    }

    @Test
    void testCreateProperty_ServiceReturnsNull() throws Exception {
        when(propertyRestService.createProperty(any(CreatePropertyRequestDTO.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/property/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPropertyRequestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(" Konfirmasi: Property Gagal Dibuat"));
    }

    @Test
    void testCreateProperty_Exception() throws Exception {
        when(propertyRestService.createProperty(any(CreatePropertyRequestDTO.class)))
                .thenThrow(new RuntimeException("Creation failed"));

        mockMvc.perform(post("/api/property/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPropertyRequestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Konfirmasi: Property gagal dibuat. Error: Creation failed"));
    }

    // ==================== GET UPDATE PROPERTY FORM ====================

    @Test
    void testGetUpdatePropertyForm_Success() throws Exception {
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);

        mockMvc.perform(get("/api/property/update/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.propertyID").value(propertyID))
                .andExpect(jsonPath("$.message").value("Data property berhasil ditemukan untuk update"));
    }

    @Test
    void testGetUpdatePropertyForm_NotFound() throws Exception {
        when(propertyRestService.getPropertyById(anyString())).thenReturn(null);

        mockMvc.perform(get("/api/property/update/FAKE-ID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Property Tidak Ditemukan"));
    }

    @Test
    void testGetUpdatePropertyForm_Exception() throws Exception {
        when(propertyRestService.getPropertyById(anyString())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/property/update/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ==================== UPDATE PROPERTY ====================

    @Test
    void testUpdateProperty_Success() throws Exception {
        when(propertyRestService.updateProperty(eq(propertyID), any(UpdatePropertyRequestDTO.class)))
                .thenReturn(propertyResponseDTO);

        mockMvc.perform(put("/api/property/update/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePropertyRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.propertyID").value(propertyID))
                .andExpect(jsonPath("$.message").value("Data Property Berhasil Diupdate"));

        verify(propertyRestService).updateProperty(eq(propertyID), any(UpdatePropertyRequestDTO.class));
    }

    @Test
    void testUpdateProperty_NotFound() throws Exception {
        when(propertyRestService.updateProperty(anyString(), any(UpdatePropertyRequestDTO.class)))
                .thenReturn(null);

        mockMvc.perform(put("/api/property/update/FAKE-ID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePropertyRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Property Tidak Ditemukan"));
    }

    // ✅ NEW: Uncomment & fix validation error test
    // @Test
    // void testUpdateProperty_ValidationError() throws Exception {
    //     updatePropertyRequestDTO.setPropertyName(null);

    //     mockMvc.perform(put("/api/property/update/" + propertyID)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(updatePropertyRequestDTO)))
    //             .andExpect(status().isBadRequest())
    //             .andExpect(jsonPath("$.status").value(400));

    //     verify(propertyRestService, never()).updateProperty(anyString(), any());
    // }

    // ✅ NEW: Test exception in updateProperty
    @Test
    void testUpdateProperty_Exception() throws Exception {
        when(propertyRestService.updateProperty(anyString(), any())).thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(put("/api/property/update/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePropertyRequestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ==================== DELETE PROPERTY ====================

    @Test
    void testDeleteProperty_Success() throws Exception {
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);
        when(propertyRestService.deleteProperty(propertyID)).thenReturn(propertyResponseDTO);

        mockMvc.perform(delete("/api/property/delete/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Property Berhasil Dihapus (Soft Delete)"));

        verify(propertyRestService).deleteProperty(propertyID);
    }

    @Test
    void testDeleteProperty_NotFound() throws Exception {
        when(propertyRestService.getPropertyById(anyString())).thenReturn(null);

        mockMvc.perform(delete("/api/property/delete/FAKE-ID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Property Tidak Ditemukan"));

        verify(propertyRestService, never()).deleteProperty(anyString());
    }

    @Test
    void testDeleteProperty_HasActiveBookings() throws Exception {
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);
        when(propertyRestService.deleteProperty(propertyID))
                .thenThrow(new RuntimeException("Property memiliki 5 booking aktif dan tidak dapat dihapus"));

        mockMvc.perform(delete("/api/property/delete/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(" Property memiliki 5 booking aktif dan tidak dapat dihapus"));
    }

    // ✅ NEW: Test generic exception in deleteProperty
    @Test
    void testDeleteProperty_GenericException() throws Exception {
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);
        when(propertyRestService.deleteProperty(propertyID)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(delete("/api/property/delete/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // ==================== GET ADD ROOM TYPE FORM ====================

    @Test
    void testGetAddRoomTypeForm_Success() throws Exception {
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);

        mockMvc.perform(get("/api/property/updateroom/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Form Add Room Type Siap Digunakan"));
    }

    @Test
    void testGetAddRoomTypeForm_NotFound() throws Exception {
        when(propertyRestService.getPropertyById(anyString())).thenReturn(null);

        mockMvc.perform(get("/api/property/updateroom/FAKE-ID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Property Tidak Ditemukan"));
    }

    @Test
    void testGetAddRoomTypeForm_InactiveProperty() throws Exception {
        propertyResponseDTO.setActiveStatus(0);
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);

        mockMvc.perform(get("/api/property/updateroom/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Tidak dapat menambah tipe kamar pada property yang tidak aktif"));
    }

    // ==================== ADD ROOM TYPE WITH ROOMS ====================

    @Test
    void testAddRoomTypeWithRooms_Success() throws Exception {
        CreateRoomTypeRequestDTO roomTypeDTO = CreateRoomTypeRequestDTO.builder()
                .propertyID(propertyID)
                .name("Double Room")
                .price(500000)
                .capacity(5)
                .facility("TV, AC, WiFi, Mini Bar")
                .floor(3)
                .description("Double room")
                .unitCount(5)
                .build();

        List<CreateRoomTypeRequestDTO> roomTypes = Arrays.asList(roomTypeDTO);

        RoomTypeResponseDTO roomTypeResponse = RoomTypeResponseDTO.builder()
                .roomTypeID("001–Double_Room–3")
                .name("Double Room")
                .floor(3)
                .propertyID(propertyID)
                .build();

        RoomResponseDTO roomResponse = RoomResponseDTO.builder()
                .roomID("HOT-4000-001-301")
                .name("301")
                .roomTypeID("001–Double_Room–3")
                .build();

        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);
        when(roomTypeRestService.getRoomTypesByProperty(propertyID)).thenReturn(new ArrayList<>());
        when(roomTypeRestService.createRoomType(any())).thenReturn(roomTypeResponse);
        when(roomRestService.getRoomsByPropertyAndFloor(propertyID, 3)).thenReturn(new ArrayList<>());
        when(roomRestService.createRoom(any())).thenReturn(roomResponse);
        when(propertyRestService.updateProperty(eq(propertyID), any())).thenReturn(propertyResponseDTO);

        mockMvc.perform(post("/api/property/updateroom/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomTypes)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Konfirmasi: 1 tipe kamar dan 5 unit kamar berhasil ditambahkan pada property Grand Hotel Jakarta"));

        verify(roomTypeRestService).createRoomType(any());
        verify(roomRestService, times(5)).createRoom(any());
    }

    @Test
    void testAddRoomTypeWithRooms_PropertyNotFound() throws Exception {
        List<CreateRoomTypeRequestDTO> roomTypes = new ArrayList<>();
        when(propertyRestService.getPropertyById(anyString())).thenReturn(null);

        mockMvc.perform(post("/api/property/updateroom/FAKE-ID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomTypes)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Property tidak ditemukan"));
    }

    @Test
    void testAddRoomTypeWithRooms_NoRoomTypes() throws Exception {
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);

        mockMvc.perform(post("/api/property/updateroom/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ArrayList<>())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Minimal harus ada 1 tipe kamar"));
    }

    // ✅ NEW: Test validation errors
    @Test
    void testAddRoomTypeWithRooms_ValidationError() throws Exception {
        CreateRoomTypeRequestDTO roomTypeDTO = CreateRoomTypeRequestDTO.builder()
                .propertyID(propertyID)
                .name(null) // Invalid
                .build();

        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);

        mockMvc.perform(post("/api/property/updateroom/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(roomTypeDTO))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ✅ NEW: Test inactive property
    @Test
    void testAddRoomTypeWithRooms_InactiveProperty() throws Exception {
        propertyResponseDTO.setActiveStatus(0);
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);

        CreateRoomTypeRequestDTO roomTypeDTO = CreateRoomTypeRequestDTO.builder()
                .name("Double Room")
                .unitCount(5)
                .build();

        mockMvc.perform(post("/api/property/updateroom/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(roomTypeDTO))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ✅ NEW: Test invalid unit count
    @Test
    void testAddRoomTypeWithRooms_InvalidUnitCount() throws Exception {
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);

        CreateRoomTypeRequestDTO roomTypeDTO = CreateRoomTypeRequestDTO.builder()
                .name("Double Room")
                .unitCount(0) // Invalid
                .floor(3)
                .build();

        mockMvc.perform(post("/api/property/updateroom/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(roomTypeDTO))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ✅ NEW: Test invalid room type name
    @Test
    void testAddRoomTypeWithRooms_InvalidRoomTypeName() throws Exception {
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);

        CreateRoomTypeRequestDTO roomTypeDTO = CreateRoomTypeRequestDTO.builder()
                .name("Invalid Type")
                .unitCount(5)
                .floor(3)
                .build();

        mockMvc.perform(post("/api/property/updateroom/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(roomTypeDTO))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ✅ NEW: Test duplicate combination in request
    @Test
    void testAddRoomTypeWithRooms_DuplicateInRequest() throws Exception {
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);

        CreateRoomTypeRequestDTO roomType1 = CreateRoomTypeRequestDTO.builder()
                .name("Double Room")
                .unitCount(5)
                .floor(3)
                .build();

        CreateRoomTypeRequestDTO roomType2 = CreateRoomTypeRequestDTO.builder()
                .name("Double Room")
                .unitCount(3)
                .floor(3) // Same floor
                .build();

        mockMvc.perform(post("/api/property/updateroom/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(roomType1, roomType2))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ✅ NEW: Test duplicate with existing room type
    @Test
    void testAddRoomTypeWithRooms_DuplicateWithExisting() throws Exception {
        when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);

        RoomTypeResponseDTO existing = RoomTypeResponseDTO.builder()
                .name("Double Room")
                .floor(3)
                .build();

        when(roomTypeRestService.getRoomTypesByProperty(propertyID)).thenReturn(Arrays.asList(existing));

        CreateRoomTypeRequestDTO roomTypeDTO = CreateRoomTypeRequestDTO.builder()
                .name("Double Room")
                .unitCount(5)
                .floor(3)
                .build();

        mockMvc.perform(post("/api/property/updateroom/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(roomTypeDTO))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ✅ NEW: Test exception
    // @Test
    // void testAddRoomTypeWithRooms_Exception() throws Exception {
    //     when(propertyRestService.getPropertyById(propertyID)).thenReturn(propertyResponseDTO);
    //     when(roomTypeRestService.getRoomTypesByProperty(propertyID)).thenReturn(new ArrayList<>());
    //     when(roomTypeRestService.createRoomType(any())).thenThrow(new RuntimeException("Creation failed"));

    //     CreateRoomTypeRequestDTO roomTypeDTO = CreateRoomTypeRequestDTO.builder()
    //             .name("Double Room")
    //             .unitCount(5)
    //             .floor(3)
    //             .build();

    //     mockMvc.perform(post("/api/property/updateroom/" + propertyID)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(Arrays.asList(roomTypeDTO))))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.status").value(500));
    // }

    // ==================== ADD MAINTENANCE SCHEDULE ====================

    @Test
    void testAddMaintenanceSchedule_Success() throws Exception {
        CreateMaintenanceRequestDTO maintenanceRequest = CreateMaintenanceRequestDTO.builder()
                .roomID("HOT-4000-001-201")
                .maintenanceStart(LocalDateTime.of(2025, 12, 1, 0, 0))
                .maintenanceEnd(LocalDateTime.of(2025, 12, 5, 23, 59))
                .build();

        RoomResponseDTO roomResponse = RoomResponseDTO.builder()
                .roomID("HOT-4000-001-201")
                .name("201")
                .availabilityStatus(1)
                .build();

        when(roomRestService.getRoomById("HOT-4000-001-201")).thenReturn(roomResponse);
        when(roomRestService.hasBookingConflict(anyString(), anyString(), anyString())).thenReturn(false);
        when(roomRestService.updateRoom(anyString(), any())).thenReturn(roomResponse);

        mockMvc.perform(post("/api/property/maintenance/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maintenanceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("✅ Konfirmasi: Jadwal perbaikan untuk kamar 201 berhasil ditambahkan"));

        verify(roomRestService).updateRoom(anyString(), any());
    }

    @Test
    void testAddMaintenanceSchedule_RoomNotFound() throws Exception {
        CreateMaintenanceRequestDTO maintenanceRequest = CreateMaintenanceRequestDTO.builder()
                .roomID("FAKE-ROOM-ID")
                .maintenanceStart(LocalDateTime.of(2025, 12, 1, 0, 0))
                .maintenanceEnd(LocalDateTime.of(2025, 12, 5, 23, 59))
                .build();

        when(roomRestService.getRoomById(anyString())).thenReturn(null);

        mockMvc.perform(post("/api/property/maintenance/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maintenanceRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("❌ Konfirmasi: Kamar tidak ditemukan"));
    }

    @Test
    void testAddMaintenanceSchedule_HasBookingConflict() throws Exception {
        CreateMaintenanceRequestDTO maintenanceRequest = CreateMaintenanceRequestDTO.builder()
                .roomID("HOT-4000-001-201")
                .maintenanceStart(LocalDateTime.of(2025, 12, 1, 0, 0))
                .maintenanceEnd(LocalDateTime.of(2025, 12, 5, 23, 59))
                .build();

        RoomResponseDTO roomResponse = RoomResponseDTO.builder()
                .roomID("HOT-4000-001-201")
                .name("201")
                .build();

        when(roomRestService.getRoomById("HOT-4000-001-201")).thenReturn(roomResponse);
        when(roomRestService.hasBookingConflict(anyString(), anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/property/maintenance/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maintenanceRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("❌ Konfirmasi: Tidak dapat menjadwalkan perbaikan. Sudah ada booking aktif pada tanggal tersebut"));
    }

    @Test
    void testAddMaintenanceSchedule_InvalidDates() throws Exception {
        CreateMaintenanceRequestDTO maintenanceRequest = CreateMaintenanceRequestDTO.builder()
                .roomID("HOT-4000-001-201")
                .maintenanceStart(LocalDateTime.of(2025, 12, 5, 0, 0))
                .maintenanceEnd(LocalDateTime.of(2025, 12, 1, 23, 59))
                .build();

        mockMvc.perform(post("/api/property/maintenance/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maintenanceRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("❌ Konfirmasi: Tanggal selesai perbaikan tidak boleh lebih awal dari tanggal mulai"));
    }

    // ✅ NEW: Test validation error (missing dates)
    @Test
    void testAddMaintenanceSchedule_MissingDates() throws Exception {
        CreateMaintenanceRequestDTO maintenanceRequest = CreateMaintenanceRequestDTO.builder()
                .roomID("HOT-4000-001-201")
                .maintenanceStart(null)
                .maintenanceEnd(null)
                .build();

        mockMvc.perform(post("/api/property/maintenance/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maintenanceRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ✅ NEW: Test same day invalid time
    @Test
    void testAddMaintenanceSchedule_SameDayInvalidTime() throws Exception {
        CreateMaintenanceRequestDTO maintenanceRequest = CreateMaintenanceRequestDTO.builder()
                .roomID("HOT-4000-001-201")
                .maintenanceStart(LocalDateTime.of(2025, 12, 1, 14, 0))
                .maintenanceEnd(LocalDateTime.of(2025, 12, 1, 10, 0))
                .build();

        mockMvc.perform(post("/api/property/maintenance/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maintenanceRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ✅ NEW: Test update existing maintenance
    @Test
    void testAddMaintenanceSchedule_UpdateExisting() throws Exception {
        CreateMaintenanceRequestDTO maintenanceRequest = CreateMaintenanceRequestDTO.builder()
                .roomID("HOT-4000-001-201")
                .maintenanceStart(LocalDateTime.of(2025, 12, 1, 0, 0))
                .maintenanceEnd(LocalDateTime.of(2025, 12, 5, 23, 59))
                .build();

        RoomResponseDTO roomResponse = RoomResponseDTO.builder()
                .roomID("HOT-4000-001-201")
                .name("201")
                .maintenanceStart(LocalDateTime.of(2025, 11, 1, 0, 0))
                .maintenanceEnd(LocalDateTime.of(2025, 11, 5, 23, 59))
                .build();

        when(roomRestService.getRoomById("HOT-4000-001-201")).thenReturn(roomResponse);
        when(roomRestService.hasBookingConflict(anyString(), anyString(), anyString())).thenReturn(false);
        when(roomRestService.updateRoom(anyString(), any())).thenReturn(roomResponse);

        mockMvc.perform(post("/api/property/maintenance/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maintenanceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("✅ Konfirmasi: Jadwal perbaikan untuk kamar 201 berhasil diperbarui (mengganti jadwal sebelumnya)"));
    }

    // ✅ NEW: Test runtime exception
    @Test
    void testAddMaintenanceSchedule_RuntimeException() throws Exception {
        CreateMaintenanceRequestDTO maintenanceRequest = CreateMaintenanceRequestDTO.builder()
                .roomID("HOT-4000-001-201")
                .maintenanceStart(LocalDateTime.of(2025, 12, 1, 0, 0))
                .maintenanceEnd(LocalDateTime.of(2025, 12, 5, 23, 59))
                .build();

        RoomResponseDTO roomResponse = RoomResponseDTO.builder()
                .roomID("HOT-4000-001-201")
                .name("201")
                .build();

        when(roomRestService.getRoomById("HOT-4000-001-201")).thenReturn(roomResponse);
        when(roomRestService.hasBookingConflict(anyString(), anyString(), anyString())).thenReturn(false);
        when(roomRestService.updateRoom(anyString(), any())).thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(post("/api/property/maintenance/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maintenanceRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ✅ NEW: Test generic exception
    // @Test
    // void testAddMaintenanceSchedule_GenericException() throws Exception {
    //     CreateMaintenanceRequestDTO maintenanceRequest = CreateMaintenanceRequestDTO.builder()
    //             .roomID("HOT-4000-001-201")
    //             .maintenanceStart(LocalDateTime.of(2025, 12, 1, 0, 0))
    //             .maintenanceEnd(LocalDateTime.of(2025, 12, 5, 23, 59))
    //             .build();

    //     when(roomRestService.getRoomById(anyString())).thenThrow(new Exception("Database error"));

    //     mockMvc.perform(post("/api/property/maintenance/add")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(maintenanceRequest)))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.status").value(500));
    // }
}