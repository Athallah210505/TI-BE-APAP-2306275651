package apap.ti._5.accommodation_2306275651_be.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "room_type")
public class RoomType {

    @Id
    @Column(name = "room_type_id", nullable = false, length = 36)
    private String roomTypeID; // Primary Key (UUID string)

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private int price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "capacity")
    private int capacity;

    @Column(name = "facility", columnDefinition = "TEXT")
    private String facility;

    @Column(name = "floor")
    private int floor;

    @ManyToOne
    @JoinColumn(name = "property_id") // samakan dengan nama kolom PK di table property
    private Property property;

   @Builder.Default
    @OneToMany(mappedBy = "roomType")
    private List<Room> listRoom = new ArrayList<>();

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        if (roomTypeID == null || roomTypeID.isBlank()) {
            roomTypeID = UUID.randomUUID().toString();
        }
        createdDate = LocalDateTime.now();
        updatedDate = createdDate;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

}