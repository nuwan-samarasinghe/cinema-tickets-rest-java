package uk.gov.dwp.engineering.recruitment.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the various error codes for booking validation failures.
 */
@RequiredArgsConstructor
public enum BookingErrorCode {

    INVALID_ACCOUNT_ID(
            "Invalid account ID",
            "Account ID must be greater than zero"
    ),
    NO_TICKETS_REQUESTED(
            "No tickets requested",
            "At least one ticket must be requested"
    ),
    INVALID_TICKET_REQUEST(
            "Invalid ticket request",
            "Ticket request must not be null"
    ),
    INVALID_TICKET_TYPE(
            "Invalid ticket type",
            "Ticket type must be provided"
    ),
    INVALID_TICKET_COUNT(
            "Invalid ticket count",
            "Ticket quantity must be greater than zero"
    ),
    MAX_TICKET_LIMIT_EXCEEDED(
            "Maximum ticket limit exceeded",
            "A maximum of %d tickets can be purchased at one time"
    ),
    ADULT_REQUIRED(
            "Adult required",
            "CHILD and INFANT tickets cannot be purchased without an ADULT ticket"
    );

    @Getter
    private final String title;
    @Getter
    private final String detail;
}
