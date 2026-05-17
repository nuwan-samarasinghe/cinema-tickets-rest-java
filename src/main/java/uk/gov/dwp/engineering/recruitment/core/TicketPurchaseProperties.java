package uk.gov.dwp.engineering.recruitment.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cinema-tickets")
public class TicketPurchaseProperties {

    private int maxTicketsPerBooking;
}
