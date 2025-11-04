package apap.ti._5.accommodation_2306275651_be.restdto.response.booking;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private String bookingID;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private Integer totalDays;
    private Integer totalPrice;
    private Integer status;
    private String statusName; // "Waiting for Payment", "Payment Confirmed", etc
    private String customerID;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Boolean isBreakfast;
    private Integer refund;
    private Integer extraPay;
    private Integer capacity;

    private String propertyName;
    private String roomName;
    private String roomID;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}