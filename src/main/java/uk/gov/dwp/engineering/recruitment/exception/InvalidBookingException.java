package uk.gov.dwp.engineering.recruitment.exception;

import uk.gov.dwp.engineering.recruitment.validation.BookingErrorCode;

public class InvalidBookingException extends RuntimeException {

  private final BookingErrorCode errorCode;

  public InvalidBookingException(BookingErrorCode errorCode) {
    super(errorCode.getDetail());
    this.errorCode = errorCode;
  }

  public BookingErrorCode getErrorCode() {
    return errorCode;
  }
}
