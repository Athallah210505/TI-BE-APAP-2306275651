package apap.ti._5.accommodation_2306275651_be.restservice;

import java.util.List;

import apap.ti._5.accommodation_2306275651_be.restdto.request.review.CreateAccommodationReviewRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.review.AccommodationReviewResponseDTO;

public interface AccommodationReviewRestService {
    List<AccommodationReviewResponseDTO> getReviewsByProperty(String propertyID, String ownerID);
    List<AccommodationReviewResponseDTO> getReviewsByCustomer(String customerID);
    AccommodationReviewResponseDTO getReviewDetail(String reviewID, String ownerID);
    AccommodationReviewResponseDTO createReview(CreateAccommodationReviewRequestDTO dto, String customerID);
}
