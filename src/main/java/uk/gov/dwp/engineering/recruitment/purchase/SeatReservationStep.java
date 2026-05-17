package uk.gov.dwp.engineering.recruitment.purchase;

import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.dwp.engineering.recruitment.exception.SeatReservationServiceException;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

/**
 * Reserves seats for the purchased tickets.
 */
@Slf4j
@RequiredArgsConstructor
public class SeatReservationStep implements TicketPurchaseStep {

    private final SeatReservationService seatReservationService;

    /**
     * Executes the seat reservation step by calling the seat reservation
     * service to reserve the required number of seats for the account. If the
     * reservation fails, a SeatReservationServiceException is thrown.
     *
     * @param ctx the ticket purchase context containing the account ID and
     * total seats reserved
     */
    @Override
    public void execute(TicketPurchaseContext ctx) {
        log.info("Reserving {} seats for account: {}", ctx.getTotalSeatsReserved(), ctx.getAccountId());
        ResponseEntity<String> response;
        try {
            response
                    = seatReservationService.reserveSeats(ctx.getAccountId(), ctx.getTotalSeatsReserved());
        } catch (Exception e) {
            throw new SeatReservationServiceException("Seat reservation failed", e);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new SeatReservationServiceException("Seat reservation failed");
        }
    }
}
