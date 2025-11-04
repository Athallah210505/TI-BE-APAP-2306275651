// ...existing code...
package apap.ti._5.accommodation_2306275651_be.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @Column(name = "booking_id", nullable = false, length = 36)
    private String bookingID; // Primary Key (UUID string)

    @Column(name = "check_in_date")
    private LocalDateTime checkInDate;

    @Column(name = "check_out_date")
    private LocalDateTime checkOutDate;

    @Column(name = "total_days")
    private int totalDays;

    @Column(name = "total_price")
    private int totalPrice;

    @Column(name = "status")
    private int status; // 0..4 per spesifikasi

    @Column(name = "customer_id", columnDefinition = "uuid")
    private UUID customerID;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "is_breakfast")
    private boolean isBreakfast;

    @Column(name = "refund_amount")
    private int refund;

    @Column(name = "extra_pay")
    private int extraPay;

    @Column(name = "capacity")
    private int capacity;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room; 

     

    @PrePersist
    protected void onCreate() {
        if (bookingID == null || bookingID.isBlank()) {
            bookingID = UUID.randomUUID().toString();
        }
         if (this.createdDate == null) {
        this.createdDate = LocalDateTime.now();
    }
        updatedDate = createdDate;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
