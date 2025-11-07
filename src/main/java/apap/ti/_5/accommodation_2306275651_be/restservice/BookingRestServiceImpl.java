package apap.ti._5.accommodation_2306275651_be.restservice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import apap.ti._5.accommodation_2306275651_be.restdto.response.booking.BookingChartResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.booking.BookingResponseDTO;

@Service
@Transactional
public class BookingRestServiceImpl implements BookingRestService {
    
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;

    // ✅ Constructor Dependency Injection
    public BookingRestServiceImpl(
            BookingRepository bookingRepository,
            RoomRepository roomRepository,
            PropertyRepository propertyRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.propertyRepository = propertyRepository;
    }

    private LocalDateTime[] normalizeBookingDates(LocalDateTime checkInDateInput, LocalDateTime checkOutDateInput) {
        // ✅ Untuk perhitungan totalDays: set jam 00:00:00
        LocalDateTime checkInForCalculation = checkInDateInput
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        
        LocalDateTime checkOutForCalculation = checkOutDateInput
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        
        // ✅ Untuk penyimpanan ke database: check-in jam 14:00, check-out jam 12:00
        LocalDateTime checkInForStorage = checkInDateInput
            .withHour(14)  // 2 PM
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        
        LocalDateTime checkOutForStorage = checkOutDateInput
            .withHour(12)  // 12 PM
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        
        return new LocalDateTime[]{
            checkInForCalculation, 
            checkOutForCalculation, 
            checkInForStorage, 
            checkOutForStorage
        };
    }



