package uk.gov.dwp.engineering.recruitment;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.exception.PaymentServiceException;
import uk.gov.dwp.engineering.recruitment.exception.SeatReservationServiceException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;
import uk.gov.dwp.engineering.recruitment.validation.BookingErrorCode;
import uk.gov.dwp.engineering.recruitment.validation.TicketPurchaseValidator;

@DisplayName("CinemaTicketsServiceImplTest")
@ExtendWith(MockitoExtension.class)
class CinemaTicketsServiceImplTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private SeatReservationService seatReservationService;

    @Mock
    private TicketPurchaseValidator validator;

    private CinemaTicketsService ticketService;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ticketService = new CinemaTicketsServiceImpl(
                paymentService,
                seatReservationService,
                validator,
                objectMapper
        );
    }

    @Test
    @DisplayName("Given valid adult ticket, when purchase tickets, then calculates total price and reserves seats")
    void givenValidAdultTicket_whenPurchaseTickets_thenCalculatesTotalPriceAndReservesSeats() {

        when(paymentService.debitAccount(anyLong(), any())).thenReturn(ResponseEntity.ok("Payment successful"));
        when(seatReservationService.reserveSeats(anyLong(), anyLong())).thenReturn(ResponseEntity.ok("Seats reserved"));

        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 1)
        };
        String output = ticketService.purchaseTickets(accountId, ticketRequests);
        JsonNode jsonOutput = objectMapper.readTree(output);
        assertEquals("SUCCESS", jsonOutput.get("status").asString());
        assertEquals("Tickets purchased successfully", jsonOutput.get("message").asString());
        assertEquals(1L, jsonOutput.get("accountId").asLong());
        assertEquals(1, jsonOutput.get("totalTicketsPurchased").asInt());
        assertEquals(1, jsonOutput.get("totalSeatsReserved").asInt());
        assertMoneyEquals("25.00", jsonOutput.get("totalAmountPaid"));
        assertEquals("ADULT", jsonOutput.get("ticketBreakdown").get(0).get("type").asString());
        assertEquals(1, jsonOutput.get("ticketBreakdown").get(0).get("ticketCount").asInt());
        assertMoneyEquals("25.00", jsonOutput.get("ticketBreakdown").get(0).get("totalCost"));
        assertEquals(1, jsonOutput.get("ticketBreakdown").get(0).get("seatsReserved").asInt());

        verify(paymentService).debitAccount(eq(1L), eq(new BigDecimal("25.00")));
        verify(seatReservationService).reserveSeats(eq(1L), eq(1L));
        verifyNoMoreInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Given valid adult and child tickets, when purchase tickets, then calculates total price and reserves seats")
    void givenValidAdultAndChildTickets_whenPurchaseTickets_thenCalculatesTotalPriceAndReservesSeats() {

        when(paymentService.debitAccount(anyLong(), any())).thenReturn(ResponseEntity.ok("Payment successful"));
        when(seatReservationService.reserveSeats(anyLong(), anyLong())).thenReturn(ResponseEntity.ok("Seats reserved"));

        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 2),
            new TicketRequest(TicketType.CHILD, 3)
        };
        String output = ticketService.purchaseTickets(accountId, ticketRequests);
        JsonNode jsonOutput = objectMapper.readTree(output);
        assertEquals("SUCCESS", jsonOutput.get("status").asString());
        assertEquals("Tickets purchased successfully", jsonOutput.get("message").asString());
        assertEquals(1L, jsonOutput.get("accountId").asLong());
        assertEquals(5, jsonOutput.get("totalTicketsPurchased").asInt());
        assertEquals(5, jsonOutput.get("totalSeatsReserved").asInt());
        assertMoneyEquals("95.00", jsonOutput.get("totalAmountPaid"));
        assertEquals("ADULT", jsonOutput.get("ticketBreakdown").get(0).get("type").asString());
        assertEquals(2, jsonOutput.get("ticketBreakdown").get(0).get("ticketCount").asInt());
        assertMoneyEquals("50.00", jsonOutput.get("ticketBreakdown").get(0).get("totalCost"));
        assertEquals(2, jsonOutput.get("ticketBreakdown").get(0).get("seatsReserved").asInt());
        assertEquals("CHILD", jsonOutput.get("ticketBreakdown").get(1).get("type").asString());
        assertEquals(3, jsonOutput.get("ticketBreakdown").get(1).get("ticketCount").asInt());
        assertMoneyEquals("45.00", jsonOutput.get("ticketBreakdown").get(1).get("totalCost"));
        assertEquals(3, jsonOutput.get("ticketBreakdown").get(1).get("seatsReserved").asInt());

        verify(paymentService).debitAccount(eq(1L), eq(new BigDecimal("95.00")));
        verify(seatReservationService).reserveSeats(eq(1L), eq(5L));
        verifyNoMoreInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Given valid adult and infant tickets, when purchase tickets, then calculates total price and reserves seats")
    void givenValidAdultAndInfantTickets_whenPurchaseTickets_thenCalculatesTotalPriceAndReservesSeats() {

        when(paymentService.debitAccount(anyLong(), any())).thenReturn(ResponseEntity.ok("Payment successful"));
        when(seatReservationService.reserveSeats(anyLong(), anyLong())).thenReturn(ResponseEntity.ok("Seats reserved"));

        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 1),
            new TicketRequest(TicketType.INFANT, 2)
        };
        String output = ticketService.purchaseTickets(accountId, ticketRequests);
        JsonNode jsonOutput = objectMapper.readTree(output);
        assertEquals("SUCCESS", jsonOutput.get("status").asString());
        assertEquals("Tickets purchased successfully", jsonOutput.get("message").asString());
        assertEquals(1L, jsonOutput.get("accountId").asLong());
        assertEquals(3, jsonOutput.get("totalTicketsPurchased").asInt());
        assertEquals(1, jsonOutput.get("totalSeatsReserved").asInt());
        assertMoneyEquals("25.00", jsonOutput.get("totalAmountPaid"));
        assertEquals("ADULT", jsonOutput.get("ticketBreakdown").get(0).get("type").asString());
        assertEquals(1, jsonOutput.get("ticketBreakdown").get(0).get("ticketCount").asInt());
        assertMoneyEquals("25.00", jsonOutput.get("ticketBreakdown").get(0).get("totalCost"));
        assertEquals(1, jsonOutput.get("ticketBreakdown").get(0).get("seatsReserved").asInt());
        assertEquals("INFANT", jsonOutput.get("ticketBreakdown").get(1).get("type").asString());
        assertEquals(2, jsonOutput.get("ticketBreakdown").get(1).get("ticketCount").asInt());
        assertMoneyEquals("0.00", jsonOutput.get("ticketBreakdown").get(1).get("totalCost"));
        assertEquals(0, jsonOutput.get("ticketBreakdown").get(1).get("seatsReserved").asInt());

        verify(paymentService).debitAccount(eq(1L), eq(new BigDecimal("25.00")));
        verify(seatReservationService).reserveSeats(eq(1L), eq(1L));
        verifyNoMoreInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Given exactly maximum tickets, when purchase tickets, then calculates total price and reserves seats")
    void givenExactlyMaximumTickets_whenPurchaseTickets_thenCalculatesTotalPriceAndReservesSeats() {

        when(paymentService.debitAccount(anyLong(), any())).thenReturn(ResponseEntity.ok("Payment successful"));
        when(seatReservationService.reserveSeats(anyLong(), anyLong())).thenReturn(ResponseEntity.ok("Seats reserved"));

        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 10),
            new TicketRequest(TicketType.CHILD, 10),
            new TicketRequest(TicketType.INFANT, 5)
        };
        String output = ticketService.purchaseTickets(accountId, ticketRequests);
        JsonNode jsonOutput = objectMapper.readTree(output);
        assertEquals("SUCCESS", jsonOutput.get("status").asString());
        assertEquals("Tickets purchased successfully", jsonOutput.get("message").asString());
        assertEquals(1L, jsonOutput.get("accountId").asLong());
        assertEquals(25, jsonOutput.get("totalTicketsPurchased").asInt());
        assertEquals(20, jsonOutput.get("totalSeatsReserved").asInt());
        assertMoneyEquals("400.00", jsonOutput.get("totalAmountPaid"));
        assertEquals("ADULT", jsonOutput.get("ticketBreakdown").get(0).get("type").asString());
        assertEquals(10, jsonOutput.get("ticketBreakdown").get(0).get("ticketCount").asInt());
        assertMoneyEquals("250.00", jsonOutput.get("ticketBreakdown").get(0).get("totalCost"));
        assertEquals(10, jsonOutput.get("ticketBreakdown").get(0).get("seatsReserved").asInt());
        assertEquals("CHILD", jsonOutput.get("ticketBreakdown").get(1).get("type").asString());
        assertEquals(10, jsonOutput.get("ticketBreakdown").get(1).get("ticketCount").asInt());
        assertMoneyEquals("150.00", jsonOutput.get("ticketBreakdown").get(1).get("totalCost"));
        assertEquals(10, jsonOutput.get("ticketBreakdown").get(1).get("seatsReserved").asInt());
        assertEquals("INFANT", jsonOutput.get("ticketBreakdown").get(2).get("type").asString());
        assertEquals(5, jsonOutput.get("ticketBreakdown").get(2).get("ticketCount").asInt());
        assertMoneyEquals("0.00", jsonOutput.get("ticketBreakdown").get(2).get("totalCost"));
        assertEquals(0, jsonOutput.get("ticketBreakdown").get(2).get("seatsReserved").asInt());

        verify(paymentService).debitAccount(eq(1L), eq(new BigDecimal("400.00")));
        verify(seatReservationService).reserveSeats(eq(1L), eq(20L));
        verifyNoMoreInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Given duplicate ticket types, when purchase tickets, then calculates total price and reserves seats")
    void givenDuplicateTicketTypes_whenPurchaseTickets_thenCalculatesTotalPriceAndReservesSeats() {

        when(paymentService.debitAccount(anyLong(), any())).thenReturn(ResponseEntity.ok("Payment successful"));
        when(seatReservationService.reserveSeats(anyLong(), anyLong())).thenReturn(ResponseEntity.ok("Seats reserved"));

        Long accountId = 1L;
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 1),
            new TicketRequest(TicketType.ADULT, 2),
            new TicketRequest(TicketType.CHILD, 3)
        };
        String output = ticketService.purchaseTickets(accountId, ticketRequests);
        JsonNode jsonOutput = objectMapper.readTree(output);
        assertEquals("SUCCESS", jsonOutput.get("status").asString());
        assertEquals("Tickets purchased successfully", jsonOutput.get("message").asString());
        assertEquals(1L, jsonOutput.get("accountId").asLong());
        assertEquals(6, jsonOutput.get("totalTicketsPurchased").asInt());
        assertEquals(6, jsonOutput.get("totalSeatsReserved").asInt());
        assertMoneyEquals("120.00", jsonOutput.get("totalAmountPaid"));
        assertEquals("ADULT", jsonOutput.get("ticketBreakdown").get(0).get("type").asString());
        assertEquals(3, jsonOutput.get("ticketBreakdown").get(0).get("ticketCount").asInt());
        assertMoneyEquals("75.00", jsonOutput.get("ticketBreakdown").get(0).get("totalCost"));
        assertEquals(3, jsonOutput.get("ticketBreakdown").get(0).get("seatsReserved").asInt());
        assertEquals("CHILD", jsonOutput.get("ticketBreakdown").get(1).get("type").asString());
        assertEquals(3, jsonOutput.get("ticketBreakdown").get(1).get("ticketCount").asInt());
        assertMoneyEquals("45.00", jsonOutput.get("ticketBreakdown").get(1).get("totalCost"));
        assertEquals(3, jsonOutput.get("ticketBreakdown").get(1).get("seatsReserved").asInt());

        verify(paymentService).debitAccount(eq(1L), eq(new BigDecimal("120.00")));
        verify(seatReservationService).reserveSeats(eq(1L), eq(6L));
        verifyNoMoreInteractions(paymentService, seatReservationService);
    }

    // Additional test cases when payment service fails and when seat reservation service fails can be added here
    @Test
    @DisplayName("Given payment service failure, when purchase tickets, then throws PaymentServiceException")
    void givenPaymentServiceFailure_whenPurchaseTickets_thenThrowsPaymentServiceException() {
        when(paymentService.debitAccount(anyLong(), any())).thenReturn(ResponseEntity.status(500).body("Payment failed"));
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 1)
        };
        PaymentServiceException exception = assertThrows(
                PaymentServiceException.class,
                () -> ticketService.purchaseTickets(1L, ticketRequests)
        );

        assertEquals("Payment failed", exception.getMessage());

        verify(paymentService).debitAccount(eq(1L), eq(new BigDecimal("25.00")));
        verify(seatReservationService, never()).reserveSeats(anyLong(), anyLong());
        verifyNoMoreInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Given seat reservation service failure, when purchase tickets, then throws SeatReservationServiceException")
    void givenSeatReservationServiceFailure_whenPurchaseTickets_thenThrowsSeatReservationServiceException() {
        when(paymentService.debitAccount(anyLong(), any())).thenReturn(ResponseEntity.ok("Payment successful"));
        when(seatReservationService.reserveSeats(anyLong(), anyLong())).thenReturn(ResponseEntity.status(500).body("Seat reservation failed"));
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.ADULT, 1)
        };
        SeatReservationServiceException exception = assertThrows(
                SeatReservationServiceException.class,
                () -> ticketService.purchaseTickets(1L, ticketRequests)
        );

        assertEquals("Seat reservation failed", exception.getMessage());

        verify(paymentService).debitAccount(eq(1L), eq(new BigDecimal("25.00")));
        verify(seatReservationService).reserveSeats(eq(1L), eq(1L));
        verifyNoMoreInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Given validator rejects booking, when purchase tickets, then no payment or seat reservation occurs")
    void givenValidatorRejectsBooking_whenPurchaseTickets_thenNoPaymentOrSeatReservationOccurs() {
        TicketRequest[] ticketRequests = {
            new TicketRequest(TicketType.CHILD, 1)
        };

        doThrow(new InvalidBookingException(BookingErrorCode.ADULT_REQUIRED))
                .when(validator)
                .validate(anyLong(), any());

        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> ticketService.purchaseTickets(1L, ticketRequests)
        );

        assertEquals(BookingErrorCode.ADULT_REQUIRED, exception.getErrorCode());

        verify(validator).validate(eq(1L), eq(ticketRequests));
        verifyNoInteractions(paymentService, seatReservationService);
    }

    private void assertMoneyEquals(String expected, JsonNode actualNode) {
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(new BigDecimal(actualNode.asString()))
        );
    }

}
