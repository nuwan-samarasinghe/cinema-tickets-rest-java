package uk.gov.dwp.engineering.recruitment.purchase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.dto.TicketBreakdownResponse;

/**
 * Shared state passed through each step of the ticket purchase process.
 * Populated progressively as each step executes.
 */
@Data
@Builder
public class TicketPurchaseContext {
    
    /** The account making the purchase. */
    private final Long accountId;

    /** The original ticket requests submitted by the account. */
    private final TicketRequest[] ticketRequests;

    /** Populated by TicketCostCalculationStep. */
    private Map<TicketType, Integer> ticketTypeCounts;

    /** Populated by TicketCostCalculationStep. */
    private List<TicketBreakdownResponse> ticketBreakdown;

    /** Populated by TicketCostCalculationStep. */
    private int totalTicketsPurchased;

    /** Populated by TicketCostCalculationStep. */
    private long totalSeatsReserved;

    /** Populated by TicketCostCalculationStep. */
    private BigDecimal totalAmountPaid;

}
