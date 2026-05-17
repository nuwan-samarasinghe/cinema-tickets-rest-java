package uk.gov.dwp.engineering.recruitment.validation;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.dwp.engineering.recruitment.core.TicketPurchaseProperties;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;

/**
 * TicketPurchaseValidator is responsible for validating the request.
 */
@Slf4j
@Component
public class TicketPurchaseValidator {

    private final TicketPurchaseProperties ticketPurchaseProperties;

    public TicketPurchaseValidator(
            TicketPurchaseProperties ticketPurchaseProperties
    ) {
        this.ticketPurchaseProperties = ticketPurchaseProperties;
    }

    /**
     * Validates the ticket purchase request before processing.
     *
     * @param accountId the account ID for which the tickets are being purchased
     * @param ticketRequests the ticket requests to validate
     * @throws InvalidBookingException if any of the validation rules are broken
     * throw this exception.
     */
    public void validate(Long accountId, TicketRequest[] ticketRequests) {
        validateForInvalidAccountId(accountId);
        validateForInvalidTicketRequest(ticketRequests);
        validateForTooManyTickets(ticketRequests);
        validateForInfantOrChildWithoutAdult(ticketRequests);
    }

    /**
     * Validates that the account ID is a positive number.
     *
     * @param accountId the account ID to validate
     * @throws InvalidBookingException if the account ID is null or not lesser
     * than zero
     */
    private void validateForInvalidAccountId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            log.error("Invalid account id: {}", accountId);
            throw new InvalidBookingException(BookingErrorCode.INVALID_ACCOUNT_ID);
        }
    }

    /**
     * Validates that the ticket requests are valid based on type is not there
     * or invalid amount and null request.
     *
     * @param ticketRequests the ticket requests to validate
     * @throws InvalidBookingException if any ticket request is invalid
     */
    private void validateForInvalidTicketRequest(TicketRequest[] ticketRequests) {
        if (ticketRequests == null || ticketRequests.length == 0) {
            log.warn("No tickets requested");
            throw new InvalidBookingException(BookingErrorCode.NO_TICKETS_REQUESTED);
        }

        for (TicketRequest ticketRequest : ticketRequests) {
            if (ticketRequest == null) {
                log.warn("Ticket request contains null item");
                throw new InvalidBookingException(BookingErrorCode.INVALID_TICKET_REQUEST);
            }

            if (ticketRequest.type() == null) {
                log.warn("Ticket request contains null ticket type");
                throw new InvalidBookingException(BookingErrorCode.INVALID_TICKET_TYPE);
            }

            if (ticketRequest.ticketCount() <= 0) {
                log.warn("Invalid ticket count: {}", ticketRequest.ticketCount());
                throw new InvalidBookingException(BookingErrorCode.INVALID_TICKET_COUNT);
            }
        }
    }

    /**
     * Validates that the total number of tickets requested does not exceed the
     * maximum limit.
     *
     * @param ticketRequests the ticket requests to validate
     * @throws InvalidBookingException if the total number of tickets requested
     * exceeds the maximum limit
     */
    private void validateForTooManyTickets(TicketRequest[] ticketRequests) {
        int totalTickets = Arrays.stream(ticketRequests)
                .mapToInt(TicketRequest::ticketCount)
                .sum();
        if (totalTickets > ticketPurchaseProperties.getMaxTicketsPerBooking()) {
            log.error("Too many tickets requested: {}", totalTickets);
            throw new InvalidBookingException(BookingErrorCode.MAX_TICKET_LIMIT_EXCEEDED);
        }
    }

    /**
     * Validates that infant or child tickets are not requested without an adult
     * ticket.
     *
     * @param ticketRequests the ticket requests to validate
     * @throws InvalidBookingException if infant or child tickets are requested
     * without an adult ticket
     */
    private void validateForInfantOrChildWithoutAdult(TicketRequest[] ticketRequests) {
        boolean hasAdultTicket = false;
        boolean hasChildOrInfantTicket = false;
        for (TicketRequest ticketRequest : ticketRequests) {
            if (ticketRequest.type() == TicketType.ADULT) {
                hasAdultTicket = true;
            }
            if (ticketRequest.type() == TicketType.CHILD
                    || ticketRequest.type() == TicketType.INFANT) {
                hasChildOrInfantTicket = true;
            }
            if (hasAdultTicket && hasChildOrInfantTicket) {
                return;
            }
        }
        if (hasChildOrInfantTicket && !hasAdultTicket) {
            log.error("Child or infant ticket requested without an adult ticket: {}", Arrays.toString(ticketRequests));
            throw new InvalidBookingException(BookingErrorCode.ADULT_REQUIRED);
        }
    }

}
