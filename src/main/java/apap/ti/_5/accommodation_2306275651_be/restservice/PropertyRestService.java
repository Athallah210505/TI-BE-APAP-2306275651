package apap.ti._5.accommodation_2306275651_be.restservice;

import java.util.List;

import apap.ti._5.accommodation_2306275651_be.restdto.request.property.CreatePropertyRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.request.property.UpdatePropertyRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.property.PropertyResponseDTO;

public interface PropertyRestService {
    PropertyResponseDTO createProperty(CreatePropertyRequestDTO dto);
    PropertyResponseDTO getPropertyById(String propertyID);
    List<PropertyResponseDTO> getAllProperties();
    List<PropertyResponseDTO> getPropertiesByOwner(String ownerID);
    PropertyResponseDTO updateProperty(String propertyID, UpdatePropertyRequestDTO dto);
    PropertyResponseDTO deleteProperty(String propertyID);
    
}