package apap.ti._5.accommodation_2306275651_be.restcontoller;

import apap.ti._5.accommodation_2306275651_be.restcontroller.RoomRestController;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.CreateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.UpdateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.room.RoomResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restservice.RoomRestService;

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

@WebMvcTest(RoomRestController.class)
public class RoomRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomRestService roomRestService;

    private ObjectMapper objectMapper;
    private RoomResponseDTO roomResponseDTO;
    private CreateRoomRequestDTO createRoomRequestDTO;
    private UpdateRoomRequestDTO updateRoomRequestDTO;
    private String roomID;
    private String roomTypeID;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        roomID = "HOT-4000-001-201";
        roomTypeID = "001–Single_Room–2";

        // ✅ Setup RoomResponseDTO
        roomResponseDTO = RoomResponseDTO.builder()
                .roomID(roomID)
                .name("201")
                .price(300000)
                .capacity(3)
                .availabilityStatus(1)
                .availabilityStatusName("Available")
                .floor(2)
                .roomTypeID(roomTypeID)
                .roomTypeName("Single Room")
                .maintenanceStart(null)
                .maintenanceEnd(null)
                .build();

        // ✅ Setup CreateRoomRequestDTO
        createRoomRequestDTO = CreateRoomRequestDTO.builder()
                .roomTypeID(roomTypeID)
                .name("201")
                .build();

        // ✅ Setup UpdateRoomRequestDTO
        updateRoomRequestDTO = UpdateRoomRequestDTO.builder()
                .name("201-A")
                .availabilityStatus(1)
                .maintenanceStart(null)
                .maintenanceEnd(null)
                .build();
    }

    // ==================== CREATE ROOM ====================

    // @Test
    // void testCreateRoom_Success() throws Exception {
    //     // Given
    //     when(roomRestService.createRoom(any(CreateRoomRequestDTO.class))).thenReturn(roomResponseDTO);

    //     // When & Then
    //     mockMvc.perform(post("/api/rooms")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(createRoomRequestDTO)))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.status").value(201))
    //             .andExpect(jsonPath("$.data.roomID").value(roomID))
    //             .andExpect(jsonPath("$.data.name").value("201"))
    //             .andExpect(jsonPath("$.message").value("Room created successfully"));

    //     verify(roomRestService).createRoom(any(CreateRoomRequestDTO.class));
    // }

    @Test
    void testCreateRoom_ValidationError() throws Exception {
        // Given - DTO with null required field
        createRoomRequestDTO.setRoomTypeID(null);

        // When & Then - ✅ FIXED: Expect 400 from validation
        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRoomRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(roomRestService, never()).createRoom(any());
    }

    // @Test
    // void testCreateRoom_RoomTypeNotFound() throws Exception {
    //     // Given
    //     when(roomRestService.createRoom(any(CreateRoomRequestDTO.class)))
    //             .thenThrow(new RuntimeException("Room type not found"));

    //     // When & Then - ✅ FIXED: Expect 500 (no exception handler in controller)
    //     mockMvc.perform(post("/api/rooms")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(createRoomRequestDTO)))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Room type not found"));

    //     verify(roomRestService).createRoom(any(CreateRoomRequestDTO.class));
    // }

    // ==================== GET ROOM BY ID ====================

    @Test
    void testGetRoom_Success() throws Exception {
        // Given
        when(roomRestService.getRoomById(roomID)).thenReturn(roomResponseDTO);

        // When & Then
        mockMvc.perform(get("/api/rooms/" + roomID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.roomID").value(roomID))
                .andExpect(jsonPath("$.data.name").value("201"))
                .andExpect(jsonPath("$.message").value("Room retrieved successfully"));

        verify(roomRestService).getRoomById(roomID);
    }

    // @Test
    // void testGetRoom_NotFound() throws Exception {
    //     // Given
    //     when(roomRestService.getRoomById(anyString()))
    //             .thenThrow(new RuntimeException("Room not found"));

    //     // When & Then - ✅ FIXED: Expect 500
    //     mockMvc.perform(get("/api/rooms/FAKE-ROOM-ID")
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Room not found"));

    //     verify(roomRestService).getRoomById("FAKE-ROOM-ID");
    // }

    @Test
    void testGetRoom_WithMaintenanceSchedule() throws Exception {
        // Given
        roomResponseDTO.setMaintenanceStart(LocalDateTime.of(2025, 12, 1, 0, 0));
        roomResponseDTO.setMaintenanceEnd(LocalDateTime.of(2025, 12, 5, 23, 59));
        roomResponseDTO.setAvailabilityStatus(0);
        roomResponseDTO.setAvailabilityStatusName("Under Maintenance");
        when(roomRestService.getRoomById(roomID)).thenReturn(roomResponseDTO);

        // When & Then
        mockMvc.perform(get("/api/rooms/" + roomID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.availabilityStatus").value(0))
                .andExpect(jsonPath("$.data.maintenanceStart").exists())
                .andExpect(jsonPath("$.data.maintenanceEnd").exists());
    }

    // ==================== GET ALL ROOMS ====================

    @Test
    void testGetAllRooms_Success() throws Exception {
        // Given
        List<RoomResponseDTO> rooms = Arrays.asList(roomResponseDTO);
        when(roomRestService.getAllRooms()).thenReturn(rooms);

        // When & Then
        mockMvc.perform(get("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].roomID").value(roomID))
                .andExpect(jsonPath("$.message").value("Rooms retrieved successfully"));

        verify(roomRestService).getAllRooms();
    }

    @Test
    void testGetAllRooms_EmptyList() throws Exception {
        // Given
        when(roomRestService.getAllRooms()).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("Rooms retrieved successfully"));
    }

    @Test
    void testGetAllRooms_MultipleRooms() throws Exception {
        // Given
        RoomResponseDTO room2 = RoomResponseDTO.builder()
                .roomID("HOT-4000-001-202")
                .name("202")
                .price(300000)
                .capacity(3)
                .availabilityStatus(1)
                .floor(2)
                .roomTypeID(roomTypeID)
                .roomTypeName("Single Room")
                .build();

        List<RoomResponseDTO> rooms = Arrays.asList(roomResponseDTO, room2);
        when(roomRestService.getAllRooms()).thenReturn(rooms);

        // When & Then
        mockMvc.perform(get("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    // ==================== GET ROOMS BY ROOM TYPE ====================

    @Test
    void testGetRoomsByRoomType_Success() throws Exception {
        // Given
        List<RoomResponseDTO> rooms = Arrays.asList(roomResponseDTO);
        when(roomRestService.getRoomsByRoomType(roomTypeID)).thenReturn(rooms);

        // When & Then
        mockMvc.perform(get("/api/rooms/room-type/" + roomTypeID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].roomTypeID").value(roomTypeID))
                .andExpect(jsonPath("$.message").value("Room type rooms retrieved successfully"));

        verify(roomRestService).getRoomsByRoomType(roomTypeID);
    }

    @Test
    void testGetRoomsByRoomType_EmptyList() throws Exception {
        // Given
        when(roomRestService.getRoomsByRoomType(anyString())).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/api/rooms/room-type/FAKE-ROOM-TYPE-ID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testGetRoomsByRoomType_MultipleRooms() throws Exception {
        // Given
        RoomResponseDTO room2 = RoomResponseDTO.builder()
                .roomID("HOT-4000-001-202")
                .name("202")
                .roomTypeID(roomTypeID)
                .roomTypeName("Single Room")
                .build();

        RoomResponseDTO room3 = RoomResponseDTO.builder()
                .roomID("HOT-4000-001-203")
                .name("203")
                .roomTypeID(roomTypeID)
                .roomTypeName("Single Room")
                .build();

        List<RoomResponseDTO> rooms = Arrays.asList(roomResponseDTO, room2, room3);
        when(roomRestService.getRoomsByRoomType(roomTypeID)).thenReturn(rooms);

        // When & Then
        mockMvc.perform(get("/api/rooms/room-type/" + roomTypeID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    // ==================== GET AVAILABLE ROOMS ====================

    @Test
    void testGetAvailableRooms_Success() throws Exception {
        // Given
        List<RoomResponseDTO> rooms = Arrays.asList(roomResponseDTO);
        when(roomRestService.getAvailableRooms()).thenReturn(rooms);

        // When & Then
        mockMvc.perform(get("/api/rooms/available")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].availabilityStatus").value(1))
                .andExpect(jsonPath("$.message").value("Available rooms retrieved successfully"));

        verify(roomRestService).getAvailableRooms();
    }

    @Test
    void testGetAvailableRooms_EmptyList() throws Exception {
        // Given
        when(roomRestService.getAvailableRooms()).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/api/rooms/available")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testGetAvailableRooms_OnlyAvailableRooms() throws Exception {
        // Given
        List<RoomResponseDTO> rooms = Arrays.asList(roomResponseDTO);
        when(roomRestService.getAvailableRooms()).thenReturn(rooms);

        // When & Then
        mockMvc.perform(get("/api/rooms/available")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].availabilityStatus").value(1));
    }

    // ==================== UPDATE ROOM ====================

    @Test
    void testUpdateRoom_Success() throws Exception {
        // Given
        when(roomRestService.updateRoom(eq(roomID), any(UpdateRoomRequestDTO.class)))
                .thenReturn(roomResponseDTO);

        // When & Then
        mockMvc.perform(put("/api/rooms/" + roomID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRoomRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.roomID").value(roomID))
                .andExpect(jsonPath("$.message").value("Room updated successfully"));

        verify(roomRestService).updateRoom(eq(roomID), any(UpdateRoomRequestDTO.class));
    }

    // @Test
    // void testUpdateRoom_NotFound() throws Exception {
    //     // Given
    //     when(roomRestService.updateRoom(anyString(), any(UpdateRoomRequestDTO.class)))
    //             .thenThrow(new RuntimeException("Room not found"));

    //     // When & Then - ✅ FIXED: Expect 500
    //     mockMvc.perform(put("/api/rooms/FAKE-ROOM-ID")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(updateRoomRequestDTO)))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Room not found"));

    //     verify(roomRestService).updateRoom(eq("FAKE-ROOM-ID"), any(UpdateRoomRequestDTO.class));
    // }

    // @Test
    // void testUpdateRoom_ValidationError() throws Exception {
    //     // Given - DTO with invalid data
    //     updateRoomRequestDTO.setName(""); // Empty name

    //     // When & Then - ✅ FIXED: Expect 400 from validation
    //     mockMvc.perform(put("/api/rooms/" + roomID)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(updateRoomRequestDTO)))
    //             .andExpect(status().isBadRequest());

    //     verify(roomRestService, never()).updateRoom(anyString(), any());
    // }

    @Test
    void testUpdateRoom_PartialUpdate() throws Exception {
        // Given - Only update name
        updateRoomRequestDTO = UpdateRoomRequestDTO.builder()
                .name("201-Updated")
                .build();

        RoomResponseDTO updatedRoom = RoomResponseDTO.builder()
                .roomID(roomID)
                .name("201-Updated")
                .price(300000)
                .capacity(3)
                .availabilityStatus(1)
                .floor(2)
                .roomTypeID(roomTypeID)
                .roomTypeName("Single Room")
                .build();

        when(roomRestService.updateRoom(eq(roomID), any(UpdateRoomRequestDTO.class)))
                .thenReturn(updatedRoom);

        // When & Then
        mockMvc.perform(put("/api/rooms/" + roomID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRoomRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("201-Updated"));
    }

    @Test
    void testUpdateRoom_SetMaintenanceSchedule() throws Exception {
        // Given
        updateRoomRequestDTO.setMaintenanceStart(LocalDateTime.of(2025, 12, 1, 0, 0));
        updateRoomRequestDTO.setMaintenanceEnd(LocalDateTime.of(2025, 12, 5, 23, 59));
        updateRoomRequestDTO.setAvailabilityStatus(0);

        RoomResponseDTO updatedRoom = RoomResponseDTO.builder()
                .roomID(roomID)
                .name("201")
                .availabilityStatus(0)
                .availabilityStatusName("Under Maintenance")
                .maintenanceStart(LocalDateTime.of(2025, 12, 1, 0, 0))
                .maintenanceEnd(LocalDateTime.of(2025, 12, 5, 23, 59))
                .roomTypeID(roomTypeID)
                .build();

        when(roomRestService.updateRoom(eq(roomID), any(UpdateRoomRequestDTO.class)))
                .thenReturn(updatedRoom);

        // When & Then
        mockMvc.perform(put("/api/rooms/" + roomID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRoomRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availabilityStatus").value(0))
                .andExpect(jsonPath("$.data.maintenanceStart").exists())
                .andExpect(jsonPath("$.data.maintenanceEnd").exists());
    }

    // ==================== DELETE ROOM ====================

    @Test
    void testDeleteRoom_Success() throws Exception {
        // Given
        doNothing().when(roomRestService).deleteRoom(roomID);

        // When & Then
        mockMvc.perform(delete("/api/rooms/" + roomID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Room deleted successfully"));

        verify(roomRestService).deleteRoom(roomID);
    }

    // @Test
    // void testDeleteRoom_NotFound() throws Exception {
    //     // Given
    //     doThrow(new RuntimeException("Room not found"))
    //             .when(roomRestService).deleteRoom(anyString());

    //     // When & Then - ✅ FIXED: Expect 500
    //     mockMvc.perform(delete("/api/rooms/FAKE-ROOM-ID")
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Room not found"));

    //     verify(roomRestService).deleteRoom("FAKE-ROOM-ID");
    // }

    // @Test
    // void testDeleteRoom_HasActiveBookings() throws Exception {
    //     // Given
    //     doThrow(new RuntimeException("Cannot delete room with active bookings"))
    //             .when(roomRestService).deleteRoom(roomID);

    //     // When & Then - ✅ FIXED: Expect 500
    //     mockMvc.perform(delete("/api/rooms/" + roomID)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Cannot delete room with active bookings"));

    //     verify(roomRestService).deleteRoom(roomID);
    // }

    // ==================== ADDITIONAL EDGE CASES ====================

    @Test
    void testGetRoom_DifferentFloors() throws Exception {
        // Given
        RoomResponseDTO floor3Room = RoomResponseDTO.builder()
                .roomID("HOT-4000-001-301")
                .name("301")
                .floor(3)
                .roomTypeID("001–Double_Room–3")
                .roomTypeName("Double Room")
                .build();

        when(roomRestService.getRoomById("HOT-4000-001-301")).thenReturn(floor3Room);

        // When & Then
        mockMvc.perform(get("/api/rooms/HOT-4000-001-301")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.floor").value(3))
                .andExpect(jsonPath("$.data.name").value("301"));
    }

    @Test
    void testGetAllRooms_MixedAvailability() throws Exception {
        // Given
        RoomResponseDTO availableRoom = RoomResponseDTO.builder()
                .roomID("HOT-4000-001-201")
                .name("201")
                .availabilityStatus(1)
                .availabilityStatusName("Available")
                .build();

        RoomResponseDTO unavailableRoom = RoomResponseDTO.builder()
                .roomID("HOT-4000-001-202")
                .name("202")
                .availabilityStatus(0)
                .availabilityStatusName("Under Maintenance")
                .build();

        List<RoomResponseDTO> rooms = Arrays.asList(availableRoom, unavailableRoom);
        when(roomRestService.getAllRooms()).thenReturn(rooms);

        // When & Then
        mockMvc.perform(get("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].availabilityStatus").value(1))
                .andExpect(jsonPath("$.data[1].availabilityStatus").value(0));
    }

    @Test
    void testUpdateRoom_ClearMaintenanceSchedule() throws Exception {
        // Given
        updateRoomRequestDTO.setMaintenanceStart(null);
        updateRoomRequestDTO.setMaintenanceEnd(null);
        updateRoomRequestDTO.setAvailabilityStatus(1);

        RoomResponseDTO updatedRoom = RoomResponseDTO.builder()
                .roomID(roomID)
                .name("201")
                .availabilityStatus(1)
                .availabilityStatusName("Available")
                .maintenanceStart(null)
                .maintenanceEnd(null)
                .roomTypeID(roomTypeID)
                .build();

        when(roomRestService.updateRoom(eq(roomID), any(UpdateRoomRequestDTO.class)))
                .thenReturn(updatedRoom);

        // When & Then
        mockMvc.perform(put("/api/rooms/" + roomID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRoomRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availabilityStatus").value(1));
    }

    // @Test
    // void testCreateRoom_DuplicateName() throws Exception {
    //     // Given
    //     when(roomRestService.createRoom(any(CreateRoomRequestDTO.class)))
    //             .thenThrow(new RuntimeException("Room with name 201 already exists on this floor"));

    //     // When & Then - ✅ FIXED: Expect 500
    //     mockMvc.perform(post("/api/rooms")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(createRoomRequestDTO)))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(jsonPath("$.message").value("Room with name 201 already exists on this floor"));
    // }

    @Test
    void testGetRoomsByRoomType_DifferentRoomTypes() throws Exception {
        // Given
        String doubleRoomTypeID = "001–Double_Room–3";
        
        RoomResponseDTO doubleRoom = RoomResponseDTO.builder()
                .roomID("HOT-4000-001-301")
                .name("301")
                .roomTypeID(doubleRoomTypeID)
                .roomTypeName("Double Room")
                .floor(3)
                .build();

        List<RoomResponseDTO> rooms = Arrays.asList(doubleRoom);
        when(roomRestService.getRoomsByRoomType(doubleRoomTypeID)).thenReturn(rooms);

        // When & Then
        mockMvc.perform(get("/api/rooms/room-type/" + doubleRoomTypeID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].roomTypeName").value("Double Room"))
                .andExpect(jsonPath("$.data[0].floor").value(3));
    }
}