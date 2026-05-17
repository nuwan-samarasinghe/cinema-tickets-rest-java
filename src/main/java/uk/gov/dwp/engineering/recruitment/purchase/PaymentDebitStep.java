package uk.gov.dwp.engineering.recruitment.purchase;

import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.dwp.engineering.recruitment.exception.PaymentServiceException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;

/**
 * Debits the account for the total ticket cost.
 */
@Slf4j
@RequiredArgsConstructor
public class PaymentDebitStep implements TicketPurchaseStep {

    private final PaymentService paymentService;

    /**
     * Executes the payment debit step by calling the payment service to debit
     * the account for the total amount paid. If the payment fails, a
     * PaymentServiceException is thrown.
     *
     * @param ctx the ticket purchase context containing the account ID and
     * total amount paid
     */
    @Override
    public void execute(TicketPurchaseContext ctx) {
        log.info("Debiting account: {}, amount: {}", ctx.getAccountId(), ctx.getTotalAmountPaid());
        ResponseEntity<String> response;
        try {
            response = paymentService.debitAccount(ctx.getAccountId(), ctx.getTotalAmountPaid());
        } catch (Exception e) {
            throw new PaymentServiceException("Payment failed", e);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new PaymentServiceException("Payment failed");
        }
    }
}
