package apap.ti._5.accommodation_2306275651_be.restservice;

import java.util.List;

import apap.ti._5.accommodation_2306275651_be.restdto.request.booking.CreateBookingRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.booking.UpdateBookingRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.booking.UpdateBookingStatusRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.booking.BookingResponseDTO;

public interface BookingRestService {
    BookingResponseDTO createBooking(CreateBookingRequestDTO dto);
    BookingResponseDTO getBookingById(String bookingID);
    List<BookingResponseDTO> getAllBookings();
    List<BookingResponseDTO> getBookingsByCustomer(String customerID);
    List<BookingResponseDTO> getBookingsByStatus(int status);
    BookingResponseDTO updateBooking(String bookingID, UpdateBookingRequestDTO dto);
    BookingResponseDTO updateBookingStatus(String bookingID, UpdateBookingStatusRequestDTO dto);
    void deleteBooking(String bookingID);
    void autoUpdateBookingStatuses();
      boolean hasBookingConflict(String roomID, String startDate, String endDate);
    
    // ✅ TAMBAHKAN method ini untuk exclude booking yang sedang diupdate
    boolean hasBookingConflictExcluding(String roomID, String startDate, String endDate, String excludeBookingID);
    BookingResponseDTO confirmPayment(String bookingID, UpdateBookingStatusRequestDTO dto);
    BookingResponseDTO cancelBooking(String bookingID, UpdateBookingStatusRequestDTO dto);
    BookingResponseDTO requestRefund(String bookingID, UpdateBookingStatusRequestDTO dto);
    
}