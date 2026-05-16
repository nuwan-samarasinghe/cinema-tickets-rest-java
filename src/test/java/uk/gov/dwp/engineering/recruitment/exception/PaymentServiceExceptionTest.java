package uk.gov.dwp.engineering.recruitment.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentServiceException")
class PaymentServiceExceptionTest {

    @Test
    @DisplayName("Given message, when exception is created, then message is set")
    void givenMessage_whenExceptionCreated_thenMessageIsSet() {
        PaymentServiceException exception =
                new PaymentServiceException("Something went wrong");

        assertEquals("Something went wrong", exception.getMessage());
    }

    @Test
    @DisplayName("Given message and cause, when exception is created, then message and cause are set")
    void givenMessageAndCause_whenExceptionCreated_thenMessageAndCauseAreSet() {
        Throwable cause = new RuntimeException("Root cause");

        PaymentServiceException exception =
                new PaymentServiceException("Something went wrong", cause);

        assertEquals("Something went wrong", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
