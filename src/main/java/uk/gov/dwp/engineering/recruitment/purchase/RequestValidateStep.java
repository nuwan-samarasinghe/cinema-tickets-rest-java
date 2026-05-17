package uk.gov.dwp.engineering.recruitment.purchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.dwp.engineering.recruitment.validation.TicketPurchaseValidator;

/**
 * Validates the ticket purchase request.
 */
@Slf4j
@RequiredArgsConstructor
public class RequestValidateStep implements TicketPurchaseStep {

    private final TicketPurchaseValidator validator;

    /**
     * Executes the validation step by calling the validator to validate the
     * account ID and ticket requests.
     *
     * @param ctx the ticket purchase context containing the account ID and
     * ticket requests
     */
    @Override
    public void execute(TicketPurchaseContext ctx) {
        log.info("Validating ticket purchase request for account ID {} with {} ticket requests",
                ctx.getAccountId(), ctx.getTicketRequests().length);
        validator.validate(ctx.getAccountId(), ctx.getTicketRequests());
        log.info("Validation successful for account ID {}", ctx.getAccountId());
    }
}
