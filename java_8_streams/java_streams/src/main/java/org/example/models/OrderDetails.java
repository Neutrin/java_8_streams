package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Getter
@AllArgsConstructor
@ToString
public class OrderDetails {
    private final BigDecimal amount;
    private final String currency;
    private final Instant deliveryDate;
    private final Integer itemCounts;
    private final Map<String, String> attributes;
}
