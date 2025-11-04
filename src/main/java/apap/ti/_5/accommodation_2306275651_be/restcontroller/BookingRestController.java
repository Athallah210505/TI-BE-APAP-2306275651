package apap.ti._5.accommodation_2306275651_be.restcontroller;

import apap.ti._5.accommodation_2306275651_be.model.Room;
import apap.ti._5.accommodation_2306275651_be.restdto.request.booking.CreateBookingRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.booking.UpdateBookingRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.booking.UpdateBookingStatusRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.BaseResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.booking.BookingResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.room.RoomResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restservice.BookingRestService;
import apap.ti._5.accommodation_2306275651_be.restservice.RoomRestService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingRestController {
    
    @Autowired
    private BookingRestService bookingRestService;
    
    @Autowired
    private RoomRestService roomRestService;
    
    // ✅ URL Constants
    public static final String BASE_URL = "/bookings";
    public static final String VIEW_BOOKING = BASE_URL + "/{id}";
    public static final String CREATE_BOOKING = BASE_URL + "/create";
    public static final String CREATE_BOOKING_WITH_ROOM = BASE_URL + "/create/{idRoom}";
    public static final String UPDATE_BOOKING = BASE_URL + "/update/{id}";
    public static final String UPDATE_BOOKING_STATUS = BASE_URL + "/update-status/{id}";
    public static final String DELETE_BOOKING = BASE_URL + "/delete/{id}";
    public static final String BOOKING_BY_CUSTOMER = BASE_URL + "/customer/{customerId}";
    public static final String BOOKING_BY_STATUS = BASE_URL + "/status/{status}";
    public static final String PAY_BOOKING = BASE_URL + "/status/pay";
    public static final String CANCEL_BOOKING = BASE_URL + "/status/cancel";
    public static final String REFUND_BOOKING = BASE_URL + "/status/refund";
    
    
    // ✅ GET Form Create Booking WITH Room (prefilled)
    @GetMapping(CREATE_BOOKING_WITH_ROOM)
    public ResponseEntity<BaseResponseDTO<RoomResponseDTO>> getCreateBookingFormWithRoom(
            @PathVariable String idRoom,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        var baseResponseDTO = new BaseResponseDTO<RoomResponseDTO>();
        
        try {
            // ✅ Get room details untuk prefill form
            RoomResponseDTO room = roomRestService.getRoomById(idRoom);
            
            if (room == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Kamar tidak ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }
            
            // ✅ Validate room availability
            if (room.getAvailabilityStatus() == 0) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("Kamar sedang tidak tersedia");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
            
            // ✅ Check booking conflict jika ada startDate & endDate
            if (startDate != null && endDate != null) {
                boolean hasConflict = roomRestService.hasBookingConflict(idRoom, startDate, endDate);
                if (hasConflict) {
                    baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                    baseResponseDTO.setMessage("Kamar sudah dibooking pada tanggal tersebut");
                    baseResponseDTO.setTimestamp(new Date());
                    return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
                }
            }
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(room);
            baseResponseDTO.setMessage("Data kamar untuk booking berhasil ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // ✅ POST Create Booking (both with/without room)
    @PostMapping(CREATE_BOOKING)
    public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> createBooking(
            @Valid @RequestBody CreateBookingRequestDTO request,
            BindingResult bindingResult) {
        
        var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();
        
        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }
            
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        try {
            // ✅ Validasi: Check-out harus setelah check-in
            if (request.getCheckOutDate().isBefore(request.getCheckInDate()) ||
                request.getCheckOutDate().isEqual(request.getCheckInDate())) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("❌ Konfirmasi: Tanggal check-out harus minimal 1 hari setelah check-in");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
            
            // ✅ Validasi: Check-in minimal hari ini
            if (request.getCheckInDate().isBefore(LocalDateTime.now())) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("❌ Konfirmasi: Tanggal check-in tidak boleh di masa lalu");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
            
            // ✅ Validasi: Cek room availability & capacity
            if (request.getRoomID() != null && !request.getRoomID().isEmpty()) {
                RoomResponseDTO room = roomRestService.getRoomById(request.getRoomID());
                
                if (room == null) {
                    baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                    baseResponseDTO.setMessage("❌ Konfirmasi: Kamar tidak ditemukan");
                    baseResponseDTO.setTimestamp(new Date());
                    return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
                }
                
                // ✅ Check capacity
                if (request.getCapacity() > room.getCapacity()) {
                    baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                    baseResponseDTO.setMessage("❌ Konfirmasi: Kapasitas tamu (" + request.getCapacity() + 
                                              ") melebihi kapasitas kamar (" + room.getCapacity() + ")");
                    baseResponseDTO.setTimestamp(new Date());
                    return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
                }
                
                // ✅ Check booking conflict
                boolean hasConflict = roomRestService.hasBookingConflict(
                    request.getRoomID(),
                    request.getCheckInDate().toString(),
                    request.getCheckOutDate().toString()
                );
                
                if (hasConflict) {
                    baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                    baseResponseDTO.setMessage("❌ Konfirmasi: Kamar sudah dibooking atau sedang maintenance pada tanggal tersebut");
                    baseResponseDTO.setTimestamp(new Date());
                    return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
                }
                
                // ✅ Check maintenance schedule
                if (room.getMaintenanceStart() != null && room.getMaintenanceEnd() != null) {
                    if (!(request.getCheckOutDate().isBefore(room.getMaintenanceStart()) ||
                          request.getCheckInDate().isAfter(room.getMaintenanceEnd()))) {
                        baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                        baseResponseDTO.setMessage("❌ Konfirmasi: Kamar sedang dalam jadwal maintenance pada tanggal tersebut");
                        baseResponseDTO.setTimestamp(new Date());
                        return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
                    }
                }
            }
            
            // ✅ Create booking
            BookingResponseDTO response = bookingRestService.createBooking(request);
            
            baseResponseDTO.setStatus(HttpStatus.CREATED.value());
            baseResponseDTO.setData(response);
            baseResponseDTO.setMessage("✅ Konfirmasi: Booking berhasil dibuat dengan ID " + response.getBookingID());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("❌ Konfirmasi: Gagal membuat booking. Error: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping(VIEW_BOOKING)
    public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> getBooking(@PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();
        
        try {
            BookingResponseDTO response = bookingRestService.getBookingById(id);
            
            if (response == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Booking Tidak Ditemukan");
    
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(response);
            baseResponseDTO.setMessage("Detail Booking Berhasil Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping(BASE_URL)
    public ResponseEntity<BaseResponseDTO<List<BookingResponseDTO>>> getAllBookings(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String search) {
        
        var baseResponseDTO = new BaseResponseDTO<List<BookingResponseDTO>>();
        
        try {
            // ✅ Auto-update status SEBELUM fetch data
            bookingRestService.autoUpdateBookingStatuses();
            
            // ✅ Fetch all bookings
            List<BookingResponseDTO> bookings = bookingRestService.getAllBookings();
            
            // ✅ Filter by status (jika ada)
            if (status != null) {
                bookings = bookings.stream()
                        .filter(b -> b.getStatus().equals(status))
                        .collect(Collectors.toList());
            }
            
            // ✅ Filter by search (property name atau room name)
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                bookings = bookings.stream()
                        .filter(b -> 
                            (b.getPropertyName() != null && b.getPropertyName().toLowerCase().contains(searchLower)) ||
                            (b.getRoomName() != null && b.getRoomName().toLowerCase().contains(searchLower))
                        )
                        .collect(Collectors.toList());
            }
            
            // ✅ Sort by bookingID (ascending)
            bookings = bookings.stream()
                    .sorted((b1, b2) -> b1.getBookingID().compareTo(b2.getBookingID()))
                    .collect(Collectors.toList());
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(bookings);
            baseResponseDTO.setMessage("Data Booking Berhasil Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping(BOOKING_BY_CUSTOMER)
    public ResponseEntity<BaseResponseDTO<List<BookingResponseDTO>>> getBookingsByCustomer(
            @PathVariable String customerId) {
        
        var baseResponseDTO = new BaseResponseDTO<List<BookingResponseDTO>>();
        
        try {
            List<BookingResponseDTO> responses = bookingRestService.getBookingsByCustomer(customerId);
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(responses);
            baseResponseDTO.setMessage("Data Booking Customer Berhasil Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping(BOOKING_BY_STATUS)
    public ResponseEntity<BaseResponseDTO<List<BookingResponseDTO>>> getBookingsByStatus(
            @PathVariable int status) {
        
        var baseResponseDTO = new BaseResponseDTO<List<BookingResponseDTO>>();
        
        try {
            List<BookingResponseDTO> responses = bookingRestService.getBookingsByStatus(status);
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(responses);
            baseResponseDTO.setMessage("Data Booking dengan Status " + status + " Berhasil Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping(UPDATE_BOOKING)
    public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> getUpdateBookingForm(
            @PathVariable String id) {
        
        var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();
        
        try {
            // ✅ Get existing booking
            BookingResponseDTO booking = bookingRestService.getBookingById(id);
            
            if (booking == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Booking tidak ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }
            
            // ✅ Validasi: Hanya booking tanpa extra pay & refund yang bisa update
            if (booking.getExtraPay() != null && booking.getExtraPay() > 0) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("❌ Konfirmasi: Booking dengan extra pay tidak dapat diubah");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
            
            if (booking.getRefund() != null && booking.getRefund() > 0) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("❌ Konfirmasi: Booking dengan refund tidak dapat diubah");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(booking);
            baseResponseDTO.setMessage("Data booking berhasil ditemukan untuk update");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping(UPDATE_BOOKING)
public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> updateBooking(
        @PathVariable String id,
        @Valid @RequestBody UpdateBookingRequestDTO request,
        BindingResult bindingResult) {
    
    var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();
    
    if (bindingResult.hasFieldErrors()) {
        StringBuilder errorMessages = new StringBuilder();
        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            errorMessages.append(error.getDefaultMessage()).append("; ");
        }
        
        baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
        baseResponseDTO.setMessage(errorMessages.toString());
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
    }
    
    try {
        // ✅ Get existing booking untuk validasi extra pay & refund
        BookingResponseDTO existingBooking = bookingRestService.getBookingById(id);
        
        if (existingBooking == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Booking tidak ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        
        // ✅ Validasi: Tidak boleh update jika ada extra pay atau refund
        if ((existingBooking.getExtraPay() != null && existingBooking.getExtraPay() > 0) ||
            (existingBooking.getRefund() != null && existingBooking.getRefund() > 0)) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("❌ Konfirmasi: Booking dengan extra pay atau refund tidak dapat diubah");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        // ✅ Validasi: Check-out harus setelah check-in
        if (request.getCheckOutDate().isBefore(request.getCheckInDate()) ||
            request.getCheckOutDate().isEqual(request.getCheckInDate())) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("❌ Konfirmasi: Tanggal check-out harus minimal 1 hari setelah check-in");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        // ✅ Validasi: Check-in minimal hari ini
        if (request.getCheckInDate().isBefore(LocalDateTime.now())) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("❌ Konfirmasi: Tanggal check-in tidak boleh di masa lalu");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        // ✅ Validasi: Jika ada roomID baru, cek availability & capacity
        if (request.getRoomID() != null && !request.getRoomID().isEmpty()) {
            Room room = roomRestService.getRoomEntityById(request.getRoomID());
            
            if (room == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("❌ Konfirmasi: Kamar tidak ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }
            
            // ✅ Check capacity
            Integer roomCapacity = room.getRoomType() != null ? room.getRoomType().getCapacity() : null;
            if (roomCapacity != null && request.getCapacity() > roomCapacity) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("❌ Konfirmasi: Kapasitas tamu (" + request.getCapacity() + 
                                          ") melebihi kapasitas kamar (" + roomCapacity + ")");
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
            
            // // ✅ Check booking conflict (exclude booking yang sedang diupdate)
            // boolean hasConflict = roomRestService.hasBookingConflictExcluding(
            //     request.getRoomID(),
            //     request.getCheckInDate().toString(),
            //     request.getCheckOutDate().toString(),
            //     id // Exclude current booking ID
            // );
            
            // if (hasConflict) {
            //     baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            //     baseResponseDTO.setMessage("❌ Konfirmasi: Kamar sudah dibooking atau sedang maintenance pada tanggal tersebut");
            //     return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            // }
            
            // ✅ Check maintenance schedule
            if (room.getMaintenanceStart() != null && room.getMaintenanceEnd() != null) {
                if (!(request.getCheckOutDate().isBefore(room.getMaintenanceStart()) ||
                      request.getCheckInDate().isAfter(room.getMaintenanceEnd()))) {
                    baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                    baseResponseDTO.setMessage("❌ Konfirmasi: Kamar sedang dalam jadwal maintenance pada tanggal tersebut");
                    baseResponseDTO.setTimestamp(new Date());
                    return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
                }
            }
        }
        
        // ✅ Update booking
        BookingResponseDTO response = bookingRestService.updateBooking(id, request);
        
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(response);
        baseResponseDTO.setMessage("✅ Konfirmasi: Data Booking Berhasil Diupdate");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
        
    } catch (Exception ex) {
        baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        baseResponseDTO.setMessage("❌ Konfirmasi: Gagal update booking. Error: " + ex.getMessage());
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
    
    @PatchMapping(UPDATE_BOOKING_STATUS)
    public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> updateBookingStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateBookingStatusRequestDTO request,
            BindingResult bindingResult) {
        
        var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();
        
        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }
            
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        try {
            BookingResponseDTO response = bookingRestService.updateBookingStatus(id, request);
            
            if (response == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Booking Tidak Ditemukan");
    
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(response);
            baseResponseDTO.setMessage("✅ Konfirmasi: Status Booking Berhasil Diupdate");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("❌ Konfirmasi: Gagal update status. Error: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping(DELETE_BOOKING)
    public ResponseEntity<BaseResponseDTO<Void>> deleteBooking(@PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<Void>();
        
        try {
            bookingRestService.deleteBooking(id);
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setMessage("✅ Konfirmasi: Booking Berhasil Dihapus");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("❌ Konfirmasi: Gagal menghapus booking. Error: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping(PAY_BOOKING)
    public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> payBooking(
            @Valid @RequestBody UpdateBookingStatusRequestDTO request,
            BindingResult bindingResult) {
        
        var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();
        
        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }
            
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        try {
            BookingResponseDTO response = bookingRestService.confirmPayment(
                request.getBookingID(), 
                request
            );
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(response);
            baseResponseDTO.setMessage("✅ Konfirmasi: Pembayaran berhasil dikonfirmasi");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("❌ Konfirmasi: Gagal konfirmasi pembayaran. Error: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping(CANCEL_BOOKING)
    public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> cancelBooking(
            @Valid @RequestBody UpdateBookingStatusRequestDTO request,
            BindingResult bindingResult) {
        
        var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();
        
        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }
            
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        try {
            BookingResponseDTO response = bookingRestService.cancelBooking(
                request.getBookingID(), 
                request
            );
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(response);
            baseResponseDTO.setMessage("✅ Konfirmasi: Booking berhasil dibatalkan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("❌ Konfirmasi: Gagal membatalkan booking. Error: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // ✅ POST Refund Booking
    @PostMapping(REFUND_BOOKING)
    public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> refundBooking(
            @Valid @RequestBody UpdateBookingStatusRequestDTO request,
            BindingResult bindingResult) {
        
        var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();
        
        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }
            
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
        try {
            BookingResponseDTO response = bookingRestService.requestRefund(
                request.getBookingID(), 
                request
            );
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(response);
            baseResponseDTO.setMessage("✅ Konfirmasi: Refund berhasil diproses");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("❌ Konfirmasi: Gagal memproses refund. Error: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}