package uk.gov.dwp.engineering.recruitment.purchase;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates the ticket purchase process by executing each step in order.
 */
@Slf4j
public class TicketPurchaseOrchestrator {

    private final List<TicketPurchaseStep> steps = new ArrayList<>();

    /**
     * Registers a step to be executed as part of the purchase process.
     *
     * @param step the step to add
     * @return this orchestrator, for fluent chaining
     */
    public TicketPurchaseOrchestrator addStep(TicketPurchaseStep step) {
        steps.add(step);
        return this;
    }

    /**
     * Executes all registered steps in order against the given context.
     *
     * @param context the shared purchase state
     */
    public void execute(TicketPurchaseContext context) {
        for (TicketPurchaseStep step : steps) {
            try {
                step.execute(context);
            } catch (Exception e) {
                log.error("Purchase step failed: {}.",
                        step.getClass().getSimpleName(), e);
                throw e;
            }
        }
    }
}
