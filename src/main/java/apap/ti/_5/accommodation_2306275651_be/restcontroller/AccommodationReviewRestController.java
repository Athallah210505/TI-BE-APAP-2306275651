package apap.ti._5.accommodation_2306275651_be.restcontroller;

import apap.ti._5.accommodation_2306275651_be.restdto.request.review.CreateAccommodationReviewRequestDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.BaseResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restdto.response.review.AccommodationReviewResponseDTO;
import apap.ti._5.accommodation_2306275651_be.restservice.AccommodationReviewRestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AccommodationReviewRestController {

    private final AccommodationReviewRestService reviewService;

    public AccommodationReviewRestController(AccommodationReviewRestService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/reviews/property/{propertyId}")
    public ResponseEntity<BaseResponseDTO<List<AccommodationReviewResponseDTO>>> getByProperty(
            @PathVariable String propertyId,
            @RequestParam(required = false) String ownerId) {
        var base = new BaseResponseDTO<List<AccommodationReviewResponseDTO>>();
        try {
            var data = reviewService.getReviewsByProperty(propertyId, ownerId);
            base.setStatus(HttpStatus.OK.value());
            base.setMessage("Reviews fetched successfully");
            base.setTimestamp(new Date());
            base.setData(data);
            return new ResponseEntity<>(base, HttpStatus.OK);
        } catch (Exception ex) {
            base.setStatus(HttpStatus.BAD_REQUEST.value());
            base.setMessage(ex.getMessage());
            base.setTimestamp(new Date());
            return new ResponseEntity<>(base, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/reviews/customer/{customerId}")
    public ResponseEntity<BaseResponseDTO<List<AccommodationReviewResponseDTO>>> getByCustomer(
            @PathVariable String customerId) {
        var base = new BaseResponseDTO<List<AccommodationReviewResponseDTO>>();
        try {
            var data = reviewService.getReviewsByCustomer(customerId);
            base.setStatus(HttpStatus.OK.value());
            base.setMessage("Reviews fetched successfully");
            base.setTimestamp(new Date());
            base.setData(data);
            return new ResponseEntity<>(base, HttpStatus.OK);
        } catch (Exception ex) {
            base.setStatus(HttpStatus.BAD_REQUEST.value());
            base.setMessage(ex.getMessage());
            base.setTimestamp(new Date());
            return new ResponseEntity<>(base, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<BaseResponseDTO<AccommodationReviewResponseDTO>> getDetail(
            @PathVariable String reviewId,
            @RequestParam(required = false) String ownerId) {
        var base = new BaseResponseDTO<AccommodationReviewResponseDTO>();
        try {
            var data = reviewService.getReviewDetail(reviewId, ownerId);
            base.setStatus(HttpStatus.OK.value());
            base.setMessage("Review fetched successfully");
            base.setTimestamp(new Date());
            base.setData(data);
            return new ResponseEntity<>(base, HttpStatus.OK);
        } catch (Exception ex) {
            base.setStatus(HttpStatus.NOT_FOUND.value());
            base.setMessage(ex.getMessage());
            base.setTimestamp(new Date());
            return new ResponseEntity<>(base, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/reviews")
    public ResponseEntity<BaseResponseDTO<AccommodationReviewResponseDTO>> create(
            @Valid @RequestBody CreateAccommodationReviewRequestDTO request,
            @RequestParam String customerId) {
        var base = new BaseResponseDTO<AccommodationReviewResponseDTO>();
        try {
            var data = reviewService.createReview(request, customerId);
            base.setStatus(HttpStatus.CREATED.value());
            base.setMessage("Review created successfully");
            base.setTimestamp(new Date());
            base.setData(data);
            return new ResponseEntity<>(base, HttpStatus.CREATED);
        } catch (Exception ex) {
            base.setStatus(HttpStatus.BAD_REQUEST.value());
            base.setMessage(ex.getMessage());
            base.setTimestamp(new Date());
            return new ResponseEntity<>(base, HttpStatus.BAD_REQUEST);
        }
    }
}
