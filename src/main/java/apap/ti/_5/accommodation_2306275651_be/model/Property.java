package apap.ti._5.accommodation_2306275651_be.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;




import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "property")
public class Property {

    @Id
    @Column(name = "property_id", nullable = false, length = 36)
    private String propertyID;

    @Column(name = "property_name")
    private String propertyName;

    @Column(name = "type")
    private int type;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "province")
    private int province; // nilai dari API lokasi

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_room")
    private int totalRoom;

    @Column(name = "active_status")
    private int activeStatus; // 0 = NonActive, 1 = Active

    @Column(name = "income")
    private int income;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomType> listRoomType;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "owner_id", columnDefinition = "uuid")
    private UUID ownerID;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    

}