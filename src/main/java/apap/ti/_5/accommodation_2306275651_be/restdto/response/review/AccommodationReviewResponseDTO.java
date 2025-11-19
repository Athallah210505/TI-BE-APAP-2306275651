package apap.ti._5.accommodation_2306275651_be.restdto.response.review;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationReviewResponseDTO {
    private String reviewID;
    private String accommodationBookingID;
    private Double overallRating;
    private Integer cleanlinessRating;
    private Integer facilityRating;
    private Integer serviceRating;
    private Integer valueRating;
    private String comment;
    private LocalDateTime createdDate;
}
