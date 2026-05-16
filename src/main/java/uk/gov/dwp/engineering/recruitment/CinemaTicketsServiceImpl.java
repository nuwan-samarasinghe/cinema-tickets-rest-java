package uk.gov.dwp.engineering.recruitment;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.dto.PurchaseResponse;
import uk.gov.dwp.engineering.recruitment.dto.TicketBreakdownResponse;
import uk.gov.dwp.engineering.recruitment.exception.CinemaTicketServiceException;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.exception.PaymentServiceException;
import uk.gov.dwp.engineering.recruitment.exception.SeatReservationServiceException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;
import uk.gov.dwp.engineering.recruitment.validation.TicketPurchaseValidator;

/**
 * Implementation of the {@link CinemaTicketsService} interface.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CinemaTicketsServiceImpl implements CinemaTicketsService {

    /**
     * The payment service used to process payments for ticket purchases.
     */
    private final PaymentService paymentService;

    /**
     * The seat reservation service used to reserve seats for ticket purchases.
     */
    private final SeatReservationService seatReservationService;

    /**
     * The validator used to validate ticket purchase requests.
     */
    private final TicketPurchaseValidator validator;

    /**
     * The object mapper used to convert objects to JSON strings.
     */
    private final ObjectMapper objectMapper;

    /**
     * A map of ticket types to their corresponding prices.
     */
    private final Map<TicketType, BigDecimal> ticketPrices = Map.of(
            TicketType.ADULT, new BigDecimal("25.00"),
            TicketType.CHILD, new BigDecimal("15.00"),
            TicketType.INFANT, new BigDecimal("0.00")
    );

    /**
     * Purchases tickets for a given account ID and an array of ticket requests.
     * Validates the request before processing the payment and reserving seats.
     * Returns a confirmation message upon successful purchase.
     */
    @Override
    public String purchaseTickets(final Long accountId, final TicketRequest... ticketRequests)
            throws InvalidBookingException {
        validator.validate(accountId, ticketRequests);
        return jsonToString(processTickets(accountId, ticketRequests));
    }

    /**
     * Converts a message to a JSON string.
     *
     * @param message the message to convert
     * @return the JSON string representation of the message
     */
    private String jsonToString(PurchaseResponse purchaseResponse) {
        try {
            return objectMapper.writeValueAsString(purchaseResponse);
        } catch (JacksonException e) {
            log.error("Error converting to JSON", e);
            throw new CinemaTicketServiceException("Error converting to JSON", e);
        }
    }

    /**
     * Processes the ticket purchase by calculating the total cost, reserving
     * seats, and returning a detailed breakdown of the purchase.
     *
     * @param accountId the account ID for which the tickets are being purchased
     * @param ticketRequests the array of ticket requests
     * @return a PurchaseResponse containing details of the purchase
     */
    private PurchaseResponse processTickets(final Long accountId, final TicketRequest... ticketRequests) {
        log.info("Processing ticket purchase for account: {}", accountId);
        Map<TicketType, Integer> ticketTypeCounts = Arrays.stream(ticketRequests)
                .collect(Collectors.groupingBy(
                        TicketRequest::type,
                        () -> new EnumMap<>(TicketType.class),
                        Collectors.summingInt(TicketRequest::ticketCount)
                ));

        List<TicketBreakdownResponse> ticketBreakdown = Arrays.stream(TicketType.values())
                .map(ticketType -> buildTicketBreakdown(ticketType, ticketTypeCounts))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        int totalTicketsPurchased = ticketBreakdown.stream()
                .mapToInt(TicketBreakdownResponse::getTicketCount)
                .sum();

        long totalSeatsReserved = ticketBreakdown.stream()
                .mapToLong(TicketBreakdownResponse::getSeatsReserved)
                .sum();

        BigDecimal totalAmountPaid = ticketBreakdown.stream()
                .map(TicketBreakdownResponse::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);

        log.info("Total tickets purchased: {}, Total seats reserved: {}, Total amount paid: {}",
                totalTicketsPurchased, totalSeatsReserved, totalAmountPaid);

        // Process payment and reserve seats
        ResponseEntity<String> paymentResponse = paymentService.debitAccount(accountId, totalAmountPaid);
        if (!paymentResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Payment failed for account: {}, amount: {}", accountId, totalAmountPaid);
            throw new PaymentServiceException("Payment failed");
        }
        // Only reserve seats if payment is successful
        ResponseEntity<String> seatResponse = seatReservationService.reserveSeats(accountId, totalSeatsReserved);
        if (!seatResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Seat reservation failed for account: {}, seats: {}", accountId, totalSeatsReserved);
            throw new SeatReservationServiceException("Seat reservation failed");
        }

        return PurchaseResponse.builder()
                .status("SUCCESS")
                .message("Tickets purchased successfully")
                .accountId(accountId)
                .totalTicketsPurchased(totalTicketsPurchased)
                .totalSeatsReserved(totalSeatsReserved)
                .totalAmountPaid(totalAmountPaid)
                .ticketBreakdown(ticketBreakdown)
                .build();
    }

    /**
     * Builds a breakdown of the ticket purchase for a specific ticket type.
     *
     * @param ticketType ticket type.
     * @param ticketTypeCounts for a give ticket type no of tickets.
     * @return an Optional containing the TicketBreakdownResponse if the ticket
     * type was purchased.
     */
    private Optional<TicketBreakdownResponse> buildTicketBreakdown(
            TicketType ticketType,
            Map<TicketType, Integer> ticketTypeCounts
    ) {
        int ticketCount = ticketTypeCounts.getOrDefault(ticketType, 0);
        if (ticketCount == 0) {
            return Optional.empty();
        }
        BigDecimal pricePerTicket = ticketPrices.get(ticketType);
        BigDecimal totalCost = new BigDecimal(ticketCount).multiply(pricePerTicket).setScale(2);
        long seatsReserved = calculateSeatsReserved(ticketType, ticketCount);
        log.info("Ticket breakdown for {}: {} tickets, total cost: {}", ticketType, ticketCount, totalCost);
        return Optional.of(
                TicketBreakdownResponse.builder()
                        .type(ticketType)
                        .ticketCount(ticketCount)
                        .totalCost(totalCost)
                        .seatsReserved(seatsReserved)
                        .build()
        );
    }

    /**
     * Calculates the number of seats to reserve based on the ticket type and
     * count. Infants do not require a seat, while adults and children do.
     *
     * @param ticketType the type of ticket
     * @param ticketCount the number of tickets of that type
     * @return the number of seats to reserve
     */
    private int calculateSeatsReserved(TicketType ticketType, int ticketCount) {
        return ticketType == TicketType.INFANT ? 0 : ticketCount;
    }
}
