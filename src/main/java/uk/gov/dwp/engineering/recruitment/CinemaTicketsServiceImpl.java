package uk.gov.dwp.engineering.recruitment;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.dto.PurchaseResponse;
import uk.gov.dwp.engineering.recruitment.exception.CinemaTicketServiceException;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.purchase.PaymentDebitStep;
import uk.gov.dwp.engineering.recruitment.purchase.RequestValidateStep;
import uk.gov.dwp.engineering.recruitment.purchase.SeatReservationStep;
import uk.gov.dwp.engineering.recruitment.purchase.TicketCostCalculationStep;
import uk.gov.dwp.engineering.recruitment.purchase.TicketPurchaseContext;
import uk.gov.dwp.engineering.recruitment.purchase.TicketPurchaseOrchestrator;
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
        TicketPurchaseContext ctx = TicketPurchaseContext.builder()
                .accountId(accountId)
                .ticketRequests(ticketRequests)
                .build();

        /**
         * *
         * Why Saga-pattern used over here ?
         *
         *
         * The Saga pattern is used in this context to manage the complex
         * workflow of purchasing cinema tickets, which involves multiple steps
         * such as
         *
         * 01. validating the request 02. calculating the total cost and no of
         * seats to reserve, 03. processing the payment 04. reserving seats.
         *
         * Each of these steps can potentially fail, and the Saga pattern allows
         * for a structured way to handle these. in here we are not looking at
         * compansation or other steps like sending eamails but now we can add
         * those steps easily into the system with minimal efffort.
         */
        new TicketPurchaseOrchestrator()
                .addStep(new RequestValidateStep(validator)) // validation step
                .addStep(new TicketCostCalculationStep(ticketPrices)) // cost calculation step
                .addStep(new PaymentDebitStep(paymentService)) // payment processing step
                .addStep(new SeatReservationStep(seatReservationService)) // seat reservation step
                .execute(ctx);

        return PurchaseResponse.builder()
                .status("SUCCESS")
                .message("Tickets purchased successfully")
                .accountId(ctx.getAccountId())
                .totalTicketsPurchased(ctx.getTotalTicketsPurchased())
                .totalSeatsReserved(ctx.getTotalSeatsReserved())
                .totalAmountPaid(ctx.getTotalAmountPaid())
                .ticketBreakdown(ctx.getTicketBreakdown())
                .build();
    }
}
