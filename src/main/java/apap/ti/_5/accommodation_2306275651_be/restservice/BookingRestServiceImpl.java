package apap.ti._5.accommodation_2306275651_be.restservice;

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
    
    System.out.println(" Calculating booking price:");
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
    
    // ✅ Validate dates
    if (dto.getCheckOutDate().isBefore(dto.getCheckInDate()) ||
        dto.getCheckOutDate().isEqual(dto.getCheckInDate())) {
        throw new RuntimeException("Check-out date must be at least 1 day after check-in date");
    }
    
    // ✅ Calculate total days dengan benar
    long totalDays = java.time.temporal.ChronoUnit.DAYS.between(
        dto.getCheckInDate().toLocalDate(), 
        dto.getCheckOutDate().toLocalDate()
    );
    
    if (totalDays < 1) {
        totalDays = 1;
    }
    
    System.out.println("📅 Updating booking duration:");
    System.out.println("   Old Check-in: " + booking.getCheckInDate());
    System.out.println("   New Check-in: " + dto.getCheckInDate());
    System.out.println("   Old Check-out: " + booking.getCheckOutDate());
    System.out.println("   New Check-out: " + dto.getCheckOutDate());
    System.out.println("   Total Days: " + totalDays);
    
    // ✅ FIX: Update Room entity & Booking ID jika ganti room
    Room newRoom = null;
    boolean roomChanged = false;
    
    if (dto.getRoomID() != null && !dto.getRoomID().isEmpty()) {
        newRoom = roomRepository.findById(dto.getRoomID())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + dto.getRoomID()));
        
        // ✅ Cek apakah room berubah
        if (booking.getRoom() == null || !booking.getRoom().getRoomID().equals(dto.getRoomID())) {
            roomChanged = true;
            System.out.println("🔄 Room changed:");
            System.out.println("   Old Room: " + (booking.getRoom() != null ? booking.getRoom().getRoomID() : "NULL"));
            System.out.println("   New Room: " + newRoom.getRoomID());
        }
    } else {
        // ✅ Jika tidak ada roomID baru, gunakan room lama
        newRoom = booking.getRoom();
    }
    
    if (newRoom == null) {
        throw new RuntimeException("Room is required for booking");
    }
    
    // ✅ Recalculate total price dengan benar
    int basePrice = newRoom.getRoomType().getPrice(); // Price per night
    int breakfastPrice = (dto.getIsBreakfast() != null && dto.getIsBreakfast()) ? 50000 : 0; // Per night
    int totalPrice = (int) ((basePrice + breakfastPrice) * totalDays);
    
    System.out.println("💰 Recalculating booking price:");
    System.out.println("   Base Price: Rp " + basePrice + " per night");
    System.out.println("   Breakfast: Rp " + breakfastPrice + " per night");
    System.out.println("   Total Days: " + totalDays + " nights");
    System.out.println("   Old Total Price: Rp " + booking.getTotalPrice());
    System.out.println("   New Total Price: Rp " + totalPrice);
    
    // ✅ FIX: Jika room berubah, buat entity BARU (jangan delete-save)
    if (roomChanged) {
        // ✅ Generate new booking ID dengan room baru, KEEP created date
        LocalDateTime createdDate = booking.getCreatedDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
        String datetime = createdDate.format(formatter);
        
        // ✅ Extract property code & room number dari new room ID
        String[] parts = newRoom.getRoomID().split("-");
        String propertyCode = parts[parts.length - 2]; // 004
        String roomNumber = parts[parts.length - 1];    // 101
        
        String newBookingID = String.format("BOOK-%s-%s-%s", propertyCode, roomNumber, datetime);
        
        System.out.println("🆔 Booking ID changed:");
        System.out.println("   Old Booking ID: " + bookingID);
        System.out.println("   New Booking ID: " + newBookingID);
        
        // ✅ FIX: Buat entity BARU, jangan delete-save yang lama
        Booking newBooking = Booking.builder()
                .bookingID(newBookingID)
                .checkInDate(dto.getCheckInDate())
                .checkOutDate(dto.getCheckOutDate())
                .totalDays((int) totalDays)
                .totalPrice(totalPrice)
                .status(dto.getStatus())
                .customerID(booking.getCustomerID()) // Keep customer ID
                .customerName(dto.getCustomerName())
                .customerEmail(dto.getCustomerEmail())
                .customerPhone(dto.getCustomerPhone())
                .isBreakfast(dto.getIsBreakfast() != null ? dto.getIsBreakfast() : false)
                .capacity(dto.getCapacity())
                .refund(booking.getRefund()) // Keep refund
                .extraPay(booking.getExtraPay()) // Keep extra pay
                .room(newRoom)
                .createdDate(createdDate) // ✅ KEEP created date lama
                .updatedDate(LocalDateTime.now())
                .build();
        
        // ✅ Delete old booking
        bookingRepository.deleteById(bookingID);
        
        // ✅ Save new booking
        Booking savedBooking = bookingRepository.save(newBooking);
        
        System.out.println("✅ New booking created:");
        System.out.println("   Booking ID: " + savedBooking.getBookingID());
        System.out.println("   Room ID: " + savedBooking.getRoom().getRoomID());
        System.out.println("   Created Date: " + savedBooking.getCreatedDate() + " (unchanged)");
        
        return convertToResponseDTO(savedBooking);
        
    } else {
        // ✅ Room TIDAK berubah, update existing booking
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setTotalDays((int) totalDays);
        booking.setTotalPrice(totalPrice);
        booking.setCustomerName(dto.getCustomerName());
        booking.setCustomerEmail(dto.getCustomerEmail());
        booking.setCustomerPhone(dto.getCustomerPhone());
        booking.setCapacity(dto.getCapacity());
        booking.setBreakfast(dto.getIsBreakfast() != null ? dto.getIsBreakfast() : false);
        booking.setStatus(dto.getStatus());
        booking.setUpdatedDate(LocalDateTime.now());
        
        Booking updatedBooking = bookingRepository.save(booking);
        
        System.out.println("✅ Booking updated (same room):");
        System.out.println("   Booking ID: " + updatedBooking.getBookingID());
        System.out.println("   Total Days: " + updatedBooking.getTotalDays());
        System.out.println("   Total Price: Rp " + updatedBooking.getTotalPrice());
        
        return convertToResponseDTO(updatedBooking);
    }
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
    
    // ✅ FIX: Hitung income yang akan ditambahkan
    int incomeToAdd;
    
    if (booking.getExtraPay() > 0) {
        // ✅ Kasus: Ada extra pay
        // Income sudah berisi total price LAMA (saat pertama kali bayar)
        // Sekarang HANYA tambahkan extra pay saja
        incomeToAdd = booking.getExtraPay();
        booking.setExtraPay(0); // Reset extra pay setelah dibayar
        
        System.out.println("💰 Payment with extra pay:");
        System.out.println("   Extra Pay: Rp " + incomeToAdd);
        System.out.println("   Total Price (already in income): Rp " + (booking.getTotalPrice() - incomeToAdd));
        
    } else {
        // ✅ Kasus: Tidak ada extra pay (pembayaran pertama kali)
        // Tambahkan total price ke income
        incomeToAdd = booking.getTotalPrice();
        
        System.out.println("💰 First payment:");
        System.out.println("   Total Price: Rp " + incomeToAdd);
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
    
    System.out.println("🚫 Cancelling booking:");
    System.out.println("   Booking ID: " + bookingID);
    System.out.println("   Current Status: " + currentStatus);
    System.out.println("   Total Price: Rp " + booking.getTotalPrice());
    System.out.println("   Extra Pay: Rp " + booking.getExtraPay());
    System.out.println("   Refund: Rp " + booking.getRefund());
    
    // ✅ FIX: Logic pengurangan income berdasarkan status sebelumnya
    if (currentStatus == 0) {
        if (booking.getExtraPay() > 0) {
            // ✅ Status 0 dengan extra pay
            // Income saat ini = extra pay yang sudah dibayar
            // Cancel = kurangi extra pay saja
            int incomeChange = -booking.getExtraPay();
            updatePropertyIncome(booking, incomeChange);
            
            System.out.println("   Income change: -Rp " + booking.getExtraPay() + " (extra pay)");
            
        } else {
            // ✅ Status 0 tanpa extra pay
            // Income tidak berubah (belum ada pembayaran)
            System.out.println("   Income change: Rp 0 (no payment yet)");
        }
        
    } else if (currentStatus == 1) {
        // ✅ Status 1 (Payment Confirmed)
        // Income saat ini = total price yang sudah dibayar
        // Cancel = kurangi total price
        updatePropertyIncome(booking, -booking.getTotalPrice());
        
        System.out.println("   Income change: -Rp " + booking.getTotalPrice() + " (refund full payment)");
        
    } else if (currentStatus == 3) {
        // ✅ Status 3 (Request Refund)
        // Income saat ini = total price LAMA (sebelum update)
        // Refund belum dikurangi dari income
        // Cancel = kurangi total price BARU + refund yang harus dikembalikan
        
        int totalPriceOld = booking.getTotalPrice() + booking.getRefund(); // Total price LAMA
        int incomeChange = -totalPriceOld; // Atau: -(booking.getTotalPrice() + booking.getRefund())
        
        updatePropertyIncome(booking, incomeChange);
        
        System.out.println("   Total Price OLD: Rp " + totalPriceOld);
        System.out.println("   Income change: -Rp " + totalPriceOld + " (full refund including pending refund)");
    }
    
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
    
    System.out.println("💸 Request refund:");
    System.out.println("   Booking ID: " + bookingID);
    System.out.println("   Refund Amount: Rp " + dto.getRefund());
    System.out.println("   ⚠️ Income NOT changed yet (will be deducted when refund is processed)");
    
    // ✅ FIX: JANGAN kurangi income dulu
    // Income baru dikurangi saat:
    // 1. Manual refund (button "Refund" di detail page) → Belum diimplementasi
    // 2. Auto-update saat check-in (status 3 → 4) → Sudah ada di autoUpdateBookingStatuses()
    
    // ❌ HAPUS LINE INI:
    // updatePropertyIncome(booking, -dto.getRefund());
    
    Booking updatedBooking = bookingRepository.save(booking);
    return convertToResponseDTO(updatedBooking);
}
    
   // ✅ Helper method untuk update property income
