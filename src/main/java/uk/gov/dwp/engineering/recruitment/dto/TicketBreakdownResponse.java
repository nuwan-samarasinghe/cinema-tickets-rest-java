package uk.gov.dwp.engineering.recruitment.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketBreakdownResponse {

    private TicketType type;
    private Integer ticketCount;
    private BigDecimal totalCost;
    private Long seatsReserved;
}
