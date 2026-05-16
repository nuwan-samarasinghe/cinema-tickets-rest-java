package uk.gov.dwp.engineering.recruitment;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import uk.gov.dwp.engineering.recruitment.dto.ErrorResponse;
import uk.gov.dwp.engineering.recruitment.exception.CinemaTicketServiceException;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.exception.PaymentServiceException;
import uk.gov.dwp.engineering.recruitment.exception.SeatReservationServiceException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBookingException(
            final InvalidBookingException exception
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .title(exception.getErrorCode().getTitle())
                .detail(exception.getErrorCode().getDetail())
                .type(exception.getErrorCode().name())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(PaymentServiceException.class)
    public ResponseEntity<ErrorResponse> handlePaymentServiceException(
            final PaymentServiceException exception
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .title("Payment Service Error")
                .detail("An error occurred while processing the payment. Please try again later.")
                .type("PAYMENT_SERVICE_ERROR")
                .build();

        log.error("Payment Service Error", exception);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    @ExceptionHandler(SeatReservationServiceException.class)
    public ResponseEntity<ErrorResponse> handleSeatReservationServiceException(
            final SeatReservationServiceException exception
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .title("Seat Reservation Service Error")
                .detail("An error occurred while processing the seat reservation. Please try again later.")
                .type("SEAT_RESERVATION_SERVICE_ERROR")
                .build();

        log.error("Seat Reservation Service Error", exception);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    @ExceptionHandler(CinemaTicketServiceException.class)
    public ResponseEntity<ErrorResponse> handleCinemaTicketServiceException(
            final CinemaTicketServiceException exception
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .title("Cinema Ticket Service Error")
                .detail("An error occurred while processing the cinema ticket request. Please try again later.")
                .type("CINEMA_TICKET_SERVICE_ERROR")
                .build();

        log.error("Cinema Ticket Service Error", exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            final HttpMessageNotReadableException exception
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .title("Bad Request")
                .detail("Invalid request body. Please provide a valid ticket request.")
                .type("INVALID_REQUEST_BODY")
                .build();

        log.error("Bad Request", exception);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            final Exception exception
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .title("Internal Server Error")
                .detail("An error occurred while processing the request. Please try again later.")
                .type("INTERNAL_SERVER_ERROR")
                .build();

        log.error("Internal Server Error", exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
