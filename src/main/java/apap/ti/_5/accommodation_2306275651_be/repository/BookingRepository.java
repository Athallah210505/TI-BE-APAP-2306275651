package apap.ti._5.accommodation_2306275651_be.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import apap.ti._5.accommodation_2306275651_be.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByCustomerID(UUID customerID);
    List<Booking> findByStatus(int status);
    List<Booking> findByCustomerEmail(String email);
    @Query("SELECT b FROM Booking b WHERE b.room.roomID = :roomID " +
           "AND b.status = 1 " +
           "AND (" +
           "  (b.checkInDate < :endDate AND b.checkOutDate > :startDate)" +
           ")")
    List<Booking> findConflictingBookings(
        @Param("roomID") String roomID,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    List<Booking> findByRoom_RoomIDAndStatus(String roomID, Integer status);
}