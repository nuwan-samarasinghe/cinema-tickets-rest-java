package uk.gov.dwp.engineering.recruitment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;

/**
 * Controller for handling cinema ticket purchases.
 */
@RestController
@RequestMapping("/cinema")
@RequiredArgsConstructor
public class CinemaTicketsController {

    /**
     * The Ticket Booking Service.
     */
    private final CinemaTicketsService cinemaTicketsService;

    /**
     * Purchase tickets for a given account and ticket request.
     *
     * @param accountId the account for which the tickets are being purchased
     * @param ticketRequests the ticket requests for which tickets are being
     * purchased
     */
    @PostMapping("/accounts/{accountId}/ticket-purchases")
    public ResponseEntity<String> purchaseTickets(@PathVariable("accountId") final Long accountId, @RequestBody final TicketRequest[] ticketRequests) {
        return ResponseEntity.ok(cinemaTicketsService.purchaseTickets(accountId, ticketRequests));
    }

}
