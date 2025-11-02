package apap.ti._5.accommodation_2306275651_be.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import apap.ti._5.accommodation_2306275651_be.model.Property;

@Repository
public interface PropertyRepository extends JpaRepository<Property, String> {
    List<Property> findByOwnerID(UUID ownerID);
    List<Property> findByActiveStatus(int activeStatus);
    List<Property> findByType(int type);
}