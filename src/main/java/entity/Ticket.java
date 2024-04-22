package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

    private Long id;
    private String passengerNo;
    private String passengerName;
    private Long flightId;
    private String seatNo;
    private BigDecimal cost;

}
