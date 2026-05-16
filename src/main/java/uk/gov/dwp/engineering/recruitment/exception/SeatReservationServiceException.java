package uk.gov.dwp.engineering.recruitment.exception;

public class SeatReservationServiceException extends RuntimeException {

    public SeatReservationServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeatReservationServiceException(String message) {
        super(message);
    }
}
