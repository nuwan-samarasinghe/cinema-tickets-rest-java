package uk.gov.dwp.engineering.recruitment.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import uk.gov.dwp.engineering.recruitment.TicketPurchaseProperties;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;

@DisplayName("TicketPurchaseValidatorTest")
class TicketPurchaseValidatorTest {

    private TicketPurchaseValidator validator;

    @BeforeEach
    void setUp() {
        TicketPurchaseProperties ticketPurchaseProperties = new TicketPurchaseProperties();
        ticketPurchaseProperties.setMaxTicketsPerBooking(25);
        validator = new TicketPurchaseValidator(ticketPurchaseProperties);
    }

    // ----------------- Valid cases -----------------

    @Test
    @DisplayName("Given valid adult ticket, when validate, then does not throw exception")
    void givenValidAdultTicket_whenValidate_thenDoesNotThrowException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 1)
        };
        assertDoesNotThrow(() -> validator.validate(accountId, ticketRequests));
    }

    @Test
    @DisplayName("Given valid adult and child tickets, when validate, then does not throw exception")
    void givenValidAdultAndChildTickets_whenValidate_thenDoesNotThrowException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 2),
            new TicketRequest(TicketType.CHILD, 3)
        };
        assertDoesNotThrow(() -> validator.validate(accountId, ticketRequests));
    }

    @Test
    @DisplayName("Given valid adult and infant tickets, when validate, then does not throw exception")
    void givenValidAdultAndInfantTickets_whenValidate_thenDoesNotThrowException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 1),
            new TicketRequest(TicketType.INFANT, 2)
        };
        assertDoesNotThrow(() -> validator.validate(accountId, ticketRequests));
    }

    @Test
    @DisplayName("Given exactly maximum tickets, when validate, then does not throw exception")
    void givenExactlyMaximumTickets_whenValidate_thenDoesNotThrowException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 10),
            new TicketRequest(TicketType.CHILD, 10),
            new TicketRequest(TicketType.INFANT, 5)
        };
        assertDoesNotThrow(() -> validator.validate(accountId, ticketRequests));
    }

    @Test
    @DisplayName("Given duplicate ticket types, when validate, then does not throw exception")
    void givenDuplicateTicketTypes_whenValidate_thenDoesNotThrowException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 1),
            new TicketRequest(TicketType.ADULT, 2),
            new TicketRequest(TicketType.CHILD, 3)
        };
        assertDoesNotThrow(() -> validator.validate(accountId, ticketRequests));
    }

    // ----------------- Invalid cases -----------------

    @Test
    @DisplayName("Given null account ID, when validate, then throws invalid account ID exception")
    void givenNullAccountId_whenValidate_thenThrowsInvalidAccountIdException() {
        Long accountId = null;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 1)
        };
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.INVALID_ACCOUNT_ID, exception.getErrorCode());
        assertEquals("Invalid account ID", exception.getErrorCode().getTitle());
        assertEquals("Account ID must be greater than zero", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given zero account ID, when validate, then throws invalid account ID exception")
    void givenZeroAccountId_whenValidate_thenThrowsInvalidAccountIdException() {
        Long accountId = 0L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 1)
        };
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.INVALID_ACCOUNT_ID, exception.getErrorCode());
        assertEquals("Invalid account ID", exception.getErrorCode().getTitle());
        assertEquals("Account ID must be greater than zero", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given negative account ID, when validate, then throws invalid account ID exception")
    void givenNegativeAccountId_whenValidate_thenThrowsInvalidAccountIdException() {
        Long accountId = -1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 1)
        };
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.INVALID_ACCOUNT_ID, exception.getErrorCode());
        assertEquals("Invalid account ID", exception.getErrorCode().getTitle());
        assertEquals("Account ID must be greater than zero", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given null ticket requests, when validate, then throws no tickets requested exception")
    void givenNullTicketRequests_whenValidate_thenThrowsNoTicketsRequestedException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = null;
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.NO_TICKETS_REQUESTED, exception.getErrorCode());
        assertEquals("No tickets requested", exception.getErrorCode().getTitle());
        assertEquals("At least one ticket must be requested", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given empty ticket requests, when validate, then throws no tickets requested exception")
    void givenEmptyTicketRequests_whenValidate_thenThrowsNoTicketsRequestedException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {};
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.NO_TICKETS_REQUESTED, exception.getErrorCode());
        assertEquals("No tickets requested", exception.getErrorCode().getTitle());
        assertEquals("At least one ticket must be requested", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given null ticket request in array, when validate, then throws invalid ticket request exception")
    void givenNullTicketRequestInArray_whenValidate_thenThrowsInvalidTicketRequestException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 1),
            null
        };
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.INVALID_TICKET_REQUEST, exception.getErrorCode());
        assertEquals("Invalid ticket request", exception.getErrorCode().getTitle());
        assertEquals("Ticket request must not be null", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given null ticket type, when validate, then throws invalid ticket type exception")
    void givenNullTicketType_whenValidate_thenThrowsInvalidTicketTypeException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(null, 1)
        };
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.INVALID_TICKET_TYPE, exception.getErrorCode());
        assertEquals("Invalid ticket type", exception.getErrorCode().getTitle());
        assertEquals("Ticket type must be provided", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given zero ticket count, when validate, then throws invalid ticket count exception")
    void givenZeroTicketCount_whenValidate_thenThrowsInvalidTicketCountException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 0)
        };
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.INVALID_TICKET_COUNT, exception.getErrorCode());
        assertEquals("Invalid ticket count", exception.getErrorCode().getTitle());
        assertEquals("Ticket quantity must be greater than zero", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given negative ticket count, when validate, then throws invalid ticket count exception")
    void givenNegativeTicketCount_whenValidate_thenThrowsInvalidTicketCountException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, -1)
        };
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.INVALID_TICKET_COUNT, exception.getErrorCode());
        assertEquals("Invalid ticket count", exception.getErrorCode().getTitle());
        assertEquals("Ticket quantity must be greater than zero", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given one invalid ticket count in multiple requests, when validate, then throws invalid ticket count exception")
    void givenOneInvalidTicketCountInMultipleRequests_whenValidate_thenThrowsInvalidTicketCountException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 2),
            new TicketRequest(TicketType.CHILD, 0),
            new TicketRequest(TicketType.INFANT, 1)
        };
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.INVALID_TICKET_COUNT, exception.getErrorCode());
        assertEquals("Invalid ticket count", exception.getErrorCode().getTitle());
        assertEquals("Ticket quantity must be greater than zero", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given more than maximum tickets in single request, when validate, then throws max ticket limit exceeded exception")
    void givenMoreThanMaximumTicketsInSingleRequest_whenValidate_thenThrowsMaxTicketLimitExceededException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 26)
        };
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.MAX_TICKET_LIMIT_EXCEEDED, exception.getErrorCode());
        assertEquals("Maximum ticket limit exceeded", exception.getErrorCode().getTitle());
        assertEquals("A maximum of %d tickets can be purchased at one time", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given more than maximum tickets across multiple requests, when validate, then throws max ticket limit exceeded exception")
    void givenMoreThanMaximumTicketsAcrossMultipleRequests_whenValidate_thenThrowsMaxTicketLimitExceededException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 10),
            new TicketRequest(TicketType.CHILD, 10),
            new TicketRequest(TicketType.INFANT, 6)
        };
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.MAX_TICKET_LIMIT_EXCEEDED, exception.getErrorCode());
        assertEquals("Maximum ticket limit exceeded", exception.getErrorCode().getTitle());
        assertEquals("A maximum of %d tickets can be purchased at one time", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given child ticket without adult, when validate, then throws adult required exception")
    void givenChildTicketWithoutAdult_whenValidate_thenThrowsAdultRequiredException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.CHILD, 1)
        };
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.ADULT_REQUIRED, exception.getErrorCode());
        assertEquals("Adult required", exception.getErrorCode().getTitle());
        assertEquals("CHILD and INFANT tickets cannot be purchased without an ADULT ticket", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given infant ticket without adult, when validate, then throws adult required exception")
    void givenInfantTicketWithoutAdult_whenValidate_thenThrowsAdultRequiredException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.INFANT, 1)
        };
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.ADULT_REQUIRED, exception.getErrorCode());
        assertEquals("Adult required", exception.getErrorCode().getTitle());
        assertEquals("CHILD and INFANT tickets cannot be purchased without an ADULT ticket", exception.getErrorCode().getDetail());
    }

    @Test
    @DisplayName("Given child and infant tickets without adult, when validate, then throws adult required exception")
    void givenChildAndInfantTicketsWithoutAdult_whenValidate_thenThrowsAdultRequiredException() {
        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.CHILD, 1),
            new TicketRequest(TicketType.INFANT, 1)
        };
        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> validator.validate(accountId, ticketRequests)
        );
        assertEquals(BookingErrorCode.ADULT_REQUIRED, exception.getErrorCode());
        assertEquals("Adult required", exception.getErrorCode().getTitle());
        assertEquals("CHILD and INFANT tickets cannot be purchased without an ADULT ticket", exception.getErrorCode().getDetail());
    }
}
