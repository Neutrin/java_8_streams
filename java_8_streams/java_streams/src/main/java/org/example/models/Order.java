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
public final class Order {
    private final String orderId;
    private final String userEmail;
    private final Status status;
    private final OrderDetails orderDetails;
    private final Instant createdAt;
    private final Instant updatedAt;
}


