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
@Builder(toBuilder = true)
@Entity
@Table(name = "room")
public class Room {

    @Id
    @Column(name = "room_id", nullable = false, length = 36)
    private String roomID;

    @Column(name = "name", nullable = false)
    private String name; 

    @Column(name = "availability_status", nullable = false)
    private int availabilityStatus; 

    @Column(name = "active_room", nullable = false)
    private int activeRoom; 

    @Column(name = "maintenance_start")
    private LocalDateTime maintenanceStart; 

    @Column(name = "maintenance_end")
    private LocalDateTime maintenanceEnd; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", referencedColumnName = "room_type_id")
    private RoomType roomType;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        if (roomID == null || roomID.isBlank()) {
            roomID = UUID.randomUUID().toString();
        }
        createdDate = LocalDateTime.now();
        updatedDate = createdDate;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}