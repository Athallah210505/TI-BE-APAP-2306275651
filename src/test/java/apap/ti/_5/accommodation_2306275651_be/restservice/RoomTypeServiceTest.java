package apap.ti._5.accommodation_2306275651_be.restservice;

import apap.ti._5.accommodation_2306275651_be.model.Property;
import apap.ti._5.accommodation_2306275651_be.model.RoomType;
import apap.ti._5.accommodation_2306275651_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306275651_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306275651_be.restdto.request.roomtype.CreateRoomTypeRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.roomtype.UpdateRoomTypeRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.roomtype.RoomTypeResponseDTO;

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

class RoomTypeServiceTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private RoomTypeRestServiceImpl roomTypeRestService;

    private String existingRoomTypeId;
    private String fakeRoomTypeId;
    private RoomType roomType;
    private Property property;
    private CreateRoomTypeRequestDTO createRoomTypeRequestDTO;
    private UpdateRoomTypeRequestDTO updateRoomTypeRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingRoomTypeId = "001–Single_Room–2";
        fakeRoomTypeId = "999–Fake_Room–9";

        // ✅ Setup Property
        property = Property.builder()
                .propertyID("HOT-4000-001")
                .propertyName("Grand Hotel Jakarta")
                .type(1)
                .activeStatus(1)
                .build();

        // ✅ Setup RoomType
        roomType = RoomType.builder()
                .roomTypeID(existingRoomTypeId)
                .name("Single Room")
                .price(300000)
                .capacity(3)
                .facility("TV, AC, WiFi, Desk")
                .floor(2)
                .description("Comfortable single room for solo travelers")
                .property(property)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        // ✅ Setup CreateRoomTypeRequestDTO (gunakan field yang BENAR)
        createRoomTypeRequestDTO = CreateRoomTypeRequestDTO.builder()
                .propertyID("HOT-4000-001")
                .name("Double Room") // ✅ BUKAN roomTypeName
                .price(500000)
                .capacity(5)
                .facility("TV, AC, WiFi, Mini Bar, Balcony")
                .floor(3)
                .description("Spacious double room for couples or families") // ✅ BUKAN roomTypeDescription
                .build();

        // // ✅ Setup UpdateRoomTypeRequestDTO (gunakan field yang BENAR)
        // updateRoomTypeRequestDTO = UpdateRoomTypeRequestDTO.builder()
        //         .name("Single Room - Updated") // ✅ BUKAN roomTypeName
        //         .price(350000)
        //         .capacity(4)
        //         .facility("TV, AC, WiFi, Desk, Mini Fridge")
        //         .floor(2)
        //         .description("Updated comfortable single room") // ✅ BUKAN roomTypeDescription
        //         .build();
    }

    // ==================== CREATE ROOM TYPE ====================

    @Test
    void testCreateRoomType_Success() {
        // Given
        when(propertyRepository.findById("HOT-4000-001")).thenReturn(Optional.of(property));
        when(roomTypeRepository.findByProperty_PropertyID("HOT-4000-001")).thenReturn(new ArrayList<>());
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(roomType);

        // When
        RoomTypeResponseDTO result = roomTypeRestService.createRoomType(createRoomTypeRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals("Single Room", result.getName());
        assertEquals(300000, result.getPrice());
        assertEquals(3, result.getCapacity());
        assertEquals(2, result.getFloor());
        verify(propertyRepository).findById("HOT-4000-001");
        verify(roomTypeRepository).save(any(RoomType.class));
    }

    @Test
    void testCreateRoomType_PropertyNotFound() {
        // Given
        when(propertyRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roomTypeRestService.createRoomType(createRoomTypeRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Property not found"));
        verify(propertyRepository).findById("HOT-4000-001");
        verify(roomTypeRepository, never()).save(any(RoomType.class));
    }

    @Test
    void testCreateRoomType_GeneratesCorrectID() {
        // Given
        when(propertyRepository.findById("HOT-4000-001")).thenReturn(Optional.of(property));
        when(roomTypeRepository.findByProperty_PropertyID("HOT-4000-001")).thenReturn(new ArrayList<>());
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> {
            RoomType saved = invocation.getArgument(0);
            // Format: 001–Double_Room–3
            assertTrue(saved.getRoomTypeID().contains("001–Double_Room–3"));
            return saved;
        });

        // When
        roomTypeRestService.createRoomType(createRoomTypeRequestDTO);

        // Then
        verify(roomTypeRepository).save(argThat(rt -> rt.getRoomTypeID().contains("Double_Room")));
    }

    @Test
    void testCreateRoomType_FirstRoomType() {
        // Given
        when(propertyRepository.findById("HOT-4000-001")).thenReturn(Optional.of(property));
        when(roomTypeRepository.findByProperty_PropertyID("HOT-4000-001"))
                .thenReturn(new ArrayList<>()); // No existing room types
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> {
            RoomType saved = invocation.getArgument(0);
            assertTrue(saved.getRoomTypeID().startsWith("001–")); // Should use property counter
            return saved;
        });

        // When
        roomTypeRestService.createRoomType(createRoomTypeRequestDTO);

        // Then
        verify(roomTypeRepository).save(any(RoomType.class));
    }

    // ==================== GET ROOM TYPE BY ID ====================

    @Test
    void testGetRoomTypeById_Found() {
        // Given
        when(roomTypeRepository.findById(existingRoomTypeId)).thenReturn(Optional.of(roomType));

        // When
        RoomTypeResponseDTO result = roomTypeRestService.getRoomTypeById(existingRoomTypeId);

        // Then
        assertNotNull(result);
        assertEquals(existingRoomTypeId, result.getRoomTypeID());
        assertEquals("Single Room", result.getName());
        assertEquals(300000, result.getPrice());
        assertEquals(3, result.getCapacity());
        assertEquals("TV, AC, WiFi, Desk", result.getFacility());
        assertEquals(2, result.getFloor());
        verify(roomTypeRepository).findById(existingRoomTypeId);
    }

    @Test
    void testGetRoomTypeById_NotFound() {
        // Given
        when(roomTypeRepository.findById(fakeRoomTypeId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roomTypeRestService.getRoomTypeById(fakeRoomTypeId);
        });

        assertTrue(exception.getMessage().contains("Room type not found"));
        verify(roomTypeRepository).findById(fakeRoomTypeId);
    }

    // ==================== GET ALL ROOM TYPES ====================

    @Test
    void testGetAllRoomTypes_Success() {
        // Given
        List<RoomType> roomTypes = Arrays.asList(roomType);
        when(roomTypeRepository.findAll()).thenReturn(roomTypes);

        // When
        List<RoomTypeResponseDTO> result = roomTypeRestService.getAllRoomTypes();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(existingRoomTypeId, result.get(0).getRoomTypeID());
        assertEquals("Single Room", result.get(0).getName());
        verify(roomTypeRepository).findAll();
    }

    @Test
    void testGetAllRoomTypes_EmptyList() {
        // Given
        when(roomTypeRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        List<RoomTypeResponseDTO> result = roomTypeRestService.getAllRoomTypes();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roomTypeRepository).findAll();
    }

    @Test
    void testGetAllRoomTypes_MultipleRoomTypes() {
        // Given
        RoomType roomType2 = RoomType.builder()
                .roomTypeID("001–Double_Room–3")
                .name("Double Room")
                .price(500000)
                .capacity(5)
                .floor(3)
                .property(property)
                .build();

        List<RoomType> roomTypes = Arrays.asList(roomType, roomType2);
        when(roomTypeRepository.findAll()).thenReturn(roomTypes);

        // When
        List<RoomTypeResponseDTO> result = roomTypeRestService.getAllRoomTypes();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ==================== GET ROOM TYPES BY PROPERTY ====================

    @Test
    void testGetRoomTypesByProperty_Found() {
        // Given
        List<RoomType> roomTypes = Arrays.asList(roomType);
        when(roomTypeRepository.findByProperty_PropertyID("HOT-4000-001")).thenReturn(roomTypes);

        // When
        List<RoomTypeResponseDTO> result = roomTypeRestService.getRoomTypesByProperty("HOT-4000-001");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("HOT-4000-001", result.get(0).getPropertyID());
        assertEquals("Single Room", result.get(0).getName());
        verify(roomTypeRepository).findByProperty_PropertyID("HOT-4000-001");
    }

    @Test
    void testGetRoomTypesByProperty_NotFound() {
        // Given
        when(roomTypeRepository.findByProperty_PropertyID("FAKE-PROPERTY")).thenReturn(new ArrayList<>());

        // When
        List<RoomTypeResponseDTO> result = roomTypeRestService.getRoomTypesByProperty("FAKE-PROPERTY");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRoomTypesByProperty_MultipleRoomTypes() {
        // Given
        RoomType roomType2 = RoomType.builder()
                .roomTypeID("001–Double_Room–3")
                .name("Double Room")
                .price(500000)
                .floor(3)
                .property(property)
                .build();

        RoomType roomType3 = RoomType.builder()
                .roomTypeID("001–Suite–5")
                .name("Suite")
                .price(1000000)
                .floor(5)
                .property(property)
                .build();

        when(roomTypeRepository.findByProperty_PropertyID("HOT-4000-001"))
                .thenReturn(Arrays.asList(roomType, roomType2, roomType3));

        // When
        List<RoomTypeResponseDTO> result = roomTypeRestService.getRoomTypesByProperty("HOT-4000-001");

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    // ==================== UPDATE ROOM TYPE ====================

    // @Test
    // void testUpdateRoomType_Success() {
    //     // Given
    //     when(roomTypeRepository.findById(existingRoomTypeId)).thenReturn(Optional.of(roomType));
    //     when(roomTypeRepository.findByProperty_PropertyID(anyString())).thenReturn(new ArrayList<>());
    //     when(roomTypeRepository.save(any(RoomType.class))).thenReturn(roomType);

    //     // When
    //     RoomTypeResponseDTO result = roomTypeRestService.updateRoomType(existingRoomTypeId, updateRoomTypeRequestDTO);

    //     // Then
    //     assertNotNull(result);
    //     verify(roomTypeRepository).findById(existingRoomTypeId);
    //     verify(roomTypeRepository).save(any(RoomType.class));
    // }

    @Test
    void testUpdateRoomType_NotFound() {
        // Given
        when(roomTypeRepository.findById(fakeRoomTypeId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roomTypeRestService.updateRoomType(fakeRoomTypeId, updateRoomTypeRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Room type not found"));
        verify(roomTypeRepository).findById(fakeRoomTypeId);
        verify(roomTypeRepository, never()).save(any(RoomType.class));
    }

    // @Test
    // void testUpdateRoomType_PartialUpdate() {
    //     // Given
    //     UpdateRoomTypeRequestDTO partialUpdate = UpdateRoomTypeRequestDTO.builder()
    //             .capacity("4") // ✅ Gunakan field yang benar
    //             .build();

    //     when(roomTypeRepository.findById(existingRoomTypeId)).thenReturn(Optional.of(roomType));
    //     when(roomTypeRepository.findByProperty_PropertyID(anyString())).thenReturn(new ArrayList<>());
    //     when(roomTypeRepository.save(any(RoomType.class))).thenReturn(roomType);

    //     // When
    //     RoomTypeResponseDTO result = roomTypeRestService.updateRoomType(existingRoomTypeId, partialUpdate);

    //     // Then
    //     assertNotNull(result);
    //     verify(roomTypeRepository).save(argThat(rt -> rt.getName().equals("Single Room - Partial")));
    // }

    // @Test
    // void testUpdateRoomType_AllFields() {
    //     // Given
    //     when(roomTypeRepository.findById(existingRoomTypeId)).thenReturn(Optional.of(roomType));
    //     when(roomTypeRepository.findByProperty_PropertyID(anyString())).thenReturn(new ArrayList<>());
    //     when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> {
    //         RoomType updated = invocation.getArgument(0);
    //         assertEquals("Single Room - Updated", updated.getName());
    //         assertEquals(350000, updated.getPrice());
    //         assertEquals(4, updated.getCapacity());
    //         assertEquals("TV, AC, WiFi, Desk, Mini Fridge", updated.getFacility());
    //         assertEquals(2, updated.getFloor());
    //         return updated;
    //     });

    //     // When
    //     roomTypeRestService.updateRoomType(existingRoomTypeId, updateRoomTypeRequestDTO);

    //     // Then
    //     verify(roomTypeRepository).save(any(RoomType.class));
    // }

    // ==================== DELETE ROOM TYPE ====================

    @Test
    void testDeleteRoomType_Success() {
        // Given
        when(roomTypeRepository.existsById(existingRoomTypeId)).thenReturn(true);
        doNothing().when(roomTypeRepository).deleteById(existingRoomTypeId);

        // When
        roomTypeRestService.deleteRoomType(existingRoomTypeId);

        // Then
        verify(roomTypeRepository).existsById(existingRoomTypeId);
        verify(roomTypeRepository).deleteById(existingRoomTypeId);
    }

    @Test
    void testDeleteRoomType_NotFound() {
        // Given
        when(roomTypeRepository.existsById(fakeRoomTypeId)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roomTypeRestService.deleteRoomType(fakeRoomTypeId);
        });

        assertTrue(exception.getMessage().contains("Room type not found"));
        verify(roomTypeRepository).existsById(fakeRoomTypeId);
        verify(roomTypeRepository, never()).deleteById(anyString());
    }

    // ==================== IS DUPLICATE ROOM TYPE FLOOR ====================

    @Test
    void testIsDuplicateRoomTypeFloor_NoDuplicate() {
        // Given
        when(roomTypeRepository.findByProperty_PropertyID("HOT-4000-001"))
                .thenReturn(new ArrayList<>());

        // When
        boolean result = roomTypeRestService.isDuplicateRoomTypeFloor("HOT-4000-001", "New Room Type", 4);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsDuplicateRoomTypeFloor_HasDuplicate() {
        // Given
        when(roomTypeRepository.findByProperty_PropertyID("HOT-4000-001"))
                .thenReturn(Arrays.asList(roomType));

        // When
        boolean result = roomTypeRestService.isDuplicateRoomTypeFloor("HOT-4000-001", "Single Room", 2);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsDuplicateRoomTypeFloor_DifferentFloor_NoDuplicate() {
        // Given
        when(roomTypeRepository.findByProperty_PropertyID("HOT-4000-001"))
                .thenReturn(Arrays.asList(roomType));

        // When
        boolean result = roomTypeRestService.isDuplicateRoomTypeFloor("HOT-4000-001", "Single Room", 5);

        // Then
        assertFalse(result); // Same name, different floor = OK
    }

    @Test
    void testIsDuplicateRoomTypeFloor_DifferentName_NoDuplicate() {
        // Given
        when(roomTypeRepository.findByProperty_PropertyID("HOT-4000-001"))
                .thenReturn(Arrays.asList(roomType));

        // When
        boolean result = roomTypeRestService.isDuplicateRoomTypeFloor("HOT-4000-001", "Different Room", 2);

        // Then
        assertFalse(result); // Different name, same floor = OK
    }

    @Test
    void testIsDuplicateRoomTypeFloor_NullFloor_ReturnsFalse() {
        // When
        boolean result = roomTypeRestService.isDuplicateRoomTypeFloor("HOT-4000-001", "Single Room", null);

        // Then
        assertFalse(result);
        verify(roomTypeRepository, never()).findByProperty_PropertyID(anyString());
    }

    // ==================== HELPER METHODS TESTS ====================

    @Test
    void testConvertToResponseDTO_AllFieldsPresent() {
        // Given
        when(roomTypeRepository.findById(existingRoomTypeId)).thenReturn(Optional.of(roomType));

        // When
        RoomTypeResponseDTO result = roomTypeRestService.getRoomTypeById(existingRoomTypeId);

        // Then
        assertEquals(existingRoomTypeId, result.getRoomTypeID());
        assertEquals("Single Room", result.getName());
        assertEquals(300000, result.getPrice());
        assertEquals(3, result.getCapacity());
        assertEquals("TV, AC, WiFi, Desk", result.getFacility());
        assertEquals(2, result.getFloor());
        assertEquals("Comfortable single room for solo travelers", result.getDescription());
        assertEquals("HOT-4000-001", result.getPropertyID());
        assertEquals("Grand Hotel Jakarta", result.getPropertyName());
    }

    @Test
    void testConvertToResponseDTO_NullDescription() {
        // Given
        roomType.setDescription(null);
        when(roomTypeRepository.findById(existingRoomTypeId)).thenReturn(Optional.of(roomType));

        // When
        RoomTypeResponseDTO result = roomTypeRestService.getRoomTypeById(existingRoomTypeId);

        // Then
        assertNull(result.getDescription());
    }

    @Test
    void testRoomTypeIDGeneration_CorrectFormat() {
        // Given
        when(propertyRepository.findById("HOT-4000-001")).thenReturn(Optional.of(property));
        when(roomTypeRepository.findByProperty_PropertyID("HOT-4000-001")).thenReturn(new ArrayList<>());
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> {
            RoomType saved = invocation.getArgument(0);
            // Format: 001–Double_Room–3
            String[] parts = saved.getRoomTypeID().split("–");
            assertEquals(3, parts.length);
            assertEquals("001", parts[0]); // Property counter
            assertEquals("Double_Room", parts[1]); // Room type name with underscore
            assertEquals("3", parts[2]); // Floor
            return saved;
        });

        // When
        roomTypeRestService.createRoomType(createRoomTypeRequestDTO);

        // Then
        verify(roomTypeRepository).save(any(RoomType.class));
    }

    @Test
    void testRoomTypeIDGeneration_VillaProperty() {
        // Given
        Property villaProperty = Property.builder()
                .propertyID("VIL-5000-002")
                .propertyName("Sunset Villa Bali")
                .type(2)
                .build();

        createRoomTypeRequestDTO.setPropertyID("VIL-5000-002");

        when(propertyRepository.findById("VIL-5000-002")).thenReturn(Optional.of(villaProperty));
        when(roomTypeRepository.findByProperty_PropertyID("VIL-5000-002")).thenReturn(new ArrayList<>());
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> {
            RoomType saved = invocation.getArgument(0);
            assertTrue(saved.getRoomTypeID().startsWith("002–")); // Villa counter
            return saved;
        });

        // When
        roomTypeRestService.createRoomType(createRoomTypeRequestDTO);

        // Then
        verify(roomTypeRepository).save(argThat(rt -> rt.getRoomTypeID().startsWith("002–")));
    }

    @Test
    void testRoomTypeIDGeneration_ApartmentProperty() {
        // Given
        Property apartmentProperty = Property.builder()
                .propertyID("APT-6000-003")
                .propertyName("Modern Apartment Jakarta")
                .type(3)
                .build();

        createRoomTypeRequestDTO.setPropertyID("APT-6000-003");

        when(propertyRepository.findById("APT-6000-003")).thenReturn(Optional.of(apartmentProperty));
        when(roomTypeRepository.findByProperty_PropertyID("APT-6000-003")).thenReturn(new ArrayList<>());
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> {
            RoomType saved = invocation.getArgument(0);
            assertTrue(saved.getRoomTypeID().startsWith("003–")); // Apartment counter
            return saved;
        });

        // When
        roomTypeRestService.createRoomType(createRoomTypeRequestDTO);

        // Then
        verify(roomTypeRepository).save(argThat(rt -> rt.getRoomTypeID().startsWith("003–")));
    }
}