    @Override
    public BookingResponseDTO createBooking(CreateBookingRequestDTO dto) {
    
        boolean hasConflict = checkBookingConflict(
                dto.getRoomID(),
                dto.getCheckInDate(),
                dto.getCheckOutDate(),
                null // No exclude ID for new booking
            );
            
            if (hasConflict) {
                System.err.println("   ❌ BOOKING CONFLICT DETECTED!");
                throw new RuntimeException("Kamar sudah dibooking pada tanggal tersebut. Silakan pilih tanggal lain.");
            }
        // ✅ Validate dates
        if (dto.getCheckOutDate().toLocalDate().isBefore(dto.getCheckInDate().toLocalDate()) || 
            dto.getCheckOutDate().toLocalDate().isEqual(dto.getCheckInDate().toLocalDate())) {
            throw new RuntimeException("Check-out date must be at least 1 day after check-in date");
        }
        
        // ✅ Normalize dates: untuk perhitungan (00:00) dan untuk storage (14:00 & 12:00)
        LocalDateTime[] normalizedDates = normalizeBookingDates(dto.getCheckInDate(), dto.getCheckOutDate());
        LocalDateTime checkInForCalculation = normalizedDates[0];
        LocalDateTime checkOutForCalculation = normalizedDates[1];
        LocalDateTime checkInForStorage = normalizedDates[2];
        LocalDateTime checkOutForStorage = normalizedDates[3];
        
        // ✅ FIX: Hitung selisih hari dengan benar menggunakan tanggal dengan jam 00:00:00
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(
            checkInForCalculation.toLocalDate(), 
            checkOutForCalculation.toLocalDate()
        );
        
        // ✅ Pastikan minimal 1 hari
        if (totalDays < 1) {
            totalDays = 1;
        }
        
        System.out.println("Calculating booking duration:");
        System.out.println("   Check-in (for calculation): " + checkInForCalculation);
        System.out.println("   Check-out (for calculation): " + checkOutForCalculation);
        System.out.println("   Total Days: " + totalDays);
        System.out.println("   Check-in (for storage): " + checkInForStorage);
        System.out.println("   Check-out (for storage): " + checkOutForStorage);
        
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
        
        // ✅ FIX: Calculate price dengan long untuk menghindari overflow
        long basePrice = room.getRoomType().getPrice(); // Price PER NIGHT
        long breakfastPrice = (dto.getIsBreakfast() != null && dto.getIsBreakfast()) ? 50000L : 0L; // Per night
        
        // ✅ Total price = (base + breakfast) * total days (gunakan long)
        long totalPriceLong = (basePrice + breakfastPrice) * totalDays;
    
        // ✅ Validasi agar tidak melebihi max int
        if (totalPriceLong > Integer.MAX_VALUE) {
            throw new RuntimeException("Total price exceeds maximum value. Please reduce booking duration.");
        }
    
        int totalPrice = (int) totalPriceLong;
        
        System.out.println("💰 Calculating booking price:");
        System.out.println("   Base Price: Rp " + basePrice + " per night");
        System.out.println("   Breakfast: Rp " + breakfastPrice + " per night");
        System.out.println("   Total Days: " + totalDays + " nights");
        System.out.println("   Total Price: Rp " + totalPrice + " (= (Rp " + basePrice + " + Rp " + breakfastPrice + ") × " + totalDays + ")");
        
        // ✅ Parse customerID String ke UUID
        UUID customerUUID = UUID.fromString(dto.getCustomerID());
        
        // ✅ Build Booking entity dengan waktu yang sudah di-normalize untuk storage
        Booking booking = Booking.builder()
                .bookingID(bookingID)
                .checkInDate(checkInForStorage)  // ✅ Gunakan checkInForStorage (jam 14:00)
                .checkOutDate(checkOutForStorage)  // ✅ Gunakan checkOutForStorage (jam 12:00)
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
// Check booking conflict (internal use)
    private boolean checkBookingConflict(
            String roomID, 
            LocalDateTime checkInDate, 
            LocalDateTime checkOutDate,
            String excludeBookingID) {
        
        System.out.println("🔍 Checking booking conflicts:");
        System.out.println("   Room ID: " + roomID);
        System.out.println("   Check-in: " + checkInDate);
        System.out.println("   Check-out: " + checkOutDate);
        System.out.println("   Exclude Booking ID: " + (excludeBookingID != null ? excludeBookingID : "none"));
        
        try {
            //  Get active bookings (status 0 & 1) untuk room ini
            List<Booking> activeBookings = bookingRepository.findByRoom_RoomIDAndStatus(roomID, 0);
            activeBookings.addAll(bookingRepository.findByRoom_RoomIDAndStatus(roomID, 1));
            
            System.out.println("   Total active bookings for this room: " + activeBookings.size());
            
            // ✅ Filter: check overlap (exclude current booking if update)
            List<Booking> conflicts = activeBookings.stream()
                .filter(b -> excludeBookingID == null || !b.getBookingID().equals(excludeBookingID))
                .filter(b -> {
                    // CRITICAL: Check if booking dates overlap
                    // Overlap occurs if:
                    // (new_checkin < existing_checkout) AND (new_checkout > existing_checkin)
                    boolean overlaps = checkInDate.isBefore(b.getCheckOutDate()) && 
                                      checkOutDate.isAfter(b.getCheckInDate());
                    
                    if (overlaps) {
                        System.out.println("   ⚠️ OVERLAP DETECTED:");
                        System.out.println("      Existing Booking ID: " + b.getBookingID());
                        System.out.println("      Existing Check-in: " + b.getCheckInDate());
                        System.out.println("      Existing Check-out: " + b.getCheckOutDate());
                        System.out.println("      New Check-in: " + checkInDate);
                        System.out.println("      New Check-out: " + checkOutDate);
                    }
                    
                    return overlaps;
                })
                .collect(Collectors.toList());
            
            if (!conflicts.isEmpty()) {
                System.out.println("   ❌ Found " + conflicts.size() + " conflicting booking(s)");
                return true;
            }
            
            System.out.println("   ✅ No conflicts found");
            return false;
            
        } catch (Exception ex) {
            System.err.println("   ⚠️ Error checking booking conflict: " + ex.getMessage());
            ex.printStackTrace();
            // ✅ Jika error, anggap ada conflict untuk safety
            return true;
        }
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
        propertyCode = parts[parts.length - 2]; 
        roomNumber = parts[parts.length - 1];    
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
        
        // ✅ Simpan data LAMA untuk hitung refund/extra pay
        int oldTotalPrice = booking.getTotalPrice();
        int oldStatus = booking.getStatus();
        
        System.out.println("📝 Updating booking:");
        System.out.println("   Booking ID: " + bookingID);
        System.out.println("   Old Status: " + oldStatus);
        System.out.println("   Old Total Price: Rp " + oldTotalPrice);
        
        // ✅ Validate dates
        if (dto.getCheckOutDate().isBefore(dto.getCheckInDate()) ||
            dto.getCheckOutDate().isEqual(dto.getCheckInDate())) {
            throw new RuntimeException("Check-out date must be at least 1 day after check-in date");
        }
        
        // ✅ Normalize dates: untuk perhitungan (00:00) dan untuk storage (14:00 & 12:00)
        LocalDateTime[] normalizedDates = normalizeBookingDates(dto.getCheckInDate(), dto.getCheckOutDate());
        LocalDateTime checkInForCalculation = normalizedDates[0];
        LocalDateTime checkOutForCalculation = normalizedDates[1];
        LocalDateTime checkInForStorage = normalizedDates[2];
        LocalDateTime checkOutForStorage = normalizedDates[3];
        
        // ✅ FIX: Hitung selisih hari dengan benar menggunakan tanggal dengan jam 00:00:00
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(
            checkInForCalculation.toLocalDate(), 
            checkOutForCalculation.toLocalDate()
        );
        
        if (totalDays < 1) {
            totalDays = 1;
        }
        
        System.out.println("📅 Calculating booking duration:");
        System.out.println("   Check-in (for calculation): " + checkInForCalculation);
        System.out.println("   Check-out (for calculation): " + checkOutForCalculation);
        System.out.println("   Total Days: " + totalDays);
        System.out.println("   Check-in (for storage): " + checkInForStorage);
        System.out.println("   Check-out (for storage): " + checkOutForStorage);
        
        // ✅ Get Room (baru atau lama)
        Room newRoom = null;
        boolean roomChanged = false;
        
        if (dto.getRoomID() != null && !dto.getRoomID().isEmpty()) {
            newRoom = roomRepository.findById(dto.getRoomID())
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + dto.getRoomID()));
            
            if (booking.getRoom() == null || !booking.getRoom().getRoomID().equals(dto.getRoomID())) {
                roomChanged = true;
                System.out.println("🔄 Room changed:");
                System.out.println("   Old Room: " + (booking.getRoom() != null ? booking.getRoom().getRoomID() : "NULL"));
                System.out.println("   New Room: " + newRoom.getRoomID());
            }
        } else {
            newRoom = booking.getRoom();
        }
        
