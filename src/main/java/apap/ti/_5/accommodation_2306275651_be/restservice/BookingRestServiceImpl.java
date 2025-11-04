package apap.ti._5.accommodation_2306275651_be.restservice;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti._5.accommodation_2306275651_be.model.Booking;
import apap.ti._5.accommodation_2306275651_be.model.Room;
import apap.ti._5.accommodation_2306275651_be.repository.BookingRepository;
import apap.ti._5.accommodation_2306275651_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306275651_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306275651_be.restdto.request.booking.CreateBookingRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.booking.UpdateBookingRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.booking.UpdateBookingStatusRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.booking.BookingResponseDTO;

@Service
@Transactional
public class BookingRestServiceImpl implements BookingRestService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private PropertyRepository propertyRepository;

   @Override
public BookingResponseDTO createBooking(CreateBookingRequestDTO dto) {
    // ✅ Validate dates
    if (dto.getCheckOutDate().isBefore(dto.getCheckInDate()) || 
        dto.getCheckOutDate().isEqual(dto.getCheckInDate())) {
        throw new RuntimeException("Check-out date must be at least 1 day after check-in date");
    }
    
    // ✅ Calculate total days (PENTING: gunakan between untuk date, bukan datetime!)
    LocalDateTime checkIn = dto.getCheckInDate();
    LocalDateTime checkOut = dto.getCheckOutDate();
    
    // ✅ FIX: Hitung selisih hari dengan benar (gunakan toLocalDate untuk akurat)
    long totalDays = java.time.temporal.ChronoUnit.DAYS.between(
        checkIn.toLocalDate(), 
        checkOut.toLocalDate()
    );
    
    // ✅ Pastikan minimal 1 hari
    if (totalDays < 1) {
        totalDays = 1;
    }
    
    System.out.println("📅 Calculating booking duration:");
    System.out.println("   Check-in: " + checkIn);
    System.out.println("   Check-out: " + checkOut);
    System.out.println("   Total Days: " + totalDays);
    
    // ✅ Get Room entity
    Room room = null;
    if (dto.getRoomID() != null && !dto.getRoomID().isEmpty()) {
        room = roomRepository.findById(dto.getRoomID())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + dto.getRoomID()));
    } else {
        throw new RuntimeException("Room ID is required for booking");
    }
    
    // ✅ Validate capacity
    if (room.getRoomType() != null && dto.getCapacity() > room.getRoomType().getCapacity()) {
        throw new RuntimeException("Capacity (" + dto.getCapacity() + 
                                  ") exceeds room capacity (" + room.getRoomType().getCapacity() + ")");
    }
    
    // ✅ Generate Booking ID
    String bookingID = generateBookingID(room.getRoomID());
    
    // ✅ FIX: Calculate price PER NIGHT * totalDays
    int basePrice = room.getRoomType().getPrice(); // Price PER NIGHT
    int breakfastPrice = (dto.getIsBreakfast() != null && dto.getIsBreakfast()) ? 50000 : 0; // Per night
    
    // ✅ Total price = (base + breakfast) * total days
    int totalPrice = (int) ((basePrice + breakfastPrice) * totalDays);
    
    System.out.println("💰 Calculating booking price:");
    System.out.println("   Base Price: Rp " + basePrice + " per night");
    System.out.println("   Breakfast: Rp " + breakfastPrice + " per night");
    System.out.println("   Total Days: " + totalDays + " nights");
    System.out.println("   Total Price: Rp " + totalPrice + " (= (Rp " + basePrice + " + Rp " + breakfastPrice + ") × " + totalDays + ")");
    
    // ✅ Parse customerID String ke UUID
    UUID customerUUID = UUID.fromString(dto.getCustomerID());
    
    // ✅ Build Booking entity
    Booking booking = Booking.builder()
            .bookingID(bookingID)
            .checkInDate(dto.getCheckInDate())
            .checkOutDate(dto.getCheckOutDate())
            .totalDays((int) totalDays)
            .totalPrice(totalPrice)
            .status(0) // 0 = Waiting for Payment
            .customerID(customerUUID)
            .customerName(dto.getCustomerName())
            .customerEmail(dto.getCustomerEmail())
            .customerPhone(dto.getCustomerPhone())
            .isBreakfast(dto.getIsBreakfast() != null ? dto.getIsBreakfast() : false)
            .capacity(dto.getCapacity())
            .refund(0)
            .extraPay(0)
            .room(room)
            .build();
    
    // ✅ Save booking
    Booking savedBooking = bookingRepository.save(booking);
    
    System.out.println("✅ Booking created:");
    System.out.println("   Booking ID: " + savedBooking.getBookingID());
    System.out.println("   Room ID: " + savedBooking.getRoom().getRoomID());
    System.out.println("   Customer: " + savedBooking.getCustomerName());
    System.out.println("   Total Days: " + savedBooking.getTotalDays());
    System.out.println("   Total Price: Rp " + savedBooking.getTotalPrice());
    
    return convertToResponseDTO(savedBooking);
}
// ✅ Helper method untuk generate Booking ID dari Room ID
private String generateBookingID(String roomID) {
    if (roomID == null || roomID.isEmpty()) {
        throw new RuntimeException("Room ID is required for booking ID generation");
    }
    
    // ✅ Extract 7 digit terakhir dari Room ID
    // Format Room ID: APT-0000-004-101
    // Ambil: 004-101 (7 karakter terakhir setelah split)
    String[] parts = roomID.split("-");
    
    String propertyCode = "";
    String roomNumber = "";
    
    if (parts.length >= 4) {
        // APT-0000-004-101
        // parts[0] = APT
        // parts[1] = 0000
        // parts[2] = 004  ← property code
        // parts[3] = 101  ← room number
        propertyCode = parts[parts.length - 2]; // 004
        roomNumber = parts[parts.length - 1];    // 101
    } else if (parts.length >= 2) {
        // Fallback untuk format lain
        propertyCode = parts[parts.length - 2];
        roomNumber = parts[parts.length - 1];
    } else {
        throw new RuntimeException("Invalid Room ID format: " + roomID);
    }
    
    // ✅ Format datetime: yyyy-MM-dd-HH:mm:ss
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
    String datetime = now.format(formatter);
    
    // ✅ Format: BOOK-{propertyCode}-{roomNumber}-{datetime}
    // Contoh: BOOK-004-101-2025-10-24-10:38:12
    return String.format("BOOK-%s-%s-%s", propertyCode, roomNumber, datetime);
}
    // ✅ TAMBAHKAN METHOD INI (yang missing)
    @Override
    public BookingResponseDTO getBookingById(String bookingID) {
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingID));
        return convertToResponseDTO(booking);
    }

   

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDTO> getBookingsByCustomer(String customerID) {
        return bookingRepository.findByCustomerID(UUID.fromString(customerID)).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDTO> getBookingsByStatus(int status) {
        return bookingRepository.findByStatus(status).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
public BookingResponseDTO updateBooking(String bookingID, UpdateBookingRequestDTO dto) {
    Booking booking = bookingRepository.findById(bookingID)
            .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingID));
    
    // Validate dates
    if (dto.getCheckOutDate().isBefore(dto.getCheckInDate())) {
        throw new RuntimeException("Check-out date must be after check-in date");
    }
    
    // Calculate total days
    long totalDays = Duration.between(dto.getCheckInDate(), dto.getCheckOutDate()).toDays();
    
    // ✅ Recalculate total price jika ada perubahan room atau breakfast
    int totalPrice = booking.getTotalPrice(); // Keep existing price by default
    
    if (dto.getRoomID() != null && !dto.getRoomID().isEmpty()) {
        Room room = roomRepository.findById(dto.getRoomID()).orElse(null);
        if (room != null && room.getRoomType() != null) {
            int basePrice = room.getRoomType().getPrice() * (int) totalDays;
            int breakfastPrice = (dto.getIsBreakfast() != null && dto.getIsBreakfast()) 
                ? 50000 * (int) totalDays 
                : 0;
            totalPrice = basePrice + breakfastPrice;
        }
    } else {
        // ✅ Jika tidak ganti room, hitung ulang breakfast saja
        // Ambil existing room dari booking (TODO: setelah ada relasi)
        // Untuk sekarang, recalculate berdasarkan existing price
        boolean oldBreakfast = booking.isBreakfast();
        boolean newBreakfast = dto.getIsBreakfast() != null ? dto.getIsBreakfast() : false;
        
        if (oldBreakfast != newBreakfast) {
            int breakfastDiff = (newBreakfast ? 1 : -1) * 50000 * (int) totalDays;
            totalPrice += breakfastDiff;
        }
    }
    
    // Update fields
    booking.setCheckInDate(dto.getCheckInDate());
    booking.setCheckOutDate(dto.getCheckOutDate());
    booking.setTotalDays((int) totalDays);
    booking.setTotalPrice(totalPrice); // ✅ Update total price
    booking.setCustomerName(dto.getCustomerName());
    booking.setCustomerEmail(dto.getCustomerEmail());
    booking.setCustomerPhone(dto.getCustomerPhone());
    booking.setCapacity(dto.getCapacity());
    booking.setBreakfast(dto.getIsBreakfast() != null ? dto.getIsBreakfast() : false);
    booking.setStatus(dto.getStatus());
    booking.setUpdatedDate(LocalDateTime.now());
    
    if (dto.getRefund() != null) {
        booking.setRefund(dto.getRefund());
    }
    if (dto.getExtraPay() != null) {
        booking.setExtraPay(dto.getExtraPay());
    }
    
    Booking updatedBooking = bookingRepository.save(booking);
    return convertToResponseDTO(updatedBooking);
}

    @Override
    public BookingResponseDTO updateBookingStatus(String bookingID, UpdateBookingStatusRequestDTO dto) {
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingID));
        
        // Update status
        booking.setStatus(dto.getStatus());
        booking.setUpdatedDate(LocalDateTime.now());
        
        if (dto.getRefund() != null) {
            booking.setRefund(dto.getRefund());
        }
        if (dto.getExtraPay() != null) {
            booking.setExtraPay(dto.getExtraPay());
        }
        
        Booking updatedBooking = bookingRepository.save(booking);
        return convertToResponseDTO(updatedBooking);
    }

    @Override
    public void deleteBooking(String bookingID) {
        if (!bookingRepository.existsById(bookingID)) {
            throw new RuntimeException("Booking not found with id: " + bookingID);
        }
        bookingRepository.deleteById(bookingID);
    }

    @Override
    @Transactional
    public void autoUpdateBookingStatuses() {
        LocalDateTime now = LocalDateTime.now();
        
        // ✅ Ambil semua bookings
        List<Booking> allBookings = bookingRepository.findAll();
        
        for (Booking booking : allBookings) {
            // ✅ Rule 1: Status 1 (Confirmed) → 4 (Done) jika sudah check-in
            if (booking.getStatus() == 1 && booking.getCheckInDate().isBefore(now)) {
                booking.setStatus(4); // Done
                booking.setUpdatedDate(now);
                bookingRepository.save(booking);
            }
            
            // ✅ Rule 2: Status 0 (Waiting Payment with extra pay) → 2 (Cancelled) jika lewat check-in
            if (booking.getStatus() == 0 && booking.getCheckInDate().isBefore(now)) {
                booking.setStatus(2); // Cancelled
                booking.setUpdatedDate(now);
                
                // ✅ Update property income (kurangi extra pay yang sudah dibayar)
                if (booking.getExtraPay() > 0) {
                    updatePropertyIncome(booking, -booking.getExtraPay());
                }
                
                bookingRepository.save(booking);
            }
            
            // ✅ Rule 3: Status 3 (Request Refund) → 4 (Done) jika lewat check-in
            if (booking.getStatus() == 3 && booking.getCheckInDate().isBefore(now)) {
                booking.setStatus(4); // Done
                booking.setUpdatedDate(now);
                
                // ✅ Update property income (kurangi refund yang harus dikembalikan)
                if (booking.getRefund() > 0) {
                    updatePropertyIncome(booking, -booking.getRefund());
                }
                
                bookingRepository.save(booking);
            }
        }
    }

      @Override
    public BookingResponseDTO confirmPayment(String bookingID, UpdateBookingStatusRequestDTO dto) {
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingID));
        
        // ✅ Validasi: Hanya booking dengan status 0 (Waiting for Payment) yang bisa confirm payment
        if (booking.getStatus() != 0) {
            throw new RuntimeException("Only bookings with status 'Waiting for Payment' can be paid");
        }
        
        // ✅ Update status menjadi 1 (Payment Confirmed)
        booking.setStatus(1);
        booking.setUpdatedDate(LocalDateTime.now());
        
        // ✅ Hitung income yang akan ditambahkan ke property
        int incomeToAdd = booking.getTotalPrice();
        
        // ✅ FIX: Hilangkan null check, langsung cek > 0
        if (booking.getExtraPay() > 0) {
            incomeToAdd = booking.getExtraPay(); // Hanya tambahkan extra pay saja
            booking.setExtraPay(0); // Reset extra pay
        }
        
        // ✅ Update property income
        updatePropertyIncome(booking, incomeToAdd);
        
        Booking updatedBooking = bookingRepository.save(booking);
        return convertToResponseDTO(updatedBooking);
    }

    @Override
    public BookingResponseDTO cancelBooking(String bookingID, UpdateBookingStatusRequestDTO dto) {
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingID));
        
        // ✅ Validasi: Hanya booking dengan status 0, 1, 3 yang bisa cancel
        if (booking.getStatus() != 0 && booking.getStatus() != 1 && booking.getStatus() != 3) {
            throw new RuntimeException("Only bookings with status 0, 1, or 3 can be cancelled");
        }
        
        int currentStatus = booking.getStatus();
        
        // ✅ Update status menjadi 2 (Cancelled)
        booking.setStatus(2);
        booking.setUpdatedDate(LocalDateTime.now());
        
        // ✅ FIX: Logic pengurangan income berdasarkan status sebelumnya
        if (currentStatus == 0 && booking.getExtraPay() > 0) {
            // Status 0 dengan extra pay: income - totalPrice + extraPay
            int incomeChange = -booking.getTotalPrice() + booking.getExtraPay();
            updatePropertyIncome(booking, incomeChange);
            
        } else if (currentStatus == 1) {
            // Status 1: income - totalPrice
            updatePropertyIncome(booking, -booking.getTotalPrice());
            
        } else if (currentStatus == 3) {
            // ✅ FIX: Status 3: income - totalPrice - refund
            int incomeChange = -booking.getTotalPrice() - booking.getRefund();
            updatePropertyIncome(booking, incomeChange);
        }
        // Status 0 tanpa extra pay: tidak ada perubahan income
        
        Booking updatedBooking = bookingRepository.save(booking);
        return convertToResponseDTO(updatedBooking);
    }

    @Override
    public BookingResponseDTO requestRefund(String bookingID, UpdateBookingStatusRequestDTO dto) {
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingID));
        
        // ✅ Validasi: Hanya booking dengan status 1 (Payment Confirmed) yang bisa request refund
        if (booking.getStatus() != 1) {
            throw new RuntimeException("Only bookings with status 'Payment Confirmed' can request refund");
        }
        
        // ✅ Validasi: Refund amount harus diisi dan > 0
        if (dto.getRefund() == null || dto.getRefund() <= 0) {
            throw new RuntimeException("Refund amount must be greater than 0");
        }
        
        // ✅ Validasi: Refund tidak boleh lebih besar dari total price
        if (dto.getRefund() > booking.getTotalPrice()) {
            throw new RuntimeException("Refund amount cannot exceed total price");
        }
        
        // ✅ Update status menjadi 3 (Request Refund)
        booking.setStatus(3);
        booking.setRefund(dto.getRefund());
        booking.setUpdatedDate(LocalDateTime.now());
        
        // ✅ Kurangi property income sejumlah refund
        updatePropertyIncome(booking, -dto.getRefund());
        
        Booking updatedBooking = bookingRepository.save(booking);
        return convertToResponseDTO(updatedBooking);
    }

    
    // ✅ Helper method untuk update property income
    private void updatePropertyIncome(Booking booking, int incomeChange) {
        // TODO: Implement setelah relasi Booking-Room selesai
        // Saat ini skip karena belum ada relasi Room di Booking
        
        // Contoh implementasi jika sudah ada relasi:
        /*
        if (booking.getRoom() != null) {
            Room room = booking.getRoom();
            RoomType roomType = room.getRoomType();
            Property property = roomType.getProperty();
            
            int currentIncome = property.getIncome() != null ? property.getIncome() : 0;
            property.setIncome(currentIncome + incomeChange);
            propertyRepository.save(property);
        }
        */
    }

    private BookingResponseDTO convertToResponseDTO(Booking booking) {
    // ✅ Get property name dari room -> roomType -> property
    String propertyName = "";
    String roomName = "";
    
    if (booking.getRoom() != null) {
        roomName = booking.getRoom().getName();
        
        if (booking.getRoom().getRoomType() != null && 
            booking.getRoom().getRoomType().getProperty() != null) {
            propertyName = booking.getRoom().getRoomType().getProperty().getPropertyName();
        }
    }
    
    return BookingResponseDTO.builder()
            .bookingID(booking.getBookingID())
            .checkInDate(booking.getCheckInDate())
            .checkOutDate(booking.getCheckOutDate())
            .totalDays(booking.getTotalDays())
            .totalPrice(booking.getTotalPrice())
            .status(booking.getStatus())
            .statusName(getStatusName(booking.getStatus()))
            .customerID(booking.getCustomerID().toString())
            .customerName(booking.getCustomerName())
            .customerEmail(booking.getCustomerEmail())
            .customerPhone(booking.getCustomerPhone())
            .isBreakfast(booking.isBreakfast())
            .refund(booking.getRefund())
            .extraPay(booking.getExtraPay())
            .capacity(booking.getCapacity())
            .propertyName(propertyName)
            .roomName(roomName)
            .roomID(booking.getRoom() != null ? booking.getRoom().getRoomID() : null) // ✅ Tambahkan roomID
            .createdDate(booking.getCreatedDate())
            .updatedDate(booking.getUpdatedDate())
            .build();
}


    @Override
public boolean hasBookingConflict(String roomID, String startDate, String endDate) {
    // TODO: Implement setelah ada relasi Booking-Room
    // Untuk sekarang return false
    return false;
}

// ✅ TAMBAHKAN method ini
@Override
public boolean hasBookingConflictExcluding(String roomID, String startDate, String endDate, String excludeBookingID) {
    // TODO: Implement setelah ada relasi Booking-Room
    // Check booking conflict tapi exclude booking dengan ID tertentu
    // Untuk sekarang return false
    return false;
}
    // ✅ Helper method untuk status name
    private String getStatusName(int status) {
        return switch (status) {
            case 0 -> "Waiting for Payment";
            case 1 -> "Payment Confirmed";
            case 2 -> "Cancelled";
            case 3 -> "Request Refund";
            case 4 -> "Done";
            default -> "Unknown";
        };
    }
}