private void updatePropertyIncome(Booking booking, int incomeChange) {
    try {
        if (booking == null || booking.getRoom() == null) {
            System.err.println("⚠️ Cannot update property income: Booking or Room is null");
            return;
        }
        
        Room room = booking.getRoom();
        
        if (room.getRoomType() == null) {
            System.err.println("⚠️ Cannot update property income: RoomType is null for room " + room.getRoomID());
            return;
        }
        
        if (room.getRoomType().getProperty() == null) {
            System.err.println("⚠️ Cannot update property income: Property is null for room type " + room.getRoomType().getRoomTypeID());
            return;
        }
        
        // ✅ Get property entity
        var property = room.getRoomType().getProperty();
        
        // ✅ Calculate new income
        int currentIncome = property.getIncome();
        int newIncome = currentIncome + incomeChange;
        
        // ✅ Prevent negative income (optional, tergantung business logic)
        if (newIncome < 0) {
            System.err.println("⚠️ Warning: Property income akan menjadi negatif!");
            System.err.println("   Current Income: Rp " + currentIncome);
            System.err.println("   Income Change: Rp " + incomeChange);
            System.err.println("   Calculated New Income: Rp " + newIncome);
            // Uncomment jika tidak boleh negatif:
            // newIncome = 0;
        }
        
        // ✅ Update property income
        property.setIncome(newIncome);
        propertyRepository.save(property);
        
        System.out.println("💰 Property income updated:");
        System.out.println("   Property ID: " + property.getPropertyID());
        System.out.println("   Property Name: " + property.getPropertyName());
        System.out.println("   Old Income: Rp " + currentIncome);
        System.out.println("   Change: " + (incomeChange >= 0 ? "+" : "") + "Rp " + incomeChange);
        System.out.println("   New Income: Rp " + newIncome);
        
    } catch (Exception ex) {
        System.err.println("❌ Error updating property income: " + ex.getMessage());
        ex.printStackTrace();
        // Jangan throw exception, biarkan booking tetap tersimpan
    }
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
    try {
        LocalDateTime checkInDate = LocalDateTime.parse(startDate);
        LocalDateTime checkOutDate = LocalDateTime.parse(endDate);
        
        System.out.println("🔍 Checking booking conflicts (excluding current booking):");
        System.out.println("   Room ID: " + roomID);
        System.out.println("   Check-in: " + checkInDate);
        System.out.println("   Check-out: " + checkOutDate);
        System.out.println("   Exclude Booking ID: " + excludeBookingID);
        
        // ✅ Get active bookings untuk room ini (status 0 & 1)
        List<Booking> activeBookings = bookingRepository.findByRoom_RoomIDAndStatus(roomID, 0);
        activeBookings.addAll(bookingRepository.findByRoom_RoomIDAndStatus(roomID, 1));
        
        // ✅ Filter: exclude current booking
        List<Booking> conflicts = activeBookings.stream()
            .filter(b -> !b.getBookingID().equals(excludeBookingID)) // Exclude current booking
            .filter(b -> {
                // Check if booking overlaps with new dates
                return b.getCheckInDate().isBefore(checkOutDate) && 
                       b.getCheckOutDate().isAfter(checkInDate);
            })
            .collect(Collectors.toList());
        
        if (!conflicts.isEmpty()) {
            System.out.println("   ❌ Found " + conflicts.size() + " conflicting booking(s):");
            for (Booking b : conflicts) {
                System.out.println("      - Booking ID: " + b.getBookingID() + 
                                 " (Check-in: " + b.getCheckInDate() + 
                                 ", Check-out: " + b.getCheckOutDate() + ")");
            }
            return true;
        }
        
        System.out.println("   ✅ No booking conflicts found");
        return false;
        
    } catch (Exception ex) {
        System.err.println("   ⚠️ Error checking booking conflict: " + ex.getMessage());
        return false;
    }
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