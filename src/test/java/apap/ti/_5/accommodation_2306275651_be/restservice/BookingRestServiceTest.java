package apap.ti._5.accommodation_2306275651_be.restservice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import apap.ti._5.accommodation_2306275651_be.model.Booking;
import apap.ti._5.accommodation_2306275651_be.model.Property;
import apap.ti._5.accommodation_2306275651_be.model.Room;
import apap.ti._5.accommodation_2306275651_be.model.RoomType;
import apap.ti._5.accommodation_2306275651_be.repository.BookingRepository;
import apap.ti._5.accommodation_2306275651_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306275651_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306275651_be.restdto.request.booking.CreateBookingRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.booking.UpdateBookingRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.booking.UpdateBookingStatusRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.booking.BookingChartResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.booking.BookingResponseDTO;

class BookingRestServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private BookingRestServiceImpl bookingRestService;

    private String existingBookingId;
    private String fakeBookingId;
    private UUID customerId;
    private Booking booking;
    private Room room;
    private RoomType roomType;
    private Property property;
    private CreateBookingRequestDTO createBookingRequestDTO;
    private UpdateBookingRequestDTO updateBookingRequestDTO;
    private UpdateBookingStatusRequestDTO updateStatusRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingBookingId = "BOOK-001-201-2025-11-06-20:37:12";
        fakeBookingId = "BOOK-999-999-2025-11-06-20:37:12";
        customerId = UUID.randomUUID();

        // ✅ Setup Property
        property = Property.builder()
                .propertyID("HOT-4000-001")
                .propertyName("Grand Hotel Jakarta")
                .type(1)
                .income(0)
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
                .roomID("HOT-4000-001-201")
                .availabilityStatus(1)
                .activeRoom(1)
                .roomType(roomType)
                .build();

        // ✅ Setup Booking
        booking = Booking.builder()
                .bookingID(existingBookingId)
                .checkInDate(LocalDateTime.of(2025, 11, 13, 14, 0))
                .checkOutDate(LocalDateTime.of(2025, 11, 14, 12, 0))
                .totalDays(1)
                .totalPrice(350000)
                .status(0) // Waiting for Payment
                .customerID(customerId)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("081234567890")
                .isBreakfast(true)
                .capacity(2)
                .refund(0)
                .extraPay(0)
                .room(room)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        // ✅ Setup CreateBookingRequestDTO
        createBookingRequestDTO = CreateBookingRequestDTO.builder()
                .roomID("HOT-4000-001-201")
                .checkInDate(LocalDateTime.of(2025, 11, 13, 14, 0))
                .checkOutDate(LocalDateTime.of(2025, 11, 14, 12, 0))
                .customerID(customerId.toString())
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("081234567890")
                .capacity(2)
                .isBreakfast(true)
                .build();

        // ✅ Setup UpdateBookingRequestDTO
        updateBookingRequestDTO = UpdateBookingRequestDTO.builder()
                .roomID("HOT-4000-001-201")
                .checkInDate(LocalDateTime.of(2025, 11, 15, 14, 0))
                .checkOutDate(LocalDateTime.of(2025, 11, 16, 12, 0))
                .customerName("John Doe Updated")
                .customerEmail("john.updated@example.com")
                .customerPhone("081234567890")
                .capacity(2)
                .isBreakfast(false)
                .build();

        // ✅ Setup UpdateBookingStatusRequestDTO
        updateStatusRequestDTO = UpdateBookingStatusRequestDTO.builder()
                .bookingID(existingBookingId)
                .status(1) // Payment Confirmed
                .build();
    }

    // ==================== CREATE BOOKING ====================

    @Test
    void testCreateBooking_Success() {
        // Given
        when(roomRepository.findById("HOT-4000-001-201")).thenReturn(Optional.of(room));
        when(bookingRepository.findByRoom_RoomIDAndStatus(anyString(), anyInt())).thenReturn(new ArrayList<>());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // When
        BookingResponseDTO result = bookingRestService.createBooking(createBookingRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(350000, result.getTotalPrice());
        assertEquals(1, result.getTotalDays());
        assertEquals(0, result.getStatus());
        verify(roomRepository).findById("HOT-4000-001-201");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_RoomNotFound() {
        // Given
        when(roomRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRestService.createBooking(createBookingRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Room not found"));
        verify(roomRepository).findById("HOT-4000-001-201");
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_InvalidDates() {
        // Given
        createBookingRequestDTO.setCheckOutDate(LocalDateTime.of(2025, 11, 13, 14, 0)); // Same as check-in

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRestService.createBooking(createBookingRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Check-out date must be at least 1 day after check-in date"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_ExceedsCapacity() {
        // Given
        createBookingRequestDTO.setCapacity(10); // Exceeds room capacity (3)
        when(roomRepository.findById("HOT-4000-001-201")).thenReturn(Optional.of(room));
        when(bookingRepository.findByRoom_RoomIDAndStatus(anyString(), anyInt())).thenReturn(new ArrayList<>());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRestService.createBooking(createBookingRequestDTO);
        });

        assertTrue(exception.getMessage().contains("exceeds room capacity"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_WithBreakfast() {
        // Given
        createBookingRequestDTO.setIsBreakfast(true);
        when(roomRepository.findById("HOT-4000-001-201")).thenReturn(Optional.of(room));
        when(bookingRepository.findByRoom_RoomIDAndStatus(anyString(), anyInt())).thenReturn(new ArrayList<>());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // When
        BookingResponseDTO result = bookingRestService.createBooking(createBookingRequestDTO);

        // Then
        assertNotNull(result);
        assertTrue(result.getIsBreakfast());
        assertEquals(350000, result.getTotalPrice()); // 300000 + 50000 breakfast
        verify(bookingRepository).save(argThat(b -> b.isBreakfast() == true));
    }

    @Test
    void testCreateBooking_MultipleNights() {
        // Given
        createBookingRequestDTO.setCheckInDate(LocalDateTime.of(2025, 11, 13, 14, 0));
        createBookingRequestDTO.setCheckOutDate(LocalDateTime.of(2025, 11, 16, 12, 0)); // 3 nights
        
        when(roomRepository.findById("HOT-4000-001-201")).thenReturn(Optional.of(room));
        when(bookingRepository.findByRoom_RoomIDAndStatus(anyString(), anyInt())).thenReturn(new ArrayList<>());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            assertEquals(3, saved.getTotalDays());
            assertEquals(1050000, saved.getTotalPrice()); // (300000 + 50000) * 3
            return saved;
        });

        // When
        bookingRestService.createBooking(createBookingRequestDTO);

        // Then
        verify(bookingRepository).save(argThat(b -> 
            b.getTotalDays() == 3 && b.getTotalPrice() == 1050000
        ));
    }

    // ==================== GET BOOKING BY ID ====================

    @Test
    void testGetBookingById_Found() {
        // Given
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When
        BookingResponseDTO result = bookingRestService.getBookingById(existingBookingId);

        // Then
        assertNotNull(result);
        assertEquals(existingBookingId, result.getBookingID());
        assertEquals("John Doe", result.getCustomerName());
        verify(bookingRepository).findById(existingBookingId);
    }

    @Test
    void testGetBookingById_NotFound() {
        // Given
        when(bookingRepository.findById(fakeBookingId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRestService.getBookingById(fakeBookingId);
        });

        assertTrue(exception.getMessage().contains("Booking not found"));
        verify(bookingRepository).findById(fakeBookingId);
    }

    // ==================== GET ALL BOOKINGS ====================

    @Test
    void testGetAllBookings_Success() {
        // Given
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findAll()).thenReturn(bookings);

        // When
        List<BookingResponseDTO> result = bookingRestService.getAllBookings();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(existingBookingId, result.get(0).getBookingID());
        verify(bookingRepository).findAll();
    }

    @Test
    void testGetAllBookings_EmptyList() {
        // Given
        when(bookingRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        List<BookingResponseDTO> result = bookingRestService.getAllBookings();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookingRepository).findAll();
    }

    // ==================== GET BOOKINGS BY CUSTOMER ====================

    @Test
    void testGetBookingsByCustomer_Found() {
        // Given
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findByCustomerID(customerId)).thenReturn(bookings);

        // When
        List<BookingResponseDTO> result = bookingRestService.getBookingsByCustomer(customerId.toString());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(customerId.toString(), result.get(0).getCustomerID());
        verify(bookingRepository).findByCustomerID(customerId);
    }

    @Test
    void testGetBookingsByCustomer_NotFound() {
        // Given
        UUID fakeCustomerId = UUID.randomUUID();
        when(bookingRepository.findByCustomerID(fakeCustomerId)).thenReturn(new ArrayList<>());

        // When
        List<BookingResponseDTO> result = bookingRestService.getBookingsByCustomer(fakeCustomerId.toString());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookingRepository).findByCustomerID(fakeCustomerId);
    }

    // ==================== GET BOOKINGS BY STATUS ====================

    @Test
    void testGetBookingsByStatus_WaitingForPayment() {
        // Given
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findByStatus(0)).thenReturn(bookings);

        // When
        List<BookingResponseDTO> result = bookingRestService.getBookingsByStatus(0);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getStatus());
        assertEquals("Waiting for Payment", result.get(0).getStatusName());
        verify(bookingRepository).findByStatus(0);
    }

    @Test
    void testGetBookingsByStatus_PaymentConfirmed() {
        // Given
        booking.setStatus(1);
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findByStatus(1)).thenReturn(bookings);

        // When
        List<BookingResponseDTO> result = bookingRestService.getBookingsByStatus(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getStatus());
        assertEquals("Payment Confirmed", result.get(0).getStatusName());
        verify(bookingRepository).findByStatus(1);
    }

    // ==================== UPDATE BOOKING ====================

    @Test
    void testUpdateBooking_Success_SameRoom() {
        // Given
        booking.setStatus(0);
        updateBookingRequestDTO.setRoomID("HOT-4000-001-201"); // Same room
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(roomRepository.findById("HOT-4000-001-201")).thenReturn(Optional.of(room));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BookingResponseDTO result = bookingRestService.updateBooking(existingBookingId, updateBookingRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).findById(existingBookingId);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testUpdateBooking_PriceIncrease_GeneratesExtraPay() {
        // Given
        booking.setStatus(1); // Payment Confirmed
        booking.setTotalPrice(300000); // Old price (1 night)
        
        updateBookingRequestDTO.setRoomID("HOT-4000-001-201"); // Same room
        updateBookingRequestDTO.setCheckInDate(LocalDateTime.of(2025, 11, 13, 14, 0));
        updateBookingRequestDTO.setCheckOutDate(LocalDateTime.of(2025, 11, 16, 12, 0)); // 3 nights instead of 1
        
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(roomRepository.findById("HOT-4000-001-201")).thenReturn(Optional.of(room));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        // When
        BookingResponseDTO result = bookingRestService.updateBooking(existingBookingId, updateBookingRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(argThat(b -> 
            b.getExtraPay() > 0 && b.getStatus() == 0 // Status changes to Waiting for Payment
        ));
    }

    @Test
    void testUpdateBooking_PriceDecrease_GeneratesRefund() {
        // Given
        booking.setStatus(1); // Payment Confirmed
        booking.setTotalPrice(900000); // Old price (3 nights)
        booking.setCheckInDate(LocalDateTime.of(2025, 11, 13, 14, 0));
        booking.setCheckOutDate(LocalDateTime.of(2025, 11, 16, 12, 0)); // 3 nights
        
        updateBookingRequestDTO.setRoomID("HOT-4000-001-201"); // Same room
        updateBookingRequestDTO.setCheckInDate(LocalDateTime.of(2025, 11, 13, 14, 0));
        updateBookingRequestDTO.setCheckOutDate(LocalDateTime.of(2025, 11, 14, 12, 0)); // 1 night
        
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(roomRepository.findById("HOT-4000-001-201")).thenReturn(Optional.of(room));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        // When
        BookingResponseDTO result = bookingRestService.updateBooking(existingBookingId, updateBookingRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(argThat(b -> 
            b.getRefund() > 0 && b.getStatus() == 3 // Status changes to Request Refund
        ));
    }

    @Test
    void testUpdateBooking_Status0_KeepExtraPay() {
        // Given
        booking.setStatus(0);
        booking.setExtraPay(50000);
        booking.setTotalPrice(300000);
        
        updateBookingRequestDTO.setRoomID("HOT-4000-001-201"); // Same room
        updateBookingRequestDTO.setCheckInDate(LocalDateTime.of(2025, 11, 13, 14, 0));
        updateBookingRequestDTO.setCheckOutDate(LocalDateTime.of(2025, 11, 14, 12, 0));
        
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(roomRepository.findById("HOT-4000-001-201")).thenReturn(Optional.of(room));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BookingResponseDTO result = bookingRestService.updateBooking(existingBookingId, updateBookingRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(argThat(b -> 
            b.getStatus() == 0 && b.getExtraPay() == 50000
        ));
    }

    @Test
    void testUpdateBooking_Status3_KeepRefund() {
        // Given
        booking.setStatus(3); // Request Refund
        booking.setRefund(100000);
        booking.setTotalPrice(300000);
        
        updateBookingRequestDTO.setRoomID("HOT-4000-001-201"); // Same room
        updateBookingRequestDTO.setCheckInDate(LocalDateTime.of(2025, 11, 13, 14, 0));
        updateBookingRequestDTO.setCheckOutDate(LocalDateTime.of(2025, 11, 14, 12, 0));
        
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(roomRepository.findById("HOT-4000-001-201")).thenReturn(Optional.of(room));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BookingResponseDTO result = bookingRestService.updateBooking(existingBookingId, updateBookingRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(argThat(b -> 
            b.getStatus() == 3 && b.getRefund() == 100000
        ));
    }

    @Test
    void testUpdateBooking_RoomChanged() {
        // Given
        Room newRoom = Room.builder()
                .roomID("HOT-4000-001-202")
                .availabilityStatus(1)
                .activeRoom(1)
                .roomType(roomType)
                .build();
        
        booking.setStatus(0);
        booking.setCreatedDate(LocalDateTime.of(2025, 10, 1, 10, 0));
        updateBookingRequestDTO.setRoomID("HOT-4000-001-202");
        
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(roomRepository.findById("HOT-4000-001-202")).thenReturn(Optional.of(newRoom));
        when(bookingRepository.findByRoom_RoomIDAndStatus(anyString(), anyInt())).thenReturn(new ArrayList<>());
        doNothing().when(bookingRepository).deleteById(anyString());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BookingResponseDTO result = bookingRestService.updateBooking(existingBookingId, updateBookingRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).deleteById(existingBookingId);
        verify(bookingRepository).save(argThat(b -> 
            b.getRoom().getRoomID().equals("HOT-4000-001-202")
        ));
    }

    @Test
    void testUpdateBooking_InvalidDates() {
        // Given
        updateBookingRequestDTO.setCheckOutDate(LocalDateTime.of(2025, 11, 13, 14, 0)); // Same as check-in
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRestService.updateBooking(existingBookingId, updateBookingRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Check-out date must be at least 1 day after check-in date"));
    }

    @Test
    void testUpdateBooking_RoomNotFound() {
        // Given
        updateBookingRequestDTO.setRoomID("INVALID-ROOM-ID");
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(roomRepository.findById("INVALID-ROOM-ID")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRestService.updateBooking(existingBookingId, updateBookingRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Room not found"));
    }

    @Test
    void testUpdateBooking_PriceSame_Status1() {
        // Given
        booking.setStatus(1);
        booking.setTotalPrice(350000); // Same price after update
        
        updateBookingRequestDTO.setRoomID("HOT-4000-001-201"); // Same room
        updateBookingRequestDTO.setCheckInDate(LocalDateTime.of(2025, 11, 13, 14, 0));
        updateBookingRequestDTO.setCheckOutDate(LocalDateTime.of(2025, 11, 14, 12, 0));
        updateBookingRequestDTO.setIsBreakfast(true); // Same as before
        
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(roomRepository.findById("HOT-4000-001-201")).thenReturn(Optional.of(room));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        // When
        BookingResponseDTO result = bookingRestService.updateBooking(existingBookingId, updateBookingRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testUpdateBooking_NoRoomID() {
        // Given
        updateBookingRequestDTO.setRoomID(null); // Will use booking.getRoom()
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BookingResponseDTO result = bookingRestService.updateBooking(existingBookingId, updateBookingRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testUpdateBooking_NotFound() {
        // Given
        when(bookingRepository.findById(fakeBookingId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRestService.updateBooking(fakeBookingId, updateBookingRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Booking not found"));
        verify(bookingRepository).findById(fakeBookingId);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // ==================== UPDATE BOOKING STATUS ====================

    @Test
    void testUpdateBookingStatus_Success() {
        // Given
        updateStatusRequestDTO.setStatus(1);
        updateStatusRequestDTO.setRefund(null);
        updateStatusRequestDTO.setExtraPay(null);
        
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BookingResponseDTO result = bookingRestService.updateBookingStatus(existingBookingId, updateStatusRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(argThat(b -> b.getStatus() == 1));
    }

    @Test
    void testUpdateBookingStatus_WithRefund() {
        // Given
        updateStatusRequestDTO.setStatus(3);
        updateStatusRequestDTO.setRefund(50000);
        updateStatusRequestDTO.setExtraPay(null);
        
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BookingResponseDTO result = bookingRestService.updateBookingStatus(existingBookingId, updateStatusRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(argThat(b -> 
            b.getStatus() == 3 && b.getRefund() == 50000
        ));
    }

    @Test
    void testUpdateBookingStatus_WithExtraPay() {
        // Given
        updateStatusRequestDTO.setStatus(0);
        updateStatusRequestDTO.setRefund(null);
        updateStatusRequestDTO.setExtraPay(100000);
        
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BookingResponseDTO result = bookingRestService.updateBookingStatus(existingBookingId, updateStatusRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(argThat(b -> 
            b.getStatus() == 0 && b.getExtraPay() == 100000
        ));
    }

    @Test
    void testUpdateBookingStatus_NotFound() {
        // Given
        when(bookingRepository.findById(fakeBookingId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRestService.updateBookingStatus(fakeBookingId, updateStatusRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Booking not found"));
    }

    // ==================== DELETE BOOKING ====================

    @Test
    void testDeleteBooking_Success() {
        // Given
        when(bookingRepository.existsById(existingBookingId)).thenReturn(true);
        doNothing().when(bookingRepository).deleteById(existingBookingId);

        // When
        bookingRestService.deleteBooking(existingBookingId);

        // Then
        verify(bookingRepository).existsById(existingBookingId);
        verify(bookingRepository).deleteById(existingBookingId);
    }

    @Test
    void testDeleteBooking_NotFound() {
        // Given
        when(bookingRepository.existsById(fakeBookingId)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRestService.deleteBooking(fakeBookingId);
        });

        assertTrue(exception.getMessage().contains("Booking not found"));
        verify(bookingRepository, never()).deleteById(anyString());
    }

    // ==================== CONFIRM PAYMENT ====================

    @Test
    void testConfirmPayment_Success() {
        // Given
        booking.setStatus(0); // Waiting for Payment
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        // When
        BookingResponseDTO result = bookingRestService.confirmPayment(existingBookingId, updateStatusRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(argThat(b -> b.getStatus() == 1));
        verify(propertyRepository).save(any(Property.class)); // Income updated
    }

    @Test
    void testConfirmPayment_InvalidStatus() {
        // Given
        booking.setStatus(2); // Cancelled
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRestService.confirmPayment(existingBookingId, updateStatusRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Only bookings with status 'Waiting for Payment'"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testConfirmPayment_WithExtraPay() {
        // Given
        booking.setStatus(0);
        booking.setExtraPay(100000);
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        // When
        BookingResponseDTO result = bookingRestService.confirmPayment(existingBookingId, updateStatusRequestDTO);

        // Then
        verify(bookingRepository).save(argThat(b -> 
            b.getStatus() == 1 && b.getExtraPay() == 0 // Extra pay cleared after payment
        ));
    }

    // ==================== CANCEL BOOKING ====================

    @Test
    void testCancelBooking_Status0_NoExtraPay() {
        // Given
        booking.setStatus(0);
        booking.setExtraPay(0);
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // When
        BookingResponseDTO result = bookingRestService.cancelBooking(existingBookingId, updateStatusRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(argThat(b -> b.getStatus() == 2));
        verify(propertyRepository, never()).save(any(Property.class)); // No income change
    }

    @Test
    void testCancelBooking_Status0_WithExtraPay() {
        // Given
        booking.setStatus(0);
        booking.setExtraPay(100000);
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        // When
        BookingResponseDTO result = bookingRestService.cancelBooking(existingBookingId, updateStatusRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(argThat(b -> b.getStatus() == 2));
        verify(propertyRepository).save(any(Property.class)); // Income decreased by extra pay
    }

    @Test
    void testCancelBooking_Status1_FullRefund() {
        // Given
        booking.setStatus(1); // Payment Confirmed
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        // When
        BookingResponseDTO result = bookingRestService.cancelBooking(existingBookingId, updateStatusRequestDTO);

        // Then
        verify(bookingRepository).save(argThat(b -> b.getStatus() == 2));
        verify(propertyRepository).save(any(Property.class)); // Income decreased
    }

    @Test
    void testCancelBooking_Status3() {
        // Given
        booking.setStatus(3); // Request Refund
        booking.setTotalPrice(300000);
        booking.setRefund(50000);
        
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        // When
        BookingResponseDTO result = bookingRestService.cancelBooking(existingBookingId, updateStatusRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(argThat(b -> b.getStatus() == 2)); // Cancelled
        verify(propertyRepository).save(any(Property.class)); // Income decreased
    }

    @Test
    void testCancelBooking_InvalidStatus() {
        // Given
        booking.setStatus(2); // Already cancelled
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRestService.cancelBooking(existingBookingId, updateStatusRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Only bookings with status 0, 1, or 3 can be cancelled"));
    }

    // ==================== PROCESS REFUND ====================

    @Test
    void testProcessRefund_Success() {
        // Given
        booking.setStatus(3); // Request Refund
        booking.setRefund(50000);
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        // When
        BookingResponseDTO result = bookingRestService.processRefund(existingBookingId, updateStatusRequestDTO);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(argThat(b -> b.getStatus() == 4)); // Done
        verify(propertyRepository).save(any(Property.class)); // Income decreased by refund
    }

    @Test
    void testProcessRefund_InvalidStatus() {
        // Given
        booking.setStatus(1); // Payment Confirmed
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRestService.processRefund(existingBookingId, updateStatusRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Only bookings with status 'Request Refund'"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testProcessRefund_NoRefundAmount() {
        // Given
        booking.setStatus(3);
        booking.setRefund(0);
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRestService.processRefund(existingBookingId, updateStatusRequestDTO);
        });

        assertTrue(exception.getMessage().contains("No refund amount to process"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // ==================== AUTO UPDATE BOOKING STATUSES ====================

    @Test
    void testAutoUpdateBookingStatuses_Status1ToDone() {
        // Given
        booking.setStatus(1);
        booking.setCheckInDate(LocalDateTime.now().minusDays(1)); // Past check-in
        
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findAll()).thenReturn(bookings);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // When
        bookingRestService.autoUpdateBookingStatuses();

        // Then
        verify(bookingRepository).save(argThat(b -> b.getStatus() == 4)); // Done
    }

    @Test
    void testAutoUpdateBookingStatuses_Status0ToCancelled() {
        // Given
        booking.setStatus(0);
        booking.setCheckInDate(LocalDateTime.now().minusDays(1)); // Past check-in
        
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findAll()).thenReturn(bookings);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        // When
        bookingRestService.autoUpdateBookingStatuses();

        // Then
        verify(bookingRepository).save(argThat(b -> b.getStatus() == 2)); // Cancelled
    }

    @Test
    void testAutoUpdateBookingStatuses_Status0ToCancelled_WithExtraPay() {
        // Given
        booking.setStatus(0);
        booking.setExtraPay(100000);
        booking.setCheckInDate(LocalDateTime.now().minusDays(1)); // Past check-in
        
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findAll()).thenReturn(bookings);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        // When
        bookingRestService.autoUpdateBookingStatuses();

        // Then
        verify(bookingRepository).save(argThat(b -> b.getStatus() == 2)); // Cancelled
        verify(propertyRepository).save(any(Property.class)); // Income decreased by extra pay
    }

    @Test
    void testAutoUpdateBookingStatuses_Status3ToDone() {
        // Given
        booking.setStatus(3); // Request Refund
        booking.setRefund(50000);
        booking.setCheckInDate(LocalDateTime.now().minusDays(1)); // Past check-in
        
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findAll()).thenReturn(bookings);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        // When
        bookingRestService.autoUpdateBookingStatuses();

        // Then
        verify(bookingRepository).save(argThat(b -> b.getStatus() == 4)); // Done
        verify(propertyRepository).save(any(Property.class)); // Income decreased
    }

    @Test
    void testAutoUpdateBookingStatuses_NoUpdates() {
        // Given
        booking.setStatus(1);
        booking.setCheckInDate(LocalDateTime.now().plusDays(1)); // Future check-in
        
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findAll()).thenReturn(bookings);

        // When
        bookingRestService.autoUpdateBookingStatuses();

        // Then
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // ==================== GET CHART DATA ====================

    @Test
    void testGetBookingChartData_Success() {
        // Given
        booking.setStatus(4); // Done
        booking.setCheckInDate(LocalDateTime.of(2025, 11, 13, 14, 0));
        
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findByStatus(4)).thenReturn(bookings);

        // When
        List<BookingChartResponseDTO> result = bookingRestService.getBookingChartData(11, 2025);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("HOT-4000-001", result.get(0).getPropertyID());
        assertEquals("Grand Hotel Jakarta", result.get(0).getPropertyName());
        assertEquals(350000, result.get(0).getTotalIncome());
        assertEquals(1, result.get(0).getTotalBookings());
        verify(bookingRepository).findByStatus(4);
    }

    @Test
    void testGetBookingChartData_EmptyList() {
        // Given
        when(bookingRepository.findByStatus(4)).thenReturn(new ArrayList<>());

        // When
        List<BookingChartResponseDTO> result = bookingRestService.getBookingChartData(11, 2025);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookingRepository).findByStatus(4);
    }

    @Test
    void testGetBookingChartData_FilterByMonth() {
        // Given
        booking.setStatus(4);
        booking.setCheckInDate(LocalDateTime.of(2025, 11, 13, 14, 0));
        
        Booking booking2 = Booking.builder()
                .bookingID("BOOK-001-202-2025-11-07-10:00:00")
                .checkInDate(LocalDateTime.of(2025, 10, 15, 14, 0)) // Different month
                .status(4)
                .totalPrice(400000)
                .room(room)
                .build();
        
        List<Booking> bookings = Arrays.asList(booking, booking2);
        when(bookingRepository.findByStatus(4)).thenReturn(bookings);

        // When
        List<BookingChartResponseDTO> result = bookingRestService.getBookingChartData(11, 2025);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(350000, result.get(0).getTotalIncome()); // Only November booking
    }

    @Test
    void testGetBookingChartData_MultipleProperties() {
        // Given
        Property property2 = Property.builder()
                .propertyID("HOT-4000-002")
                .propertyName("Another Hotel")
                .income(0)
                .build();
        
        RoomType roomType2 = RoomType.builder()
                .roomTypeID("RT-HOT-4000-002-001")
                .property(property2)
                .build();
        
        Room room2 = Room.builder()
                .roomID("HOT-4000-002-301")
                .roomType(roomType2)
                .build();
        
        Booking booking2 = Booking.builder()
                .bookingID("BOOK-002-301-2025-11-07-10:00:00")
                .checkInDate(LocalDateTime.of(2025, 11, 20, 14, 0))
                .status(4)
                .totalPrice(500000)
                .room(room2)
                .build();
        
        booking.setStatus(4);
        booking.setCheckInDate(LocalDateTime.of(2025, 11, 13, 14, 0));
        
        List<Booking> bookings = Arrays.asList(booking, booking2);
        when(bookingRepository.findByStatus(4)).thenReturn(bookings);

        // When
        List<BookingChartResponseDTO> result = bookingRestService.getBookingChartData(11, 2025);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Should be sorted by income descending
        assertTrue(result.get(0).getTotalIncome() >= result.get(1).getTotalIncome());
    }

    @Test
    void testGetBookingChartData_FilterByYearOnly() {
        // Given
        booking.setStatus(4);
        booking.setCheckInDate(LocalDateTime.of(2025, 10, 15, 14, 0)); // October
        
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findByStatus(4)).thenReturn(bookings);

        // When - null month, only year
        List<BookingChartResponseDTO> result = bookingRestService.getBookingChartData(null, 2025);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetBookingChartData_BookingWithNullRoom() {
        // Given
        booking.setStatus(4);
        booking.setRoom(null);
        
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findByStatus(4)).thenReturn(bookings);

        // When
        List<BookingChartResponseDTO> result = bookingRestService.getBookingChartData(11, 2025);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Bookings with null room are skipped
    }

    @Test
    void testGetBookingChartData_BookingWithNullCheckInDate() {
        // Given
        booking.setStatus(4);
        booking.setCheckInDate(null);
        
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findByStatus(4)).thenReturn(bookings);

        // When
        List<BookingChartResponseDTO> result = bookingRestService.getBookingChartData(11, 2025);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Bookings with null check-in date are skipped
    }

    // ==================== HELPER METHODS ====================

    @Test
    void testStatusNameConversion_WaitingForPayment() {
        // Given
        booking.setStatus(0);
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When
        BookingResponseDTO result = bookingRestService.getBookingById(existingBookingId);

        // Then
        assertEquals("Waiting for Payment", result.getStatusName());
    }

    @Test
    void testStatusNameConversion_PaymentConfirmed() {
        // Given
        booking.setStatus(1);
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When
        BookingResponseDTO result = bookingRestService.getBookingById(existingBookingId);

        // Then
        assertEquals("Payment Confirmed", result.getStatusName());
    }

    @Test
    void testStatusNameConversion_Cancelled() {
        // Given
        booking.setStatus(2);
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When
        BookingResponseDTO result = bookingRestService.getBookingById(existingBookingId);

        // Then
        assertEquals("Cancelled", result.getStatusName());
    }

    @Test
    void testStatusNameConversion_RequestRefund() {
        // Given
        booking.setStatus(3);
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When
        BookingResponseDTO result = bookingRestService.getBookingById(existingBookingId);

        // Then
        assertEquals("Request Refund", result.getStatusName());
    }

    @Test
    void testStatusNameConversion_Done() {
        // Given
        booking.setStatus(4);
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When
        BookingResponseDTO result = bookingRestService.getBookingById(existingBookingId);

        // Then
        assertEquals("Done", result.getStatusName());
    }

    @Test
    void testStatusNameConversion_Unknown() {
        // Given
        booking.setStatus(99); // Unknown status
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When
        BookingResponseDTO result = bookingRestService.getBookingById(existingBookingId);

        // Then
        assertEquals("Unknown", result.getStatusName());
    }

    @Test
    void testRoomNameExtraction() {
        // Given
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When
        BookingResponseDTO result = bookingRestService.getBookingById(existingBookingId);

        // Then
        assertEquals("201", result.getRoomName()); // Extracted from HOT-4000-001-201
        assertEquals("HOT-4000-001-201", result.getRoomID()); // Full ID still available
    }

    @Test
    void testPropertyNameExtraction() {
        // Given
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When
        BookingResponseDTO result = bookingRestService.getBookingById(existingBookingId);

        // Then
        assertEquals("Grand Hotel Jakarta", result.getPropertyName());
    }

    @Test
    void testConvertToResponseDTO_WithNullRoom() {
        // Given
        booking.setRoom(null);
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When
        BookingResponseDTO result = bookingRestService.getBookingById(existingBookingId);

        // Then
        assertNotNull(result);
        assertNull(result.getRoomID());
        assertTrue(result.getRoomName() == null || result.getRoomName().isEmpty());
    }

    @Test
    void testConvertToResponseDTO_InvalidRoomIDFormat() {
        // Given
        Room roomWithInvalidID = Room.builder()
                .roomID("INVALID")
                .roomType(roomType)
                .build();
        booking.setRoom(roomWithInvalidID);
        
        when(bookingRepository.findById(existingBookingId)).thenReturn(Optional.of(booking));

        // When
        BookingResponseDTO result = bookingRestService.getBookingById(existingBookingId);

        // Then
        assertNotNull(result);
        assertEquals("INVALID", result.getRoomName()); // Fallback to full ID
    }

    @Test
    void testHasBookingConflictExcluding_NoConflict() {
        // Given
        when(bookingRepository.findByRoom_RoomIDAndStatus(anyString(), anyInt())).thenReturn(new ArrayList<>());

        // When
        boolean result = bookingRestService.hasBookingConflictExcluding(
            "HOT-4000-001-201",
            "2025-11-15T14:00:00",
            "2025-11-16T12:00:00",
            existingBookingId
        );

        // Then
        assertFalse(result);
    }

    @Test
    void testHasBookingConflictExcluding_WithConflict() {
        // Given
        Booking conflictBooking = Booking.builder()
                .bookingID("BOOK-001-201-2025-11-06-21:00:00")
                .checkInDate(LocalDateTime.of(2025, 11, 15, 14, 0))
                .checkOutDate(LocalDateTime.of(2025, 11, 16, 12, 0))
                .room(room)
                .build();
        
        when(bookingRepository.findByRoom_RoomIDAndStatus("HOT-4000-001-201", 0))
                .thenReturn(Arrays.asList(conflictBooking));
        when(bookingRepository.findByRoom_RoomIDAndStatus("HOT-4000-001-201", 1))
                .thenReturn(new ArrayList<>());

        // When
        boolean result = bookingRestService.hasBookingConflictExcluding(
            "HOT-4000-001-201",
            "2025-11-15T14:00:00",
            "2025-11-17T12:00:00",
            existingBookingId
        );

        // Then
        assertTrue(result);
    }

    @Test
    void testHasBookingConflictExcluding_ExcludeBookingID() {
        // Given
        Booking conflictBooking = Booking.builder()
                .bookingID(existingBookingId)
                .checkInDate(LocalDateTime.of(2025, 11, 15, 14, 0))
                .checkOutDate(LocalDateTime.of(2025, 11, 16, 12, 0))
                .room(room)
                .build();
        
        when(bookingRepository.findByRoom_RoomIDAndStatus("HOT-4000-001-201", 0))
                .thenReturn(Arrays.asList(conflictBooking));
        when(bookingRepository.findByRoom_RoomIDAndStatus("HOT-4000-001-201", 1))
                .thenReturn(new ArrayList<>());

        // When - exclude the booking itself
        boolean result = bookingRestService.hasBookingConflictExcluding(
            "HOT-4000-001-201",
            "2025-11-15T14:00:00",
            "2025-11-17T12:00:00",
            existingBookingId
        );

        // Then
        assertFalse(result); // No conflict because we excluded this booking
    }

    // @Test
    // void testCheckBookingConflict_WithConflict() {
    //     // Given
    //     Booking conflictBooking = Booking.builder()
    //             .bookingID("BOOK-001-201-2025-11-06-21:00:00")
    //             .checkInDate(LocalDateTime.of(2025, 11, 15, 14, 0))
    //             .checkOutDate(LocalDateTime.of(2025, 11, 16, 12, 0))
    //             .room(room)
    //             .status(1)
    //             .build();
        
    //     when(bookingRepository.findByRoom_RoomIDAndStatus("HOT-4000-001-201", 0))
    //             .thenReturn(new ArrayList<>());
    //     when(bookingRepository.findByRoom_RoomIDAndStatus("HOT-4000-001-201", 1))
    //             .thenReturn(Arrays.asList(conflictBooking));

    //     // When
    //     boolean result = bookingRestService.checkBookingConflict(
    //         "HOT-4000-001-201",
    //         LocalDateTime.of(2025, 11, 15, 14, 0),
    //         LocalDateTime.of(2025, 11, 17, 12, 0),
    //         null
    //     );

    //     // Then
    //     assertTrue(result);
    // }

    // @Test
    // void testCheckBookingConflict_ExcludeBookingID() {
    //     // Given
    //     Booking conflictBooking = Booking.builder()
    //             .bookingID(existingBookingId)
    //             .checkInDate(LocalDateTime.of(2025, 11, 15, 14, 0))
    //             .checkOutDate(LocalDateTime.of(2025, 11, 16, 12, 0))
    //             .room(room)
    //             .status(1)
    //             .build();
        
    //     when(bookingRepository.findByRoom_RoomIDAndStatus("HOT-4000-001-201", 0))
    //             .thenReturn(new ArrayList<>());
    //     when(bookingRepository.findByRoom_RoomIDAndStatus("HOT-4000-001-201", 1))
    //             .thenReturn(Arrays.asList(conflictBooking));

    //     // When - exclude the booking itself
    //     boolean result = bookingRestService.checkBookingConflict(
    //         "HOT-4000-001-201",
    //         LocalDateTime.of(2025, 11, 15, 14, 0),
    //         LocalDateTime.of(2025, 11, 17, 12, 0),
    //         existingBookingId
    //     );

    //     // Then
    //     assertFalse(result); // No conflict because we excluded this booking
    // }
}
