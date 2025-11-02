package apap.ti._5.accommodation_2306275651_be.repository;

import apap.ti._5.accommodation_2306275651_be.model.AccommodationBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccommodationBookingRepository extends JpaRepository<AccommodationBooking, String> {
    List<AccommodationBooking> findByCustomerID(UUID customerID);
    List<AccommodationBooking> findByStatus(int status);
    List<AccommodationBooking> findByCustomerEmail(String email);
}