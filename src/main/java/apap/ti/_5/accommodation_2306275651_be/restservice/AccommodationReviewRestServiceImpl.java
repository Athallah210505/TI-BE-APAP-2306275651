package apap.ti._5.accommodation_2306275651_be.restservice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti._5.accommodation_2306275651_be.model.AccommodationReview;
import apap.ti._5.accommodation_2306275651_be.model.Booking;
import apap.ti._5.accommodation_2306275651_be.model.Property;
import apap.ti._5.accommodation_2306275651_be.repository.AccommodationReviewRepository;
import apap.ti._5.accommodation_2306275651_be.repository.BookingRepository;
import apap.ti._5.accommodation_2306275651_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306275651_be.restdto.request.review.CreateAccommodationReviewRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.review.AccommodationReviewResponseDTO;

@Service
@Transactional
public class AccommodationReviewRestServiceImpl implements AccommodationReviewRestService {

    private final AccommodationReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;

    public AccommodationReviewRestServiceImpl(
            AccommodationReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            PropertyRepository propertyRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
    }

    @Override
    public List<AccommodationReviewResponseDTO> getReviewsByProperty(String propertyID, String ownerID) {
        Property property = propertyRepository.findById(propertyID)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + propertyID));
        
        if (ownerID != null && !ownerID.isEmpty()) {
            if (!property.getOwnerID().toString().equals(ownerID)) {
                throw new RuntimeException("You can only view reviews for your own properties");
            }
        }
        
        return reviewRepository.findByProperty(property).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccommodationReviewResponseDTO> getReviewsByCustomer(String customerID) {
        UUID customerUUID = UUID.fromString(customerID);
        return reviewRepository.findByBooking_CustomerID(customerUUID).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AccommodationReviewResponseDTO getReviewDetail(String reviewID, String ownerID) {
        AccommodationReview review = reviewRepository.findById(reviewID)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewID));
        
        if (ownerID != null && !ownerID.isEmpty()) {
            if (!review.getProperty().getOwnerID().toString().equals(ownerID)) {
                throw new RuntimeException("You can only view review details for your own properties");
            }
        }
        
        return toResponse(review);
    }

    @Override
    public AccommodationReviewResponseDTO createReview(CreateAccommodationReviewRequestDTO dto, String customerID) {
        Booking booking = bookingRepository.findById(dto.getAccommodationBookingID())
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + dto.getAccommodationBookingID()));
        
        if (customerID == null || !booking.getCustomerID().toString().equals(customerID)) {
            throw new RuntimeException("You can only create reviews for your own bookings");
        }
        
        if (booking.getStatus() != 1 && booking.getStatus() != 4) {
            throw new RuntimeException("Can only review bookings with Payment Confirmed (1) or Done (4) status");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(booking.getCheckOutDate())) {
            throw new RuntimeException("Can only review bookings after checkout date");
        }
        
        if (reviewRepository.findByBooking(booking).isPresent()) {
            throw new RuntimeException("Review already exists for this booking");
        }
        
        double overallRating = calculateOverallRating(
            dto.getCleanlinessRating(),
            dto.getFacilityRating(),
            dto.getServiceRating(),
            dto.getValueRating()
        );
        
        String reviewID = UUID.randomUUID().toString();
        
        AccommodationReview review = AccommodationReview.builder()
                .reviewID(reviewID)
                .booking(booking)
                .property(booking.getRoom().getRoomType().getProperty())
                .overallRating(overallRating)
                .cleanlinessRating(dto.getCleanlinessRating())
                .facilityRating(dto.getFacilityRating())
                .serviceRating(dto.getServiceRating())
                .valueRating(dto.getValueRating())
                .comment(dto.getComment())
                .createdDate(LocalDateTime.now())
                .build();

        AccommodationReview saved = reviewRepository.save(review);
        return toResponse(saved);
    }

    private double calculateOverallRating(Integer cleanliness, Integer facility, Integer service, Integer value) {
        return (cleanliness + facility + service + value) / 4.0;
    }

    private AccommodationReviewResponseDTO toResponse(AccommodationReview review) {
        return AccommodationReviewResponseDTO.builder()
                .reviewID(review.getReviewID())
                .accommodationBookingID(review.getBooking().getBookingID())
                .overallRating(review.getOverallRating())
                .cleanlinessRating(review.getCleanlinessRating())
                .facilityRating(review.getFacilityRating())
                .serviceRating(review.getServiceRating())
                .valueRating(review.getValueRating())
                .comment(review.getComment())
                .createdDate(review.getCreatedDate())
                .build();
    }
}