        if (newRoom == null) {
            throw new RuntimeException("Room is required for booking");
        }
        
        // ✅ FIX: Calculate NEW total price dengan long untuk menghindari overflow
        long basePrice = newRoom.getRoomType().getPrice();
        long breakfastPrice = (dto.getIsBreakfast() != null && dto.getIsBreakfast()) ? 50000L : 0L;
        long newTotalPriceLong = (basePrice + breakfastPrice) * totalDays;
        
        // ✅ Validasi agar tidak melebihi max int
        if (newTotalPriceLong > Integer.MAX_VALUE) {
            throw new RuntimeException("Total price exceeds maximum value. Please reduce booking duration.");
        }
        
        int newTotalPrice = (int) newTotalPriceLong;
        
        System.out.println("💰 Price calculation:");
        System.out.println("   Base Price: Rp " + basePrice + " per night");
        System.out.println("   Breakfast: Rp " + breakfastPrice + " per night");
        System.out.println("   Total Days: " + totalDays + " nights");
        System.out.println("   New Total Price: Rp " + newTotalPrice);
        
        // ✅ FIX: Auto-calculate refund/extra pay berdasarkan selisih harga
        int newStatus = oldStatus;
        int newExtraPay = 0;
        int newRefund = 0;
        
        // ✅ HANYA hitung refund/extra pay jika booking sudah pernah dibayar (status 1)
        if (oldStatus == 1) {
            int priceDifference = newTotalPrice - oldTotalPrice;
            
            if (priceDifference > 0) {
                // ✅ Harga NAIK → Extra Pay
                newExtraPay = priceDifference;
                newStatus = 0; // Status berubah ke "Waiting for Payment"
                
                System.out.println("📈 Price increased:");
                System.out.println("   Difference: +Rp " + priceDifference);
                System.out.println("   Extra Pay: Rp " + newExtraPay);
                System.out.println("   Status changed: 1 → 0 (Waiting for Payment)");
                
            } else if (priceDifference < 0) {
                // ✅ Harga TURUN → Refund
                newRefund = Math.abs(priceDifference); // Selisih harga (positif)
                newStatus = 3; // Status berubah ke "Request Refund"
                
                System.out.println("📉 Price decreased:");
                System.out.println("   Difference: -Rp " + Math.abs(priceDifference));
                System.out.println("   Refund: Rp " + newRefund);
                System.out.println("   Status changed: 1 → 3 (Request Refund)");
                System.out.println("   ⚠️ Income NOT changed yet (Rp " + oldTotalPrice + " still in income)");
                
            } else {
                // ✅ Harga SAMA
                System.out.println("   Price unchanged, no extra pay or refund");
            }
            
        } else if (oldStatus == 0) {
            // ✅ Status 0: Tetap status 0, keep extra pay yang lama
            newExtraPay = booking.getExtraPay();
            System.out.println("   Status 0: Keep existing extra pay (Rp " + newExtraPay + ")");
            
        } else if (oldStatus == 3) {
            // ✅ Status 3: Tetap status 3, keep refund yang lama
            newRefund = booking.getRefund();
            newStatus = 3;
            System.out.println("   Status 3: Keep existing refund (Rp " + newRefund + ")");
        }
        
