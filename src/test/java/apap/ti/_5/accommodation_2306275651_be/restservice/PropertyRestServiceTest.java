package apap.ti._5.accommodation_2306275651_be.restservice;

import apap.ti._5.accommodation_2306275651_be.model.Property;
import apap.ti._5.accommodation_2306275651_be.repository.BookingRepository;
import apap.ti._5.accommodation_2306275651_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306275651_be.restdto.request.property.CreatePropertyRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.property.UpdatePropertyRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.room.CreateRoomRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.property.PropertyResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.property.RoomTypeInfoDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.room.RoomResponseDTO;
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

class PropertyRestServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private RoomTypeRestService roomTypeRestService;

    @Mock
    private RoomRestService roomRestService;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PropertyRestServiceImpl propertyRestService;

    private String existingPropertyId;
    private String fakePropertyId;
    private UUID ownerId;
    private Property property;
    private PropertyResponseDTO propertyResponseDTO;
    private CreatePropertyRequestDTO createPropertyRequestDTO;
    private UpdatePropertyRequestDTO updatePropertyRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingPropertyId = "HOT-4000-001";
        fakePropertyId = "HOT-9999-999";
        ownerId = UUID.randomUUID();

        // ✅ Setup Property entity
        property = Property.builder()
                .propertyID(existingPropertyId)
                .propertyName("Grand Hotel Jakarta")
                .type(1) // Hotel
                .address("Jl. Sudirman No. 123, Jakarta Pusat")
                .province(31) // Jakarta
                .description("Luxury 5-star hotel in the heart of Jakarta")
                .totalRoom(17)
                .activeStatus(1)
                .income(0)
                .ownerName("John Doe")
                .ownerID(ownerId)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        // ✅ Setup PropertyResponseDTO
        propertyResponseDTO = PropertyResponseDTO.builder()
                .propertyID(existingPropertyId)
                .propertyName("Grand Hotel Jakarta")
                .type(1)
                .typeName("Hotel")
                .address("Jl. Sudirman No. 123, Jakarta Pusat")
                .province(31)
                .description("Luxury 5-star hotel in the heart of Jakarta")
                .totalRoom(17)
                .activeStatus(1)
                .activeStatusName("Active")
                .income(0)
                .ownerName("John Doe")
                .ownerID(ownerId.toString())
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .roomTypes(new ArrayList<>())
                .build();

        // ✅ Setup CreatePropertyRequestDTO
        CreateRoomRequestDTO singleRoomType = CreateRoomRequestDTO.builder()
                .roomTypeName("Single Room")
                .price(300000)
                .roomTypeDescription("Comfortable single room for solo travelers")
                .capacity(3)
                .facility("TV, AC, WiFi, Desk")
                .floor(2)
                .unit(6)
                .build();

        CreateRoomRequestDTO doubleRoomType = CreateRoomRequestDTO.builder()
                .roomTypeName("Double Room")
                .price(500000)
                .roomTypeDescription("Spacious double room for couples or families")
                .capacity(5)
                .facility("TV, AC, WiFi, Mini Bar, Balcony")
                .floor(3)
                .unit(11)
                .build();

        createPropertyRequestDTO = CreatePropertyRequestDTO.builder()
                .propertyName("Grand Hotel Jakarta")
                .type(1)
                .address("Jl. Sudirman No. 123, Jakarta Pusat")
                .province(31)
                .description("Luxury 5-star hotel in the heart of Jakarta")
                .totalRoom(17) // 6 single + 11 double
                .ownerName("John Doe")
                .ownerID(ownerId.toString())
                .roomTypes(Arrays.asList(singleRoomType, doubleRoomType))
                .build();

        // ✅ Setup UpdatePropertyRequestDTO
        updatePropertyRequestDTO = UpdatePropertyRequestDTO.builder()
                .propertyName("Grand Hotel Jakarta - Updated")
                .type(1)
                .address("Jl. Sudirman No. 123, Jakarta Pusat - Updated")
                .province(31)
                .description("Updated description")
                .totalRoom(17)
                .activeStatus(1)
                .roomTypes(new ArrayList<>())
                .build();
    }

    // ==================== GET ALL PROPERTIES ====================

    @Test
    void testGetAllProperties_Success() {
        // Given
        List<Property> properties = Arrays.asList(property);
        when(propertyRepository.findAll()).thenReturn(properties);

        // When
        List<PropertyResponseDTO> result = propertyRestService.getAllProperties();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(existingPropertyId, result.get(0).getPropertyID());
        assertEquals("Grand Hotel Jakarta", result.get(0).getPropertyName());
        verify(propertyRepository).findAll();
    }

    @Test
    void testGetAllProperties_EmptyList() {
        // Given
        when(propertyRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        List<PropertyResponseDTO> result = propertyRestService.getAllProperties();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(propertyRepository).findAll();
    }

    // ==================== GET PROPERTY BY ID ====================

    @Test
    void testGetPropertyById_Found() {
        // Given
        when(propertyRepository.findById(existingPropertyId)).thenReturn(Optional.of(property));
        when(roomTypeRestService.getRoomTypesByProperty(existingPropertyId)).thenReturn(new ArrayList<>());

        // When
        PropertyResponseDTO result = propertyRestService.getPropertyById(existingPropertyId);

        // Then
        assertNotNull(result);
        assertEquals(existingPropertyId, result.getPropertyID());
        assertEquals("Grand Hotel Jakarta", result.getPropertyName());
        assertEquals(1, result.getType());
        assertEquals("Hotel", result.getTypeName());
        verify(propertyRepository).findById(existingPropertyId);
    }

    @Test
    void testGetPropertyById_NotFound() {
        // Given
        when(propertyRepository.findById(fakePropertyId)).thenReturn(Optional.empty());

        // When
        PropertyResponseDTO result = propertyRestService.getPropertyById(fakePropertyId);

        // Then
        assertNull(result);
        verify(propertyRepository).findById(fakePropertyId);
        verify(roomTypeRestService, never()).getRoomTypesByProperty(anyString());
    }

    @Test
    void testGetPropertyById_WithRoomTypes() {
        // Given
        RoomTypeResponseDTO roomType = RoomTypeResponseDTO.builder()
                .roomTypeID("RT-001")
                .name("Single Room")
                .price(300000)
                .capacity(3)
                .floor(2)
                .facility("TV, AC, WiFi")
                .description("Single room")
                .build();

        RoomResponseDTO room = RoomResponseDTO.builder()
                .roomID("HOT-4000-001-201")
                .name("201")
                .availabilityStatus(1)
                .activeRoom(1)
                .build();

        when(propertyRepository.findById(existingPropertyId)).thenReturn(Optional.of(property));
        when(roomTypeRestService.getRoomTypesByProperty(existingPropertyId))
                .thenReturn(Arrays.asList(roomType));
        when(roomRestService.getRoomsByRoomType("RT-001"))
                .thenReturn(Arrays.asList(room));

        // When
        PropertyResponseDTO result = propertyRestService.getPropertyById(existingPropertyId);

        // Then
        assertNotNull(result);
        assertNotNull(result.getRoomTypes());
        assertEquals(1, result.getRoomTypes().size());
        assertEquals("Single Room", result.getRoomTypes().get(0).getRoomTypeName());
        assertEquals(1, result.getRoomTypes().get(0).getListRoom().size());
        verify(roomTypeRestService).getRoomTypesByProperty(existingPropertyId);
        verify(roomRestService).getRoomsByRoomType("RT-001");
    }

    // ==================== GET PROPERTIES BY OWNER ====================

    @Test
    void testGetPropertiesByOwner_Found() {
        // Given
        List<Property> properties = Arrays.asList(property);
        when(propertyRepository.findByOwnerID(ownerId)).thenReturn(properties);

        // When
        List<PropertyResponseDTO> result = propertyRestService.getPropertiesByOwner(ownerId.toString());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ownerId.toString(), result.get(0).getOwnerID());
        verify(propertyRepository).findByOwnerID(ownerId);
    }

    @Test
    void testGetPropertiesByOwner_NotFound() {
        // Given
        UUID fakeOwnerId = UUID.randomUUID();
        when(propertyRepository.findByOwnerID(fakeOwnerId)).thenReturn(new ArrayList<>());

        // When
        List<PropertyResponseDTO> result = propertyRestService.getPropertiesByOwner(fakeOwnerId.toString());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(propertyRepository).findByOwnerID(fakeOwnerId);
    }

    // ==================== UPDATE PROPERTY ====================

    @Test
    void testUpdateProperty_Success() {
        // Given
        when(propertyRepository.findById(existingPropertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenReturn(property);
        when(roomTypeRestService.getRoomTypesByProperty(existingPropertyId)).thenReturn(new ArrayList<>());

        // When
        PropertyResponseDTO result = propertyRestService.updateProperty(existingPropertyId, updatePropertyRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals(existingPropertyId, result.getPropertyID());
        verify(propertyRepository).findById(existingPropertyId);
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void testUpdateProperty_NotFound() {
        // Given
        when(propertyRepository.findById(fakePropertyId)).thenReturn(Optional.empty());

        // When
        PropertyResponseDTO result = propertyRestService.updateProperty(fakePropertyId, updatePropertyRequestDTO);

        // Then
        assertNull(result);
        verify(propertyRepository).findById(fakePropertyId);
        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    void testUpdateProperty_PartialUpdate() {
        // Given
        UpdatePropertyRequestDTO partialUpdate = UpdatePropertyRequestDTO.builder()
                .propertyName("Updated Name Only")
                .build();

        when(propertyRepository.findById(existingPropertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenReturn(property);
        when(roomTypeRestService.getRoomTypesByProperty(existingPropertyId)).thenReturn(new ArrayList<>());

        // When
        PropertyResponseDTO result = propertyRestService.updateProperty(existingPropertyId, partialUpdate);

        // Then
        assertNotNull(result);
        verify(propertyRepository).save(argThat(prop -> 
            prop.getPropertyName().equals("Updated Name Only")
        ));
    }

    // ==================== DELETE PROPERTY ====================

    @Test
    void testDeleteProperty_Success_NoActiveBookings() {
        // Given
        when(propertyRepository.findById(existingPropertyId)).thenReturn(Optional.of(property));
        when(bookingRepository.countActiveBookingsByPropertyID(existingPropertyId)).thenReturn(0L);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        // When
        PropertyResponseDTO result = propertyRestService.deleteProperty(existingPropertyId);

        // Then
        assertNotNull(result);
        assertEquals(existingPropertyId, result.getPropertyID());
        verify(bookingRepository).countActiveBookingsByPropertyID(existingPropertyId);
        verify(propertyRepository).save(argThat(prop -> prop.getActiveStatus() == 0));
    }

    @Test
    void testDeleteProperty_NotFound() {
        // Given
        when(propertyRepository.findById(fakePropertyId)).thenReturn(Optional.empty());

        // When
        PropertyResponseDTO result = propertyRestService.deleteProperty(fakePropertyId);

        // Then
        assertNull(result);
        verify(propertyRepository).findById(fakePropertyId);
        verify(bookingRepository, never()).countActiveBookingsByPropertyID(anyString());
    }

    @Test
    void testDeleteProperty_FailWithActiveBookings() {
        // Given
        when(propertyRepository.findById(existingPropertyId)).thenReturn(Optional.of(property));
        when(bookingRepository.countActiveBookingsByPropertyID(existingPropertyId)).thenReturn(5L);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            propertyRestService.deleteProperty(existingPropertyId);
        });

        assertTrue(exception.getMessage().contains("booking aktif"));
        assertTrue(exception.getMessage().contains("5"));
        verify(bookingRepository).countActiveBookingsByPropertyID(existingPropertyId);
        verify(propertyRepository, never()).save(any(Property.class));
    }

    // ==================== HELPER METHODS TESTS ====================

    @Test
    void testTypePrefixGeneration_Hotel() {
        // Given
        createPropertyRequestDTO.setType(1);

        // When
        when(propertyRepository.count()).thenReturn(0L);
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> {
            Property prop = invocation.getArgument(0);
            assertTrue(prop.getPropertyID().startsWith("HOT-"));
            return prop;
        });
        when(roomTypeRestService.createRoomType(any())).thenReturn(
            RoomTypeResponseDTO.builder().roomTypeID("RT-001").build()
        );
        when(roomRestService.createRoom(any())).thenReturn(
            RoomResponseDTO.builder().roomID("ROOM-001").build()
        );

        // Then
        propertyRestService.createProperty(createPropertyRequestDTO);
        verify(propertyRepository).save(argThat(prop -> prop.getPropertyID().startsWith("HOT-")));
    }

    @Test
    void testTypePrefixGeneration_Villa() {
        // Given
        createPropertyRequestDTO.setType(2);

        // When
        when(propertyRepository.count()).thenReturn(0L);
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> {
            Property prop = invocation.getArgument(0);
            assertTrue(prop.getPropertyID().startsWith("VIL-"));
            return prop;
        });
        when(roomTypeRestService.createRoomType(any())).thenReturn(
            RoomTypeResponseDTO.builder().roomTypeID("RT-001").build()
        );
        when(roomRestService.createRoom(any())).thenReturn(
            RoomResponseDTO.builder().roomID("ROOM-001").build()
        );

        // Then
        propertyRestService.createProperty(createPropertyRequestDTO);
        verify(propertyRepository).save(argThat(prop -> prop.getPropertyID().startsWith("VIL-")));
    }

    @Test
    void testTypePrefixGeneration_Apartment() {
        // Given
        createPropertyRequestDTO.setType(3);

        // When
        when(propertyRepository.count()).thenReturn(0L);
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> {
            Property prop = invocation.getArgument(0);
            assertTrue(prop.getPropertyID().startsWith("APT-"));
            return prop;
        });
        when(roomTypeRestService.createRoomType(any())).thenReturn(
            RoomTypeResponseDTO.builder().roomTypeID("RT-001").build()
        );
        when(roomRestService.createRoom(any())).thenReturn(
            RoomResponseDTO.builder().roomID("ROOM-001").build()
        );

        // Then
        propertyRestService.createProperty(createPropertyRequestDTO);
        verify(propertyRepository).save(argThat(prop -> prop.getPropertyID().startsWith("APT-")));
    }

    @Test
    void testTypeNameConversion_Hotel() {
        // Given
        when(propertyRepository.findById(existingPropertyId)).thenReturn(Optional.of(property));
        when(roomTypeRestService.getRoomTypesByProperty(existingPropertyId)).thenReturn(new ArrayList<>());

        // When
        PropertyResponseDTO result = propertyRestService.getPropertyById(existingPropertyId);

        // Then
        assertEquals("Hotel", result.getTypeName());
    }

    @Test
    void testTypeNameConversion_Villa() {
        // Given
        property.setType(2);
        when(propertyRepository.findById(existingPropertyId)).thenReturn(Optional.of(property));
        when(roomTypeRestService.getRoomTypesByProperty(existingPropertyId)).thenReturn(new ArrayList<>());

        // When
        PropertyResponseDTO result = propertyRestService.getPropertyById(existingPropertyId);

        // Then
        assertEquals("Villa", result.getTypeName());
    }

    @Test
    void testTypeNameConversion_Apartment() {
        // Given
        property.setType(3);
        when(propertyRepository.findById(existingPropertyId)).thenReturn(Optional.of(property));
        when(roomTypeRestService.getRoomTypesByProperty(existingPropertyId)).thenReturn(new ArrayList<>());

        // When
        PropertyResponseDTO result = propertyRestService.getPropertyById(existingPropertyId);

        // Then
        assertEquals("Apartemen", result.getTypeName());
    }

    @Test
    void testActiveStatusConversion_Active() {
        // Given
        property.setActiveStatus(1);
        when(propertyRepository.findById(existingPropertyId)).thenReturn(Optional.of(property));
        when(roomTypeRestService.getRoomTypesByProperty(existingPropertyId)).thenReturn(new ArrayList<>());

        // When
        PropertyResponseDTO result = propertyRestService.getPropertyById(existingPropertyId);

        // Then
        assertEquals("Active", result.getActiveStatusName());
    }

    @Test
    void testActiveStatusConversion_Inactive() {
        // Given
        property.setActiveStatus(0);
        when(propertyRepository.findById(existingPropertyId)).thenReturn(Optional.of(property));
        when(roomTypeRestService.getRoomTypesByProperty(existingPropertyId)).thenReturn(new ArrayList<>());

        // When
        PropertyResponseDTO result = propertyRestService.getPropertyById(existingPropertyId);

        // Then
        assertEquals("Non-Active", result.getActiveStatusName());
    }
}