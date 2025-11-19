package apap.ti._5.accommodation_2306275651_be.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import apap.ti._5.accommodation_2306275651_be.model.AccommodationReview;
import apap.ti._5.accommodation_2306275651_be.model.Booking;
import apap.ti._5.accommodation_2306275651_be.model.Property;

public interface AccommodationReviewRepository extends JpaRepository<AccommodationReview, String> {
    List<AccommodationReview> findByProperty(Property property);
    List<AccommodationReview> findByBooking_CustomerID(UUID customerID);
    Optional<AccommodationReview> findByBooking(Booking booking);
}
