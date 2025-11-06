package apap.ti._5.accommodation_2306275651_be.restdto.response.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingChartResponseDTO {
    private String propertyID;
    private String propertyName;
    private Integer totalIncome;
    private Integer totalBookings;
    private Integer month;
    private Integer year;
}