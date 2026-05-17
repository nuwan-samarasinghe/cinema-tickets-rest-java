package uk.gov.dwp.engineering.recruitment.purchase;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.dto.TicketBreakdownResponse;

/**
 * Calculates ticket costs, seat counts, and totals from the raw ticket
 * requests. This is a pure in-memory calculation — no external services are
 * called,
 */
@Slf4j
@RequiredArgsConstructor
public class TicketCostCalculationStep implements TicketPurchaseStep {

    private final Map<TicketType, BigDecimal> ticketPrices;

    /**
     * Executes the cost calculation step by processing the ticket requests to
     * calculate the total number of tickets purchased, total seats reserved,
     * and total amount paid. The results are stored in the context for use by
     * subsequent steps. The breakdown of ticket types, counts, costs, and seats
     * reserved is also calculated and stored in the context.
     *
     * @param ctx the ticket purchase context containing the raw ticket requests
     * and where the calculated totals will be stored
     *
     */
    @Override
    public void execute(TicketPurchaseContext ctx) {
        Map<TicketType, Integer> counts = Arrays.stream(ctx.getTicketRequests())
                .collect(Collectors.groupingBy(
                        TicketRequest::type,
                        () -> new EnumMap<>(TicketType.class),
                        Collectors.summingInt(TicketRequest::ticketCount)
                ));
        ctx.setTicketTypeCounts(counts);

        List<TicketBreakdownResponse> breakdown = Arrays.stream(TicketType.values())
                .map(type -> buildBreakdown(type, counts))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        ctx.setTicketBreakdown(breakdown);

        ctx.setTotalTicketsPurchased(
                breakdown.stream().mapToInt(TicketBreakdownResponse::getTicketCount).sum()
        );
        ctx.setTotalSeatsReserved(
                breakdown.stream().mapToLong(TicketBreakdownResponse::getSeatsReserved).sum()
        );
        ctx.setTotalAmountPaid(
                breakdown.stream()
                        .map(TicketBreakdownResponse::getTotalCost)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(2)
        );

        log.info("Cost calculation complete — tickets: {}, seats: {}, amount: {}",
                ctx.getTotalTicketsPurchased(), ctx.getTotalSeatsReserved(), ctx.getTotalAmountPaid());
    }

    private Optional<TicketBreakdownResponse> buildBreakdown(
            TicketType ticketType, Map<TicketType, Integer> counts) {
        int count = counts.getOrDefault(ticketType, 0);
        if (count == 0) {
            return Optional.empty();
        }
        BigDecimal price = ticketPrices.get(ticketType);
        BigDecimal total = new BigDecimal(count).multiply(price).setScale(2);
        long seats = ticketType == TicketType.INFANT ? 0 : count;
        return Optional.of(TicketBreakdownResponse.builder()
                .type(ticketType)
                .ticketCount(count)
                .totalCost(total)
                .seatsReserved(seats)
                .build());
    }
}
