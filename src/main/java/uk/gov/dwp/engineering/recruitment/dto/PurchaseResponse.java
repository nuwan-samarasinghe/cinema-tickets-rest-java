package uk.gov.dwp.engineering.recruitment.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseResponse {

    private String status;
    private String message;
    private Long accountId;
    private Integer totalTicketsPurchased;
    private Long totalSeatsReserved;
    private BigDecimal totalAmountPaid;

    @Builder.Default
    private List<TicketBreakdownResponse> ticketBreakdown = new ArrayList<>();

}
