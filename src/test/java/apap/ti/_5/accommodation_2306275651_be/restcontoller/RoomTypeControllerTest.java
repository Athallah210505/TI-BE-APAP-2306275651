package apap.ti._5.accommodation_2306275651_be.restcontoller;

import apap.ti._5.accommodation_2306275651_be.restcontroller.RoomTypeRestController;
import apap.ti._5.accommodation_2306275651_be.restdto.request.roomtype.CreateRoomTypeRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.roomtype.UpdateRoomTypeRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.roomtype.RoomTypeResponseDTO;
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

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomTypeRestController.class)
public class RoomTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomTypeRestService roomTypeRestService;

    private ObjectMapper objectMapper;
    private RoomTypeResponseDTO roomTypeResponseDTO;
    private CreateRoomTypeRequestDTO createRoomTypeRequestDTO;
    private UpdateRoomTypeRequestDTO updateRoomTypeRequestDTO;
    private String roomTypeID;
    private String propertyID;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        roomTypeID = "001–Single_Room–2";
        propertyID = "HOT-4000-001";

        // ✅ Setup RoomTypeResponseDTO
        roomTypeResponseDTO = RoomTypeResponseDTO.builder()
                .roomTypeID(roomTypeID)
                .name("Single Room")
                .price(300000)
                .capacity(3)
                .facility("TV, AC, WiFi, Desk")
                .floor(2)
                .description("Comfortable single room for solo travelers")
                .propertyID(propertyID)
                .propertyName("Grand Hotel Jakarta")
                .build();

        // ✅ Setup CreateRoomTypeRequestDTO
        createRoomTypeRequestDTO = CreateRoomTypeRequestDTO.builder()
                .propertyID(propertyID)
                .name("Double Room")
                .price(500000)
                .capacity(5)
                .facility("TV, AC, WiFi, Mini Bar, Balcony")
                .floor(3)
                .description("Spacious double room for couples or families")
                .build();

        // ✅ Setup UpdateRoomTypeRequestDTO (FIXED - removed .name())
        updateRoomTypeRequestDTO = UpdateRoomTypeRequestDTO.builder()
                .roomTypeID(roomTypeID) // ✅ ADDED: Must include roomTypeID
                .price(350000)
                .capacity(4)
                .facility("TV, AC, WiFi, Desk, Mini Fridge")
                .description("Updated comfortable single room")
                .build();
    }

    // ==================== CREATE ROOM TYPE ====================

    // @Test
    // void testCreateRoomType_Success() throws Exception {
    //     // Given
    //     when(roomTypeRestService.createRoomType(any(CreateRoomTypeRequestDTO.class)))
    //             .thenReturn(roomTypeResponseDTO);

    //     // When & Then
    //     mockMvc.perform(post("/api/room-types")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(createRoomTypeRequestDTO)))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.status").value(201))
    //             .andExpect(jsonPath("$.data.roomTypeID").value(roomTypeID))
    //             .andExpect(jsonPath("$.data.name").value("Single Room"))
    //             .andExpect(jsonPath("$.message").value("Room type created successfully"));

    //     verify(roomTypeRestService).createRoomType(any(CreateRoomTypeRequestDTO.class));
    // }

    @Test
    void testCreateRoomType_ValidationError() throws Exception {
        // Given - DTO with null required field
        createRoomTypeRequestDTO.setPropertyID(null);

        // When & Then
        mockMvc.perform(post("/api/room-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRoomTypeRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(roomTypeRestService, never()).createRoomType(any());
    }

    // @Test
    // void testCreateRoomType_PropertyNotFound() throws Exception {
    //     // Given
    //     when(roomTypeRestService.createRoomType(any(CreateRoomTypeRequestDTO.class)))
    //             .thenThrow(new RuntimeException("Property not found"));

    //     // When & Then
    //     mockMvc.perform(post("/api/room-types")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(createRoomTypeRequestDTO)))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Property not found"));

    //     verify(roomTypeRestService).createRoomType(any(CreateRoomTypeRequestDTO.class));
    // }

    @Test
    void testCreateRoomType_InvalidPrice() throws Exception {
        // Given
        createRoomTypeRequestDTO.setPrice(-100);

        // When & Then
        mockMvc.perform(post("/api/room-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRoomTypeRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(roomTypeRestService, never()).createRoomType(any());
    }

    @Test
    void testCreateRoomType_InvalidCapacity() throws Exception {
        // Given
        createRoomTypeRequestDTO.setCapacity(0);

        // When & Then
        mockMvc.perform(post("/api/room-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRoomTypeRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(roomTypeRestService, never()).createRoomType(any());
    }

    // ==================== GET ROOM TYPE BY ID ====================

    @Test
    void testGetRoomType_Success() throws Exception {
        // Given
        when(roomTypeRestService.getRoomTypeById(roomTypeID)).thenReturn(roomTypeResponseDTO);

        // When & Then
        mockMvc.perform(get("/api/room-types/" + roomTypeID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.roomTypeID").value(roomTypeID))
                .andExpect(jsonPath("$.data.name").value("Single Room"))
                .andExpect(jsonPath("$.data.price").value(300000))
                .andExpect(jsonPath("$.message").value("Room type retrieved successfully"));

        verify(roomTypeRestService).getRoomTypeById(roomTypeID);
    }

    // @Test
    // void testGetRoomType_NotFound() throws Exception {
    //     // Given
    //     when(roomTypeRestService.getRoomTypeById(anyString()))
    //             .thenThrow(new RuntimeException("Room type not found"));

    //     // When & Then
    //     mockMvc.perform(get("/api/room-types/FAKE-ROOM-TYPE-ID")
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Room type not found"));

    //     verify(roomTypeRestService).getRoomTypeById("FAKE-ROOM-TYPE-ID");
    // }

    @Test
    void testGetRoomType_WithAllFields() throws Exception {
        // Given
        when(roomTypeRestService.getRoomTypeById(roomTypeID)).thenReturn(roomTypeResponseDTO);

        // When & Then
        mockMvc.perform(get("/api/room-types/" + roomTypeID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.facility").value("TV, AC, WiFi, Desk"))
                .andExpect(jsonPath("$.data.floor").value(2))
                .andExpect(jsonPath("$.data.description").value("Comfortable single room for solo travelers"))
                .andExpect(jsonPath("$.data.propertyID").value(propertyID))
                .andExpect(jsonPath("$.data.propertyName").value("Grand Hotel Jakarta"));
    }

    // ==================== GET ALL ROOM TYPES ====================

    @Test
    void testGetAllRoomTypes_Success() throws Exception {
        // Given
        List<RoomTypeResponseDTO> roomTypes = Arrays.asList(roomTypeResponseDTO);
        when(roomTypeRestService.getAllRoomTypes()).thenReturn(roomTypes);

        // When & Then
        mockMvc.perform(get("/api/room-types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].roomTypeID").value(roomTypeID))
                .andExpect(jsonPath("$.message").value("Room types retrieved successfully"));

        verify(roomTypeRestService).getAllRoomTypes();
    }

    @Test
    void testGetAllRoomTypes_EmptyList() throws Exception {
        // Given
        when(roomTypeRestService.getAllRoomTypes()).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/api/room-types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("Room types retrieved successfully"));
    }

    @Test
    void testGetAllRoomTypes_MultipleRoomTypes() throws Exception {
        // Given
        RoomTypeResponseDTO roomType2 = RoomTypeResponseDTO.builder()
                .roomTypeID("001–Double_Room–3")
                .name("Double Room")
                .price(500000)
                .capacity(5)
                .floor(3)
                .propertyID(propertyID)
                .build();

        RoomTypeResponseDTO roomType3 = RoomTypeResponseDTO.builder()
                .roomTypeID("001–Suite_Room–4")
                .name("Suite Room")
                .price(1000000)
                .capacity(6)
                .floor(4)
                .propertyID(propertyID)
                .build();

        List<RoomTypeResponseDTO> roomTypes = Arrays.asList(roomTypeResponseDTO, roomType2, roomType3);
        when(roomTypeRestService.getAllRoomTypes()).thenReturn(roomTypes);

        // When & Then
        mockMvc.perform(get("/api/room-types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    // ==================== GET ROOM TYPES BY PROPERTY ====================

    @Test
    void testGetRoomTypesByProperty_Success() throws Exception {
        // Given
        List<RoomTypeResponseDTO> roomTypes = Arrays.asList(roomTypeResponseDTO);
        when(roomTypeRestService.getRoomTypesByProperty(propertyID)).thenReturn(roomTypes);

        // When & Then
        mockMvc.perform(get("/api/room-types/property/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].propertyID").value(propertyID))
                .andExpect(jsonPath("$.message").value("Property room types retrieved successfully"));

        verify(roomTypeRestService).getRoomTypesByProperty(propertyID);
    }

    @Test
    void testGetRoomTypesByProperty_EmptyList() throws Exception {
        // Given
        when(roomTypeRestService.getRoomTypesByProperty(anyString())).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/api/room-types/property/FAKE-PROPERTY-ID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testGetRoomTypesByProperty_MultipleRoomTypes() throws Exception {
        // Given
        RoomTypeResponseDTO roomType2 = RoomTypeResponseDTO.builder()
                .roomTypeID("001–Double_Room–3")
                .name("Double Room")
                .propertyID(propertyID)
                .propertyName("Grand Hotel Jakarta")
                .build();

        List<RoomTypeResponseDTO> roomTypes = Arrays.asList(roomTypeResponseDTO, roomType2);
        when(roomTypeRestService.getRoomTypesByProperty(propertyID)).thenReturn(roomTypes);

        // When & Then
        mockMvc.perform(get("/api/room-types/property/" + propertyID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    // ==================== UPDATE ROOM TYPE ====================

    @Test
    void testUpdateRoomType_Success() throws Exception {
        // Given
        RoomTypeResponseDTO updatedRoomType = RoomTypeResponseDTO.builder()
                .roomTypeID(roomTypeID)
                .name("Single Room - Updated")
                .price(350000)
                .capacity(4)
                .facility("TV, AC, WiFi, Desk, Mini Fridge")
                .floor(2)
                .description("Updated comfortable single room")
                .propertyID(propertyID)
                .propertyName("Grand Hotel Jakarta")
                .build();

        when(roomTypeRestService.updateRoomType(eq(roomTypeID), any(UpdateRoomTypeRequestDTO.class)))
                .thenReturn(updatedRoomType);

        // When & Then
        mockMvc.perform(put("/api/room-types/" + roomTypeID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRoomTypeRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.name").value("Single Room - Updated"))
                .andExpect(jsonPath("$.data.price").value(350000))
                .andExpect(jsonPath("$.message").value("Room type updated successfully"));

        verify(roomTypeRestService).updateRoomType(eq(roomTypeID), any(UpdateRoomTypeRequestDTO.class));
    }

    // @Test
    // void testUpdateRoomType_NotFound() throws Exception {
    //     // Given
    //     when(roomTypeRestService.updateRoomType(anyString(), any(UpdateRoomTypeRequestDTO.class)))
    //             .thenThrow(new RuntimeException("Room type not found"));

    //     // When & Then
    //     mockMvc.perform(put("/api/room-types/FAKE-ROOM-TYPE-ID")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(updateRoomTypeRequestDTO)))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Room type not found"));

    //     verify(roomTypeRestService).updateRoomType(eq("FAKE-ROOM-TYPE-ID"), any(UpdateRoomTypeRequestDTO.class));
    // }

    @Test
    void testUpdateRoomType_ValidationError() throws Exception {
        // Given - DTO with invalid data (FIXED - use setRoomTypeID instead)
        updateRoomTypeRequestDTO.setRoomTypeID(""); // ✅ FIXED: Empty roomTypeID

        // When & Then
        mockMvc.perform(put("/api/room-types/" + roomTypeID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRoomTypeRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(roomTypeRestService, never()).updateRoomType(anyString(), any());
    }

    @Test
    void testUpdateRoomType_PartialUpdate() throws Exception {
        // Given - Only update price
        updateRoomTypeRequestDTO = UpdateRoomTypeRequestDTO.builder()
                .roomTypeID(roomTypeID)
                .price(400000)
                .capacity(3)
                .facility("TV, AC, WiFi, Desk")
                .build();

        RoomTypeResponseDTO partialUpdatedRoomType = RoomTypeResponseDTO.builder()
                .roomTypeID(roomTypeID)
                .name("Single Room")
                .price(400000) // Only price changed
                .capacity(3)
                .facility("TV, AC, WiFi, Desk")
                .floor(2)
                .propertyID(propertyID)
                .build();

        when(roomTypeRestService.updateRoomType(eq(roomTypeID), any(UpdateRoomTypeRequestDTO.class)))
                .thenReturn(partialUpdatedRoomType);

        // When & Then
        mockMvc.perform(put("/api/room-types/" + roomTypeID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRoomTypeRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.price").value(400000))
                .andExpect(jsonPath("$.data.name").value("Single Room"));
    }

    @Test
    void testUpdateRoomType_InvalidPrice() throws Exception {
        // Given
        updateRoomTypeRequestDTO.setPrice(-500000);

        // When & Then
        mockMvc.perform(put("/api/room-types/" + roomTypeID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRoomTypeRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(roomTypeRestService, never()).updateRoomType(anyString(), any());
    }

    // ==================== DELETE ROOM TYPE ====================

    @Test
    void testDeleteRoomType_Success() throws Exception {
        // Given
        doNothing().when(roomTypeRestService).deleteRoomType(roomTypeID);

        // When & Then
        mockMvc.perform(delete("/api/room-types/" + roomTypeID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Room type deleted successfully"));

        verify(roomTypeRestService).deleteRoomType(roomTypeID);
    }

    // @Test
    // void testDeleteRoomType_NotFound() throws Exception {
    //     // Given
    //     doThrow(new RuntimeException("Room type not found"))
    //             .when(roomTypeRestService).deleteRoomType(anyString());

    //     // When & Then
    //     mockMvc.perform(delete("/api/room-types/FAKE-ROOM-TYPE-ID")
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Room type not found"));

    //     verify(roomTypeRestService).deleteRoomType("FAKE-ROOM-TYPE-ID");
    // }

    // @Test
    // void testDeleteRoomType_HasRooms() throws Exception {
    //     // Given
    //     doThrow(new RuntimeException("Cannot delete room type with existing rooms"))
    //             .when(roomTypeRestService).deleteRoomType(roomTypeID);

    //     // When & Then
    //     mockMvc.perform(delete("/api/room-types/" + roomTypeID)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Cannot delete room type with existing rooms"));

    //     verify(roomTypeRestService).deleteRoomType(roomTypeID);
    // }

    // ==================== ADDITIONAL EDGE CASES ====================

    @Test
    void testGetRoomTypesByProperty_DifferentProperties() throws Exception {
        // Given
        String property2ID = "HOT-4000-002";
        
        RoomTypeResponseDTO roomTypeProperty2 = RoomTypeResponseDTO.builder()
                .roomTypeID("002–Deluxe_Room–3")
                .name("Deluxe Room")
                .price(750000)
                .capacity(4)
                .floor(3)
                .propertyID(property2ID)
                .propertyName("Luxury Hotel Surabaya")
                .build();

        List<RoomTypeResponseDTO> roomTypes = Arrays.asList(roomTypeProperty2);
        when(roomTypeRestService.getRoomTypesByProperty(property2ID)).thenReturn(roomTypes);

        // When & Then
        mockMvc.perform(get("/api/room-types/property/" + property2ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].propertyName").value("Luxury Hotel Surabaya"))
                .andExpect(jsonPath("$.data[0].propertyID").value(property2ID));
    }

    @Test
    void testCreateRoomType_DifferentFloors() throws Exception {
        // Given
        createRoomTypeRequestDTO.setFloor(5);
        
        RoomTypeResponseDTO floor5RoomType = RoomTypeResponseDTO.builder()
                .roomTypeID("001–Penthouse–5")
                .name("Penthouse")
                .floor(5)
                .price(2000000)
                .capacity(8)
                .propertyID(propertyID)
                .build();

        when(roomTypeRestService.createRoomType(any(CreateRoomTypeRequestDTO.class)))
                .thenReturn(floor5RoomType);

        // When & Then
        mockMvc.perform(post("/api/room-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRoomTypeRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.floor").value(5))
                .andExpect(jsonPath("$.data.name").value("Penthouse"));
    }

    @Test
    void testGetAllRoomTypes_MixedFloors() throws Exception {
        // Given
        RoomTypeResponseDTO floor2RoomType = RoomTypeResponseDTO.builder()
                .roomTypeID("001–Single_Room–2")
                .name("Single Room")
                .floor(2)
                .build();

        RoomTypeResponseDTO floor3RoomType = RoomTypeResponseDTO.builder()
                .roomTypeID("001–Double_Room–3")
                .name("Double Room")
                .floor(3)
                .build();

        RoomTypeResponseDTO floor4RoomType = RoomTypeResponseDTO.builder()
                .roomTypeID("001–Suite–4")
                .name("Suite")
                .floor(4)
                .build();

        List<RoomTypeResponseDTO> roomTypes = Arrays.asList(floor2RoomType, floor3RoomType, floor4RoomType);
        when(roomTypeRestService.getAllRoomTypes()).thenReturn(roomTypes);

        // When & Then
        mockMvc.perform(get("/api/room-types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].floor").value(2))
                .andExpect(jsonPath("$.data[1].floor").value(3))
                .andExpect(jsonPath("$.data[2].floor").value(4));
    }

    @Test
    void testCreateRoomType_NullDescription() throws Exception {
        // Given
        createRoomTypeRequestDTO.setDescription(null);
        
        RoomTypeResponseDTO roomTypeWithoutDescription = RoomTypeResponseDTO.builder()
                .roomTypeID(roomTypeID)
                .name("Double Room")
                .price(500000)
                .capacity(5)
                .facility("TV, AC, WiFi")
                .floor(3)
                .description(null)
                .propertyID(propertyID)
                .build();

        when(roomTypeRestService.createRoomType(any(CreateRoomTypeRequestDTO.class)))
                .thenReturn(roomTypeWithoutDescription);

        // When & Then
        mockMvc.perform(post("/api/room-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRoomTypeRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.description").doesNotExist());
    }

    @Test
    void testUpdateRoomType_UpdateAllFields() throws Exception {
        // Given (FIXED - removed .name())
        updateRoomTypeRequestDTO = UpdateRoomTypeRequestDTO.builder()
                .roomTypeID(roomTypeID)
                .price(450000)
                .capacity(3)
                .facility("TV, AC, WiFi, Desk, Safe, Mini Bar")
                .description("Premium single room with additional amenities")
                .build();

        RoomTypeResponseDTO fullyUpdatedRoomType = RoomTypeResponseDTO.builder()
                .roomTypeID(roomTypeID)
                .name("Premium Single Room")
                .price(450000)
                .capacity(3)
                .facility("TV, AC, WiFi, Desk, Safe, Mini Bar")
                .floor(2)
                .description("Premium single room with additional amenities")
                .propertyID(propertyID)
                .propertyName("Grand Hotel Jakarta")
                .build();

        when(roomTypeRestService.updateRoomType(eq(roomTypeID), any(UpdateRoomTypeRequestDTO.class)))
                .thenReturn(fullyUpdatedRoomType);

        // When & Then
        mockMvc.perform(put("/api/room-types/" + roomTypeID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRoomTypeRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Premium Single Room"))
                .andExpect(jsonPath("$.data.price").value(450000))
                .andExpect(jsonPath("$.data.facility").value("TV, AC, WiFi, Desk, Safe, Mini Bar"))
                .andExpect(jsonPath("$.data.description").value("Premium single room with additional amenities"));
    }
}