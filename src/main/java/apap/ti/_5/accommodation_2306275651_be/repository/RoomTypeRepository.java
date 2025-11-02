package apap.ti._5.accommodation_2306275651_be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import apap.ti._5.accommodation_2306275651_be.model.RoomType;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, String> {
    List<RoomType> findByProperty_PropertyID(String propertyID);
}