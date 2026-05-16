package uk.gov.dwp.engineering.recruitment.exception;

public class CinemaTicketServiceException extends RuntimeException {

    public CinemaTicketServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CinemaTicketServiceException(String message) {
        super(message);
    }
}
