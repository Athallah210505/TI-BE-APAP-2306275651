package apap.ti._5.accommodation_2306275651_be.restservice;

import apap.ti._5.accommodation_2306275651_be.model.Booking;
import apap.ti._5.accommodation_2306275651_be.model.Property;
import apap.ti._5.accommodation_2306275651_be.model.Room;
import apap.ti._5.accommodation_2306275651_be.model.RoomType;
import apap.ti._5.accommodation_2306275651_be.repository.BookingRepository;
import apap.ti._5.accommodation_2306275651_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306275651_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.CreateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.UpdateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.room.RoomResponseDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RoomRestServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private RoomRestServiceImpl roomRestService;

    private String existingRoomId;
    private String fakeRoomId;
    private Room room;
    private RoomType roomType;
    private Property property;
    private CreateRoomRequestDTO createRoomRequestDTO;
    private UpdateRoomRequestDTO updateRoomRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingRoomId = "HOT-4000-001-201";
        fakeRoomId = "HOT-9999-999";

        // ✅ Setup Property
        property = Property.builder()
                .propertyID("HOT-4000-001")
                .propertyName("Grand Hotel Jakarta")
                .type(1)
                .activeStatus(1)
                .build();

        // ✅ Setup RoomType
        roomType = RoomType.builder()
                .roomTypeID("RT-HOT-4000-001-001")
                .name("Single Room")
                .price(300000)
                .capacity(3)
                .facility("TV, AC, WiFi")
                .floor(2)
                .property(property)
                .build();

        // ✅ Setup Room
        room = Room.builder()
                .roomID(existingRoomId)
                .name("201")
                .availabilityStatus(1)
                .activeRoom(1)
                .maintenanceStart(null)
                .maintenanceEnd(null)
                .roomType(roomType)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        // ✅ Setup CreateRoomRequestDTO
        createRoomRequestDTO = CreateRoomRequestDTO.builder()
                .roomTypeID("RT-HOT-4000-001-001")
                .availabilityStatus(1)
                .activeRoom(1)
                .build();

        // ✅ Setup UpdateRoomRequestDTO
        updateRoomRequestDTO = UpdateRoomRequestDTO.builder()
                .name("201-Updated")
                .availabilityStatus(1)
                .activeRoom(1)
                .maintenanceStart(LocalDateTime.of(2025, 12, 1, 0, 0))
                .maintenanceEnd(LocalDateTime.of(2025, 12, 5, 23, 59))
                .build();
    }

    // ==================== CREATE ROOM ====================

    @Test
    void testCreateRoom_Success() {
        // Given
        when(roomTypeRepository.findById("RT-HOT-4000-001-001")).thenReturn(Optional.of(roomType));
        when(roomRepository.findByPropertyIDAndFloor("HOT-4000-001", 2)).thenReturn(new ArrayList<>());
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        RoomResponseDTO result = roomRestService.createRoom(createRoomRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals(existingRoomId, result.getRoomID());
        assertEquals("201", result.getName());
        assertEquals(1, result.getAvailabilityStatus());
        assertEquals("Available", result.getAvailabilityStatusName());
        verify(roomTypeRepository).findById("RT-HOT-4000-001-001");
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void testCreateRoom_RoomTypeNotFound() {
        // Given
        when(roomTypeRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roomRestService.createRoom(createRoomRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Room type not found"));
        verify(roomTypeRepository).findById("RT-HOT-4000-001-001");
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testCreateRoom_GeneratesSequentialRoomID() {
        // Given
        Room existingRoom1 = Room.builder()
                .roomID("HOT-4000-001-201")
                .roomType(roomType)
                .build();
        
        Room existingRoom2 = Room.builder()
                .roomID("HOT-4000-001-202")
                .roomType(roomType)
                .build();

        when(roomTypeRepository.findById("RT-HOT-4000-001-001")).thenReturn(Optional.of(roomType));
        when(roomRepository.findByPropertyIDAndFloor("HOT-4000-001", 2))
                .thenReturn(Arrays.asList(existingRoom1, existingRoom2));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room saved = invocation.getArgument(0);
            assertEquals("HOT-4000-001-203", saved.getRoomID()); // Should be 3rd room
            return saved;
        });

        // When
        roomRestService.createRoom(createRoomRequestDTO);

        // Then
        verify(roomRepository).save(argThat(r -> r.getRoomID().equals("HOT-4000-001-203")));
    }

    // ==================== GET ROOM BY ID ====================

    @Test
    void testGetRoomById_Found() {
        // Given
        when(roomRepository.findById(existingRoomId)).thenReturn(Optional.of(room));

        // When
        RoomResponseDTO result = roomRestService.getRoomById(existingRoomId);

        // Then
        assertNotNull(result);
        assertEquals(existingRoomId, result.getRoomID());
        assertEquals("201", result.getName());
        assertEquals(300000, result.getPrice());
        assertEquals(3, result.getCapacity());
        verify(roomRepository).findById(existingRoomId);
    }

    @Test
    void testGetRoomById_NotFound() {
        // Given
        when(roomRepository.findById(fakeRoomId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roomRestService.getRoomById(fakeRoomId);
        });

        assertTrue(exception.getMessage().contains("Room not found"));
        verify(roomRepository).findById(fakeRoomId);
    }

    @Test
    void testGetRoomById_ExtractsRoomNameCorrectly() {
        // Given
        when(roomRepository.findById(existingRoomId)).thenReturn(Optional.of(room));

        // When
        RoomResponseDTO result = roomRestService.getRoomById(existingRoomId);

        // Then
        assertEquals("201", result.getName()); // Extracted from HOT-4000-001-201
        assertEquals("HOT-4000-001-201", result.getRoomID()); // Full ID preserved
    }

    // ==================== GET ROOM ENTITY BY ID ====================

    @Test
    void testGetRoomEntityById_Found() {
        // Given
        when(roomRepository.findById(existingRoomId)).thenReturn(Optional.of(room));

        // When
        Room result = roomRestService.getRoomEntityById(existingRoomId);

        // Then
        assertNotNull(result);
        assertEquals(existingRoomId, result.getRoomID());
        assertEquals(roomType, result.getRoomType());
        verify(roomRepository).findById(existingRoomId);
    }

    @Test
    void testGetRoomEntityById_NotFound() {
        // Given
        when(roomRepository.findById(fakeRoomId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roomRestService.getRoomEntityById(fakeRoomId);
        });

        assertTrue(exception.getMessage().contains("Room not found"));
    }

    // ==================== GET ALL ROOMS ====================

    @Test
    void testGetAllRooms_Success() {
        // Given
        List<Room> rooms = Arrays.asList(room);
        when(roomRepository.findAll()).thenReturn(rooms);

        // When
        List<RoomResponseDTO> result = roomRestService.getAllRooms();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(existingRoomId, result.get(0).getRoomID());
        verify(roomRepository).findAll();
    }

    @Test
    void testGetAllRooms_EmptyList() {
        // Given
        when(roomRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        List<RoomResponseDTO> result = roomRestService.getAllRooms();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roomRepository).findAll();
    }

    // ==================== GET ROOMS BY ROOM TYPE ====================

    @Test
    void testGetRoomsByRoomType_Found() {
        // Given
        List<Room> rooms = Arrays.asList(room);
        when(roomRepository.findByRoomType_RoomTypeID("RT-HOT-4000-001-001")).thenReturn(rooms);

        // When
        List<RoomResponseDTO> result = roomRestService.getRoomsByRoomType("RT-HOT-4000-001-001");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("RT-HOT-4000-001-001", result.get(0).getRoomTypeID());
        verify(roomRepository).findByRoomType_RoomTypeID("RT-HOT-4000-001-001");
    }

    @Test
    void testGetRoomsByRoomType_NotFound() {
        // Given
        when(roomRepository.findByRoomType_RoomTypeID("RT-FAKE-001")).thenReturn(new ArrayList<>());

        // When
        List<RoomResponseDTO> result = roomRestService.getRoomsByRoomType("RT-FAKE-001");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== GET AVAILABLE ROOMS ====================

    @Test
    void testGetAvailableRooms_Found() {
        // Given
        List<Room> rooms = Arrays.asList(room);
        when(roomRepository.findByAvailabilityStatus(1)).thenReturn(rooms);

        // When
        List<RoomResponseDTO> result = roomRestService.getAvailableRooms();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getAvailabilityStatus());
        assertEquals("Available", result.get(0).getAvailabilityStatusName());
        verify(roomRepository).findByAvailabilityStatus(1);
    }

    @Test
    void testGetAvailableRooms_EmptyList() {
        // Given
        when(roomRepository.findByAvailabilityStatus(1)).thenReturn(new ArrayList<>());

        // When
        List<RoomResponseDTO> result = roomRestService.getAvailableRooms();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== UPDATE ROOM ====================

    @Test
    void testUpdateRoom_Success() {
        // Given
        when(roomRepository.findById(existingRoomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        RoomResponseDTO result = roomRestService.updateRoom(existingRoomId, updateRoomRequestDTO);

        // Then
        assertNotNull(result);
        verify(roomRepository).findById(existingRoomId);
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void testUpdateRoom_NotFound() {
        // Given
        when(roomRepository.findById(fakeRoomId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roomRestService.updateRoom(fakeRoomId, updateRoomRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Room not found"));
        verify(roomRepository).findById(fakeRoomId);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testUpdateRoom_WithMaintenance_SetsUnavailable() {
        // Given
        when(roomRepository.findById(existingRoomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room updated = invocation.getArgument(0);
            assertEquals(0, updated.getAvailabilityStatus()); // Should be unavailable
            assertNotNull(updated.getMaintenanceStart());
            assertNotNull(updated.getMaintenanceEnd());
            return updated;
        });

        // When
        roomRestService.updateRoom(existingRoomId, updateRoomRequestDTO);

        // Then
        verify(roomRepository).save(argThat(r -> 
            r.getAvailabilityStatus() == 0 &&
            r.getMaintenanceStart() != null &&
            r.getMaintenanceEnd() != null
        ));
    }

    @Test
    void testUpdateRoom_PartialUpdate() {
        // Given
        UpdateRoomRequestDTO partialUpdate = UpdateRoomRequestDTO.builder()
                .name("201-Partial")
                .build();

        when(roomRepository.findById(existingRoomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        RoomResponseDTO result = roomRestService.updateRoom(existingRoomId, partialUpdate);

        // Then
        assertNotNull(result);
        verify(roomRepository).save(argThat(r -> r.getName().equals("201-Partial")));
    }

    @Test
    void testUpdateRoom_ReplacesExistingMaintenance() {
        // Given
        room.setMaintenanceStart(LocalDateTime.of(2025, 11, 1, 0, 0));
        room.setMaintenanceEnd(LocalDateTime.of(2025, 11, 5, 23, 59));

        when(roomRepository.findById(existingRoomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room updated = invocation.getArgument(0);
            assertEquals(LocalDateTime.of(2025, 12, 1, 0, 0), updated.getMaintenanceStart());
            assertEquals(LocalDateTime.of(2025, 12, 5, 23, 59), updated.getMaintenanceEnd());
            return updated;
        });

        // When
        roomRestService.updateRoom(existingRoomId, updateRoomRequestDTO);

        // Then
        verify(roomRepository).save(any(Room.class));
    }

    // ==================== DELETE ROOM ====================

    @Test
    void testDeleteRoom_Success() {
        // Given
        when(roomRepository.existsById(existingRoomId)).thenReturn(true);
        doNothing().when(roomRepository).deleteById(existingRoomId);

        // When
        roomRestService.deleteRoom(existingRoomId);

        // Then
        verify(roomRepository).existsById(existingRoomId);
        verify(roomRepository).deleteById(existingRoomId);
    }

    @Test
    void testDeleteRoom_NotFound() {
        // Given
        when(roomRepository.existsById(fakeRoomId)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roomRestService.deleteRoom(fakeRoomId);
        });

        assertTrue(exception.getMessage().contains("Room not found"));
        verify(roomRepository).existsById(fakeRoomId);
        verify(roomRepository, never()).deleteById(anyString());
    }

    // ==================== HAS BOOKING CONFLICT ====================

    @Test
    void testHasBookingConflict_NoConflict() {
        // Given
        when(bookingRepository.findConflictingBookings(
                anyString(), 
                any(LocalDateTime.class), 
                any(LocalDateTime.class)
        )).thenReturn(new ArrayList<>());

        // When
        boolean result = roomRestService.hasBookingConflict(
                existingRoomId,
                "2025-12-01T00:00:00",
                "2025-12-05T23:59:59"
        );

        // Then
        assertFalse(result);
        verify(bookingRepository).findConflictingBookings(
                eq(existingRoomId),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    @Test
    void testHasBookingConflict_WithConflict() {
        // Given
        Booking conflictBooking = Booking.builder()
                .bookingID("BOOK-001-201-2025-11-06-20:37:12")
                .checkInDate(LocalDateTime.of(2025, 12, 2, 14, 0))
                .checkOutDate(LocalDateTime.of(2025, 12, 4, 12, 0))
                .room(room)
                .build();

        when(bookingRepository.findConflictingBookings(
                anyString(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(Arrays.asList(conflictBooking));

        // When
        boolean result = roomRestService.hasBookingConflict(
                existingRoomId,
                "2025-12-01T00:00:00",
                "2025-12-05T23:59:59"
        );

        // Then
        assertTrue(result);
    }

    @Test
    void testHasBookingConflict_InvalidDateFormat_ReturnsFalse() {
        // When
        boolean result = roomRestService.hasBookingConflict(
                existingRoomId,
                "invalid-date",
                "2025-12-05T23:59:59"
        );

        // Then
        assertFalse(result); // Should return false on parsing error
        verify(bookingRepository, never()).findConflictingBookings(anyString(), any(), any());
    }

    // ==================== GET ROOMS BY PROPERTY AND FLOOR ====================

    @Test
    void testGetRoomsByPropertyAndFloor_Found() {
        // Given
        List<RoomType> roomTypes = Arrays.asList(roomType);
        List<Room> rooms = Arrays.asList(room);

        when(roomTypeRepository.findByProperty_PropertyID("HOT-4000-001")).thenReturn(roomTypes);
        when(roomRepository.findByRoomType_RoomTypeID("RT-HOT-4000-001-001")).thenReturn(rooms);

        // When
        List<RoomResponseDTO> result = roomRestService.getRoomsByPropertyAndFloor("HOT-4000-001", 2);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getFloor());
        verify(roomTypeRepository).findByProperty_PropertyID("HOT-4000-001");
    }

    @Test
    void testGetRoomsByPropertyAndFloor_DifferentFloor_EmptyList() {
        // Given
        List<RoomType> roomTypes = Arrays.asList(roomType);
        when(roomTypeRepository.findByProperty_PropertyID("HOT-4000-001")).thenReturn(roomTypes);

        // When
        List<RoomResponseDTO> result = roomRestService.getRoomsByPropertyAndFloor("HOT-4000-001", 5);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Floor 5 doesn't exist
    }

    @Test
    void testGetRoomsByPropertyAndFloor_PropertyNotFound() {
        // Given
        when(roomTypeRepository.findByProperty_PropertyID("FAKE-PROPERTY")).thenReturn(new ArrayList<>());

        // When
        List<RoomResponseDTO> result = roomRestService.getRoomsByPropertyAndFloor("FAKE-PROPERTY", 2);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== AUTO UPDATE ROOM MAINTENANCE STATUS ====================

    @Test
    void testAutoUpdateRoomMaintenanceStatus_ClearsMaintenance() {
        // Given
        room.setMaintenanceStart(LocalDateTime.now().minusDays(10));
        room.setMaintenanceEnd(LocalDateTime.now().minusDays(1)); // Ended yesterday
        room.setAvailabilityStatus(0); // Unavailable

        List<Room> rooms = Arrays.asList(room);
        when(roomRepository.findAll()).thenReturn(rooms);
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room updated = invocation.getArgument(0);
            assertNull(updated.getMaintenanceStart());
            assertNull(updated.getMaintenanceEnd());
            assertEquals(1, updated.getAvailabilityStatus()); // Should be available
            return updated;
        });

        // When
        roomRestService.autoUpdateRoomMaintenanceStatus();

        // Then
        verify(roomRepository).save(argThat(r ->
                r.getMaintenanceStart() == null &&
                r.getMaintenanceEnd() == null &&
                r.getAvailabilityStatus() == 1
        ));
    }

    @Test
    void testAutoUpdateRoomMaintenanceStatus_NoUpdate_OngoingMaintenance() {
        // Given
        room.setMaintenanceStart(LocalDateTime.now().minusDays(1));
        room.setMaintenanceEnd(LocalDateTime.now().plusDays(5)); // Still ongoing
        room.setAvailabilityStatus(0);

        List<Room> rooms = Arrays.asList(room);
        when(roomRepository.findAll()).thenReturn(rooms);

        // When
        roomRestService.autoUpdateRoomMaintenanceStatus();

        // Then
        verify(roomRepository, never()).save(any(Room.class)); // Should not save
    }

    @Test
    void testAutoUpdateRoomMaintenanceStatus_NoMaintenance() {
        // Given
        room.setMaintenanceStart(null);
        room.setMaintenanceEnd(null);

        List<Room> rooms = Arrays.asList(room);
        when(roomRepository.findAll()).thenReturn(rooms);

        // When
        roomRestService.autoUpdateRoomMaintenanceStatus();

        // Then
        verify(roomRepository, never()).save(any(Room.class)); // No maintenance to update
    }

    @Test
    void testAutoUpdateRoomMaintenanceStatus_MultipleRooms() {
        // Given
        Room room1 = Room.builder()
                .roomID("HOT-4000-001-201")
                .maintenanceEnd(LocalDateTime.now().minusDays(1)) // Ended
                .availabilityStatus(0)
                .roomType(roomType)
                .build();

        Room room2 = Room.builder()
                .roomID("HOT-4000-001-202")
                .maintenanceEnd(LocalDateTime.now().plusDays(1)) // Ongoing
                .availabilityStatus(0)
                .roomType(roomType)
                .build();

        Room room3 = Room.builder()
                .roomID("HOT-4000-001-203")
                .maintenanceEnd(null) // No maintenance
                .availabilityStatus(1)
                .roomType(roomType)
                .build();

        when(roomRepository.findAll()).thenReturn(Arrays.asList(room1, room2, room3));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        roomRestService.autoUpdateRoomMaintenanceStatus();

        // Then
        verify(roomRepository, times(1)).save(any(Room.class)); // Only room1 should be updated
    }

    // ==================== HELPER METHODS TESTS ====================

    @Test
    void testAvailabilityStatusName_Available() {
        // Given
        room.setAvailabilityStatus(1);
        when(roomRepository.findById(existingRoomId)).thenReturn(Optional.of(room));

        // When
        RoomResponseDTO result = roomRestService.getRoomById(existingRoomId);

        // Then
        assertEquals("Available", result.getAvailabilityStatusName());
    }

    @Test
    void testAvailabilityStatusName_Unavailable() {
        // Given
        room.setAvailabilityStatus(0);
        when(roomRepository.findById(existingRoomId)).thenReturn(Optional.of(room));

        // When
        RoomResponseDTO result = roomRestService.getRoomById(existingRoomId);

        // Then
        assertEquals("Unavailable", result.getAvailabilityStatusName());
    }

    @Test
    void testActiveRoomName_Active() {
        // Given
        room.setActiveRoom(1);
        when(roomRepository.findById(existingRoomId)).thenReturn(Optional.of(room));

        // When
        RoomResponseDTO result = roomRestService.getRoomById(existingRoomId);

        // Then
        assertEquals("Active", result.getActiveRoomName());
    }

    @Test
    void testActiveRoomName_Inactive() {
        // Given
        room.setActiveRoom(0);
        when(roomRepository.findById(existingRoomId)).thenReturn(Optional.of(room));

        // When
        RoomResponseDTO result = roomRestService.getRoomById(existingRoomId);

        // Then
        assertEquals("Inactive", result.getActiveRoomName());
    }

    @Test
    void testRoomNameExtraction_WithHyphen() {
        // Given
        room.setRoomID("HOT-4000-001-204");
        when(roomRepository.findById("HOT-4000-001-204")).thenReturn(Optional.of(room));

        // When
        RoomResponseDTO result = roomRestService.getRoomById("HOT-4000-001-204");

        // Then
        assertEquals("204", result.getName()); // Should extract last part
    }

    @Test
    void testRoomNameExtraction_WithoutHyphen() {
        // Given
        room.setRoomID("ROOM101");
        when(roomRepository.findById("ROOM101")).thenReturn(Optional.of(room));

        // When
        RoomResponseDTO result = roomRestService.getRoomById("ROOM101");

        // Then
        assertEquals("ROOM101", result.getName()); // Should use full ID as fallback
    }
}