        // ✅ Jika room berubah, buat booking BARU dengan ID baru
        if (roomChanged) {
            LocalDateTime createdDate = booking.getCreatedDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
            String datetime = createdDate.format(formatter);
            
            String[] parts = newRoom.getRoomID().split("-");
            String propertyCode = parts[parts.length - 2];
            String roomNumber = parts[parts.length - 1];
            String newBookingID = String.format("BOOK-%s-%s-%s", propertyCode, roomNumber, datetime);
            
            System.out.println("🆔 Creating new booking with new room:");
            System.out.println("   New Booking ID: " + newBookingID);
            
            Booking newBooking = Booking.builder()
                    .bookingID(newBookingID)
                    .checkInDate(checkInForStorage)  // ✅ Gunakan checkInForStorage (jam 14:00)
                    .checkOutDate(checkOutForStorage)  // ✅ Gunakan checkOutForStorage (jam 12:00)
                    .totalDays((int) totalDays)
                    .totalPrice(newTotalPrice)
                    .status(newStatus)
                    .customerID(booking.getCustomerID())
                    .customerName(dto.getCustomerName())
                    .customerEmail(dto.getCustomerEmail())
                    .customerPhone(dto.getCustomerPhone())
                    .isBreakfast(dto.getIsBreakfast() != null ? dto.getIsBreakfast() : false)
                    .capacity(dto.getCapacity())
                    .refund(newRefund) // ✅ Set refund otomatis
                    .extraPay(newExtraPay) // ✅ Set extra pay otomatis
                    .room(newRoom)
                    .createdDate(createdDate) // ✅ Keep created date lama
                    .updatedDate(LocalDateTime.now())
                    .build();
            
            bookingRepository.deleteById(bookingID);
            Booking savedBooking = bookingRepository.save(newBooking);
            
            System.out.println("✅ New booking created:");
            System.out.println("   Booking ID: " + savedBooking.getBookingID());
            System.out.println("   Status: " + newStatus);
            System.out.println("   Extra Pay: Rp " + newExtraPay);
            System.out.println("   Refund: Rp " + newRefund);
            
            return convertToResponseDTO(savedBooking);
            
        } else {
            // ✅ Room SAMA, update existing booking
            booking.setCheckInDate(checkInForStorage);  // ✅ Gunakan checkInForStorage (jam 14:00)
            booking.setCheckOutDate(checkOutForStorage);  // ✅ Gunakan checkOutForStorage (jam 12:00)
            booking.setTotalDays((int) totalDays);
            booking.setTotalPrice(newTotalPrice);
            booking.setCustomerName(dto.getCustomerName());
            booking.setCustomerEmail(dto.getCustomerEmail());
            booking.setCustomerPhone(dto.getCustomerPhone());
            booking.setCapacity(dto.getCapacity());
            booking.setBreakfast(dto.getIsBreakfast() != null ? dto.getIsBreakfast() : false);
            booking.setStatus(newStatus); // ✅ Update status
            booking.setExtraPay(newExtraPay); // ✅ Set extra pay otomatis
            booking.setRefund(newRefund); // ✅ Set refund otomatis
            booking.setUpdatedDate(LocalDateTime.now());
            
            Booking updatedBooking = bookingRepository.save(booking);
            
            System.out.println("✅ Booking updated:");
            System.out.println("   Booking ID: " + updatedBooking.getBookingID());
            System.out.println("   Status: " + newStatus);
            System.out.println("   Extra Pay: Rp " + newExtraPay);
            System.out.println("   Refund: Rp " + newRefund);
            
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
        
        System.out.println("🔄 Auto-updating booking statuses...");
        System.out.println("   Current Time: " + now);
        
        // ✅ Ambil semua bookings
        List<Booking> allBookings = bookingRepository.findAll();
        
        int updatedCount = 0;
        int doneCount = 0;
        int cancelledCount = 0;
        
        for (Booking booking : allBookings) {
            boolean wasUpdated = false;
            
            // ✅ RULE 1: Status 1 (Payment Confirmed) → 4 (Done) jika sudah check-in
            if (booking.getStatus() == 1 && booking.getCheckInDate().isBefore(now)) {
                System.out.println("📅 Booking " + booking.getBookingID() + ": Status 1 → 4 (Done)");
                System.out.println("   Check-in: " + booking.getCheckInDate() + " < Now: " + now);
                
                booking.setStatus(4); // Done
                booking.setUpdatedDate(now);
                wasUpdated = true;
                doneCount++;
            }
            
            // ✅ RULE 2: Status 0 (Waiting for Payment) → 2 (Cancelled) jika lewat check-in
            else if (booking.getStatus() == 0 && booking.getCheckInDate().isBefore(now)) {
                System.out.println("🚫 Booking " + booking.getBookingID() + ": Status 0 → 2 (Cancelled)");
                System.out.println("   Check-in: " + booking.getCheckInDate() + " < Now: " + now);
                
                booking.setStatus(2); // Cancelled
                booking.setUpdatedDate(now);
                
                // ✅ FIX: Kurangi income sesuai aturan cancel (sama seperti manual cancel)
                if (booking.getExtraPay() > 0) {
                    // Ada extra pay yang sudah dibayar → kurangi totalPriceLama dari income
                    int totalPriceLama = booking.getTotalPrice() - booking.getExtraPay();
                    System.out.println("   💰 Reducing income by total price LAMA: Rp " + totalPriceLama);
                    updatePropertyIncome(booking, -totalPriceLama);
                } else {
                    // Tidak ada extra pay → tidak ada income yang dikurangi
                    System.out.println("   ℹ️ No income change (no payment yet)");
                }
                
                wasUpdated = true;
                cancelledCount++;
            }
            
            // ✅ RULE 3: Status 3 (Request Refund) → 2 (Cancelled) jika lewat check-in
            //    DAN kurangi refund dari property income
            else if (booking.getStatus() == 3 && booking.getCheckInDate().isBefore(now)) {
                System.out.println("🚫 Booking " + booking.getBookingID() + ": Status 3 → 2 (Cancelled with refund)");
                System.out.println("   Check-in: " + booking.getCheckInDate() + " < Now: " + now);
                System.out.println("   Refund Amount: Rp " + booking.getRefund());
                
                booking.setStatus(2); // Cancelled (bukan Done)
                booking.setUpdatedDate(now);
                
                // ✅ Kurangi property income: total price LAMA (totalPrice + refund)
                int totalPriceOld = booking.getTotalPrice() + booking.getRefund();
                System.out.println("   💰 Total Price OLD: Rp " + totalPriceOld);
                System.out.println("   💰 Reducing income by total price OLD: Rp " + totalPriceOld);
                updatePropertyIncome(booking, -totalPriceOld);
                
                wasUpdated = true;
                cancelledCount++;
            }
            
            // ✅ Save booking jika ada perubahan
            if (wasUpdated) {
                bookingRepository.save(booking);
                updatedCount++;
            }
        }
        
        System.out.println("✅ Auto-update complete:");
        System.out.println("   Total Bookings Checked: " + allBookings.size());
        System.out.println("   Total Updated: " + updatedCount);
        System.out.println("   Status 1 → 4 (Done): " + doneCount);
        System.out.println("   Status 0/3 → 2 (Cancelled): " + cancelledCount);
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
    
    System.out.println("Cancelling booking:");
    System.out.println("   Booking ID: " + bookingID);
    System.out.println("   Current Status: " + currentStatus);
    System.out.println("   Total Price: Rp " + booking.getTotalPrice());
    System.out.println("   Extra Pay: Rp " + booking.getExtraPay());
    System.out.println("   Refund: Rp " + booking.getRefund());
    
    // ✅ FIX: Logic pengurangan income berdasarkan status sebelumnya
    if (currentStatus == 0) {
        if (booking.getExtraPay() > 0) {
            // ✅ Status 0 dengan extra pay
            // Income saat ini = totalPriceLama (yang sudah dibayar pertama kali)
            // TotalPriceLama = totalPrice - extraPay
            // Cancel = kurangi totalPriceLama = (totalPrice - extraPay)
            int totalPriceLama = booking.getTotalPrice() - booking.getExtraPay();
            updatePropertyIncome(booking, -totalPriceLama);
            
            System.out.println("   Total Price LAMA: Rp " + totalPriceLama);
            System.out.println("   Income change: -Rp " + totalPriceLama + " (total harga baru - extra pay)");
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
public BookingResponseDTO processRefund(String bookingID, UpdateBookingStatusRequestDTO dto) {
    Booking booking = bookingRepository.findById(bookingID)
            .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingID));
    
    // ✅ Validasi: Hanya booking dengan status 3 (Request Refund) yang bisa process refund
    if (booking.getStatus() != 3) {
        throw new RuntimeException("Only bookings with status 'Request Refund' can process refund");
    }
    
    // ✅ Validasi: Harus ada refund amount
    if (booking.getRefund() <= 0) {
        throw new RuntimeException("No refund amount to process");
    }
    
    System.out.println("Processing refund:");
    System.out.println("   Booking ID: " + bookingID);
    System.out.println("   Refund Amount: Rp " + booking.getRefund());
    System.out.println("   Check-in Date: " + booking.getCheckInDate());
    System.out.println("   Current Time: " + LocalDateTime.now());
    
    // ✅ Kurangi property income sejumlah refund
    updatePropertyIncome(booking, -booking.getRefund());
    
    // ✅ FIX: Cek apakah sudah check-in atau belum
    LocalDateTime now = LocalDateTime.now();
    int newStatus;
    
    if (booking.getCheckInDate().isBefore(now)) {
        // ✅ Sudah melewati check-in → Status menjadi 2 (Cancelled)
        newStatus = 4;
        System.out.println("   ⚠️ Check-in date has passed, status changed to Cancelled");
    } else {
        // ✅ Belum check-in → Status menjadi 1 (Payment Confirmed)
        newStatus = 1;
        System.out.println("   ✅ Check-in date has not passed, status changed to Payment Confirmed");
    }
    
    // ✅ Update status
    booking.setStatus(newStatus);
    booking.setUpdatedDate(LocalDateTime.now());
    
    Booking updatedBooking = bookingRepository.save(booking);
    
    System.out.println("✅ Refund processed, status changed to " + 
        (newStatus == 2 ? "Cancelled" : "Payment Confirmed"));
    
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
        // ✅ FIX: Extract HANYA nomor unit dari Room ID
        // Room ID format: HOT-4000-001-201
        // Ambil bagian terakhir setelah split: 201
        String roomID = booking.getRoom().getRoomID();
        if (roomID != null && roomID.contains("-")) {
            String[] parts = roomID.split("-");
            roomName = parts[parts.length - 1]; // ✅ Ambil bagian terakhir: "201"
        } else {
            roomName = roomID; // Fallback jika format tidak sesuai
        }
        
        System.out.println("🏠 Converting booking to DTO:");
        System.out.println("   Full Room ID: " + roomID);
        System.out.println("   Extracted Room Name: " + roomName);
        
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
            .roomName(roomName) // ✅ Sekarang hanya "201"
            .roomID(booking.getRoom() != null ? booking.getRoom().getRoomID() : null) // Full ID tetap ada
            .createdDate(booking.getCreatedDate())
            .updatedDate(booking.getUpdatedDate())
            .build();
}


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

    @Override
public List<BookingChartResponseDTO> getBookingChartData(Integer month, Integer year) {
    System.out.println(" Generating booking chart data:");
    System.out.println("   Month: " + month);
    System.out.println("   Year: " + year);
    
    // ✅ Ambil semua bookings dengan status 4 (Done)
    List<Booking> doneBookings = bookingRepository.findByStatus(4);
    
    // ✅ Filter berdasarkan bulan dan tahun dari check-in date
    List<Booking> filteredBookings = doneBookings.stream()
            .filter(b -> {
                if (b.getCheckInDate() == null) return false;
                
                int bookingMonth = b.getCheckInDate().getMonthValue();
                int bookingYear = b.getCheckInDate().getYear();
                
                boolean matchMonth = (month == null || bookingMonth == month);
                boolean matchYear = (year == null || bookingYear == year);
                
                return matchMonth && matchYear;
            })
            .collect(Collectors.toList());
    
    System.out.println("   Total Done Bookings: " + doneBookings.size());
    System.out.println("   Filtered Bookings: " + filteredBookings.size());
    
    // ✅ Group by property dan hitung total income
    Map<String, BookingChartResponseDTO> propertyIncomeMap = new HashMap<>();
    
    for (Booking booking : filteredBookings) {
        if (booking.getRoom() == null || 
            booking.getRoom().getRoomType() == null || 
            booking.getRoom().getRoomType().getProperty() == null) {
            continue;
        }
        
        var property = booking.getRoom().getRoomType().getProperty();
        String propertyID = property.getPropertyID();
        
        // ✅ Get atau create entry untuk property ini
        BookingChartResponseDTO chartData = propertyIncomeMap.getOrDefault(
            propertyID,
            BookingChartResponseDTO.builder()
                .propertyID(propertyID)
                .propertyName(property.getPropertyName())
                .totalIncome(0)
                .totalBookings(0)
                .month(month)
                .year(year)
                .build()
        );
        
        // ✅ Update total income dan total bookings
        chartData.setTotalIncome(chartData.getTotalIncome() + booking.getTotalPrice());
        chartData.setTotalBookings(chartData.getTotalBookings() + 1);
        
        propertyIncomeMap.put(propertyID, chartData);
    }
    
    // ✅ Convert map to list dan sort by income (descending)
    List<BookingChartResponseDTO> result = propertyIncomeMap.values().stream()
            .sorted((a, b) -> b.getTotalIncome().compareTo(a.getTotalIncome()))
            .collect(Collectors.toList());
    
    System.out.println("   Properties found: " + result.size());
    for (BookingChartResponseDTO data : result) {
        System.out.println("   - " + data.getPropertyName() + ": Rp " + data.getTotalIncome() + 
                         " (" + data.getTotalBookings() + " bookings)");
    }
    
    return result;
}


}