package apap.ti._5.accommodation_2306275651_be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import apap.ti._5.accommodation_2306275651_be.model.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByRoomType_RoomTypeID(String roomTypeID);
    List<Room> findByAvailabilityStatus(int status);
    List<Room> findByActiveRoom(int activeRoom);
    @Query("SELECT r FROM Room r WHERE r.roomType.floor = :floor AND r.roomType.roomTypeID = :roomTypeID")
    List<Room> findByFloorAndRoomTypeID(@Param("floor") Integer floor, @Param("roomTypeID") String roomTypeID);
}