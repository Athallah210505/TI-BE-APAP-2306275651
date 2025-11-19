package apap.ti._5.accommodation_2306275651_be.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accommodation_review")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationReview {

    @Id
    @Column(name = "review_id", nullable = false, unique = true, length = 64)
    private String reviewID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "overall_rating", nullable = false)
    private Double overallRating;

    @Column(name = "cleanliness_rating", nullable = false)
    private Integer cleanlinessRating;

    @Column(name = "facility_rating", nullable = false)
    private Integer facilityRating;

    @Column(name = "service_rating", nullable = false)
    private Integer serviceRating;

    @Column(name = "value_rating", nullable = false)
    private Integer valueRating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
}
