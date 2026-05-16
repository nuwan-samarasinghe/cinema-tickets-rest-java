package uk.gov.dwp.engineering.recruitment.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import uk.gov.dwp.engineering.recruitment.validation.BookingErrorCode;

@DisplayName("InvalidBookingExceptionTest")
class InvalidBookingExceptionTest {

    @Test
    @DisplayName("Given a BookingErrorCode, when InvalidBookingException is created, then it should store the error code")
    void givenBookingErrorCode_whenExceptionIsCreated_thenStoresErrorCode() {
        BookingErrorCode errorCode = BookingErrorCode.INVALID_ACCOUNT_ID;
        InvalidBookingException exception = new InvalidBookingException(errorCode);
        assertEquals(exception.getErrorCode(), errorCode);
    }

}
