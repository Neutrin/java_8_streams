package org.example;

import com.github.javafaker.Faker;
import org.example.models.Order;
import org.example.models.OrderDetails;
import org.example.models.Status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

        for (int i = 1; i <= 5; i++) {
            //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
            // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
            System.out.println("i = " + i);
        }
    }

    public static List<Order> generateAndFetchOrders() {
        Random rnd = new Random(42);
        Faker faker = new Faker();
        String[] currencies = {"INR", "USD", "EUR"};
        Status[] statuses = Status.values();
        List<Order> orders = new ArrayList<Order>();
        for(int i = 0; i <20; i++) {
            // core identifiers & timestamps
            String orderId = UUID.randomUUID().toString();
            String userEmail = faker.internet().emailAddress();

            // createdAt: sometime in last 30 days
            Instant createdAt = faker.date().past(30, TimeUnit.DAYS).toInstant();

            // updatedAt: sometimes null, sometimes after createdAt
            Instant updatedAt = null;
            if (rnd.nextBoolean()) {
                long plusSeconds = 600L + rnd.nextInt(48 * 3600); // 10 min to ~2 days later
                updatedAt = createdAt.plusSeconds(plusSeconds);
            }

            // monetary details
            BigDecimal amount = BigDecimal.valueOf(
                    faker.number().randomDouble(2, 500, 25_000)   // 500.00 – 25000.00
            );
            String currency = currencies[rnd.nextInt(currencies.length)];

            // deliveryDate: 60% chance present, within next 15 days
            Instant deliveryDate = null;
//            if (rnd.nextInt(100) < 60) {
            Date future = faker.date().future(15, TimeUnit.DAYS);
            deliveryDate = future.toInstant();
//            }

            int itemCount = faker.number().numberBetween(1, 6);

            // attributes: simple mutable map (OrderDetails will defensively copy)
            HashMap<String, String> attrs = new HashMap<>();
            attrs.put("channel", faker.options().option("web", "app", "callcenter"));
            attrs.put("promo", faker.options().option("NONE", "NEWUSER10", "DIWALI20"));

            OrderDetails details = new OrderDetails(
                    amount,
                    currency,
                    deliveryDate,
                    itemCount,
                    attrs
            );

            Status status = statuses[rnd.nextInt(statuses.length)];

            Order order = new Order(
                    orderId,
                    userEmail,
                    status,
                    details,
                    createdAt,
                    updatedAt
            );

            orders.add(order);
        }
        return orders;
    }
}