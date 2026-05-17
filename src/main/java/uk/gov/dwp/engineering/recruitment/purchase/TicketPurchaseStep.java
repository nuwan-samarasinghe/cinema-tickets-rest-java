package uk.gov.dwp.engineering.recruitment.purchase;

/**
 * Represents a single step in the ticket purchase process.
 * Each step is responsible for executing its own logic.
 */
public interface TicketPurchaseStep {

    /**
     * Executes the step logic against the shared purchase context.
     *
     * @param context the shared state of the ticket purchase
     */
    void execute(TicketPurchaseContext context);
    
}
