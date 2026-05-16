package uk.gov.dwp.engineering.recruitment;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CinemaTicketsControllerIntegrationTest")
class CinemaTicketsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private SeatReservationService seatReservationService;

    // success scenario
    @Test
    @DisplayName("Given valid ticket request, when endpoint is called, then purchases tickets successfully")
    void givenValidTicketRequest_whenEndpointCalled_thenPurchasesTicketsSuccessfully() throws Exception {
        when(paymentService.debitAccount(eq(1L), eq(new BigDecimal("95.00"))))
                .thenReturn(ResponseEntity.ok("Payment successful"));

        when(seatReservationService.reserveSeats(eq(1L), eq(5L)))
                .thenReturn(ResponseEntity.ok("Seats reserved"));

        String requestBody = """
                [
                    {
                        "type": "ADULT",
                        "ticketCount": 2
                    },
                    {
                        "type": "CHILD",
                        "ticketCount": 3
                    }
                ]
                """;

        mockMvc.perform(post("/cinema/accounts/{accountId}/ticket-purchases", 1L)
                .contentType("application/json")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Tickets purchased successfully"))
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.totalTicketsPurchased").value(5))
                .andExpect(jsonPath("$.totalSeatsReserved").value(5))
                .andExpect(jsonPath("$.totalAmountPaid").value(95.00))
                .andExpect(jsonPath("$.ticketBreakdown[0].type").value("ADULT"))
                .andExpect(jsonPath("$.ticketBreakdown[0].ticketCount").value(2))
                .andExpect(jsonPath("$.ticketBreakdown[0].totalCost").value(50.00))
                .andExpect(jsonPath("$.ticketBreakdown[0].seatsReserved").value(2))
                .andExpect(jsonPath("$.ticketBreakdown[1].type").value("CHILD"))
                .andExpect(jsonPath("$.ticketBreakdown[1].ticketCount").value(3))
                .andExpect(jsonPath("$.ticketBreakdown[1].totalCost").value(45.00))
                .andExpect(jsonPath("$.ticketBreakdown[1].seatsReserved").value(3));

        verify(paymentService).debitAccount(eq(1L), eq(new BigDecimal("95.00")));
        verify(seatReservationService).reserveSeats(eq(1L), eq(5L));
    }

    // error scenarios
    @Test
    @DisplayName("Given invalid booking without adult, when endpoint is called, then returns bad request")
    void givenInvalidBookingWithoutAdult_whenEndpointCalled_thenReturnsBadRequest() throws Exception {
        String requestBody = """
            [
                {
                    "type": "CHILD",
                    "ticketCount": 1
                }
            ]
            """;

        mockMvc.perform(post("/cinema/accounts/{accountId}/ticket-purchases", 1L)
                .contentType("application/json")
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.type").value("ADULT_REQUIRED"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Given malformed JSON, when endpoint is called, then returns invalid request body error")
    void givenMalformedJson_whenEndpointCalled_thenReturnsInvalidRequestBodyError() throws Exception {
        String requestBody = """
            [
                {
                    "type": "ADULT",
                    "ticketCount": 1
                }
            """;

        mockMvc.perform(post("/cinema/accounts/{accountId}/ticket-purchases", 1L)
                .contentType("application/json")
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid request body. Please provide a valid ticket request."))
                .andExpect(jsonPath("$.type").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Given empty request body, when endpoint is called, then returns invalid request body error")
    void givenEmptyRequestBody_whenEndpointCalled_thenReturnsInvalidRequestBodyError() throws Exception {
        mockMvc.perform(post("/cinema/accounts/{accountId}/ticket-purchases", 1L)
                .contentType("application/json")
                .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid request body. Please provide a valid ticket request."))
                .andExpect(jsonPath("$.type").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Given invalid ticket type, when endpoint is called, then returns invalid request body error")
    void givenInvalidTicketType_whenEndpointCalled_thenReturnsInvalidRequestBodyError() throws Exception {
        String requestBody = """
            [
                {
                    "type": "SENIOR",
                    "ticketCount": 1
                }
            ]
            """;

        mockMvc.perform(post("/cinema/accounts/{accountId}/ticket-purchases", 1L)
                .contentType("application/json")
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid request body. Please provide a valid ticket request."))
                .andExpect(jsonPath("$.type").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Given payment fails, when endpoint is called, then returns server error")
    void givenPaymentFails_whenEndpointCalled_thenReturnsServerError() throws Exception {
        when(paymentService.debitAccount(eq(1L), eq(new BigDecimal("25.00"))))
                .thenReturn(ResponseEntity.status(500).body("Payment failed"));

        String requestBody = """
            [
                {
                    "type": "ADULT",
                    "ticketCount": 1
                }
            ]
            """;

        mockMvc.perform(post("/cinema/accounts/{accountId}/ticket-purchases", 1L)
                .contentType("application/json")
                .content(requestBody))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.status").value("502"))
                .andExpect(jsonPath("$.title").value("Payment Service Error"))
                .andExpect(jsonPath("$.detail").value("An error occurred while processing the payment. Please try again later."))
                .andExpect(jsonPath("$.type").value("PAYMENT_SERVICE_ERROR"));

        verify(paymentService).debitAccount(eq(1L), eq(new BigDecimal("25.00")));
    }

    @Test
    @DisplayName("Given seat reservation fails, when endpoint is called, then returns server error")
    void givenSeatReservationFails_whenEndpointCalled_thenReturnsServerError() throws Exception {
        when(paymentService.debitAccount(eq(1L), eq(new BigDecimal("25.00"))))
                .thenReturn(ResponseEntity.ok("Payment successful"));

        when(seatReservationService.reserveSeats(eq(1L), eq(1L)))
                .thenReturn(ResponseEntity.status(500).body("Seat reservation failed"));

        String requestBody = """
            [
                {
                    "type": "ADULT",
                    "ticketCount": 1
                }
            ]
            """;

        mockMvc.perform(post("/cinema/accounts/{accountId}/ticket-purchases", 1L)
                .contentType("application/json")
                .content(requestBody))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.status").value("502"))
                .andExpect(jsonPath("$.title").value("Seat Reservation Service Error"))
                .andExpect(jsonPath("$.detail").value("An error occurred while processing the seat reservation. Please try again later."))
                .andExpect(jsonPath("$.type").value("SEAT_RESERVATION_SERVICE_ERROR"));

        verify(paymentService).debitAccount(eq(1L), eq(new BigDecimal("25.00")));
        verify(seatReservationService).reserveSeats(eq(1L), eq(1L));
    }
}
