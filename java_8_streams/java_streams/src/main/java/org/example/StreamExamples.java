package org.example;

import org.example.models.Order;
import org.example.models.Status;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Locale.filter;
import static java.util.stream.Collectors.toList;
import static org.example.Main.generateAndFetchOrders;

public class StreamExamples {

    public static Instant instantDefault  =  Instant.parse("2025-10-24T00:00:00Z"); // 15 days before today
    public static ZoneId indiaZoneId = ZoneId.of("Asia/Kolkata");

    public static BigDecimal bigDecimalThreshold = BigDecimal.valueOf(20000.45);
    public static void main(String []args) {
        List<Order> orders = generateAndFetchOrders();
        int totalOrders = orders.size();


        List<Order> sortedOrders = orders.stream().sorted(Comparator.comparing(order ->
                order.getOrderDetails().getAmount())).collect(toList());

        Stream<Order> minOrders = sortedOrders.stream().limit(Math.min(totalOrders, 2));


        Stream<Order> maxOrders = IntStream.range(0, Math.min(2, totalOrders))
                .mapToObj(i -> sortedOrders.get(totalOrders-1-i));

        List<Order> combinedList = Stream.concat(minOrders, maxOrders)
                        .distinct().
                collect(toList());


        System.out.println("***** list of all orders is ******");
        orders.forEach(System.out::println);

        System.out.println("**** and the sorted orders are ****");
        combinedList.forEach(System.out::println);



        List<Order> everyThirdIndexOrder = IntStream.rangeClosed(0, totalOrders).
                filter(index -> (index < totalOrders) && ((index+1)%3 ==0))
                .mapToObj(orders::get).
                collect(toList());

        System.out.println("****** every third order becomes *******");
        everyThirdIndexOrder.forEach(System.out::println);

        // option one using reduce operations
        orders.stream().
                reduce((orderOne, orderTwo) -> {
                    if(orderOne.getCreatedAt() == null){
                        return orderTwo;
                    }
                    if(orderTwo.getCreatedAt() == null){
                        return orderOne;
                    }
                    return (orderOne.getCreatedAt().isBefore(orderTwo.getCreatedAt()) ? orderOne : orderTwo);
                }).ifPresent(orderOne -> System.out.println(" order one with earliest created at is" + orderOne));

        // there is one stream operation also provided for the same

        orders.stream().
                min(Comparator.comparing(Order::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()))).ifPresent(
                                order -> System.out.println(" order with earliest created at times becomes" + order));



        orders.stream().
                reduce((orderOne, orderTwo) -> {
                    if(orderOne.getCreatedAt() == null){
                        return orderTwo;
                    }
                    if(orderTwo.getCreatedAt() == null){
                        return orderOne;
                    }

                    return (orderOne.getCreatedAt().isAfter(orderTwo.getCreatedAt()) ? orderOne : orderTwo);
                }).ifPresent(order -> System.out.println(" order with highest create order times is" + order));

        orders.stream().max(Comparator.comparing(Order::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder()))).ifPresent(
                order -> System.out.println(" order with highest create order time becomes " + order));


        List<Order> orderWithDeliveryInNext48Hours = orders.stream().
                filter(order -> {
                    Instant deliveryDate = order.getOrderDetails().getDeliveryDate();
                    if(deliveryDate == null){
                        return false;
                    }
                    Instant currentDate = Instant.now();
                    Instant nextDate = currentDate.plus(Duration.ofHours(48));
                    return (deliveryDate.isAfter(currentDate) && deliveryDate.isBefore(nextDate));

                }).collect(toList());
        System.out.println("******* orders with delivery in next 48 hours are *******");
        orderWithDeliveryInNext48Hours.forEach(System.out::println);


        List<String> emailIdListBecomes = orders.stream()
                .filter(order -> order.getStatus() == Status.PAID && order.getUserEmail() != null)
                .map(order -> order.getUserEmail().trim().toLowerCase())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(toList());

        System.out.println("******** Email ids sorted with trimmed spaces becomes ******");
        emailIdListBecomes.forEach(System.out::println);



        Optional<BigDecimal> finalAmount = orders.stream()
                .map(order -> order.getOrderDetails().getAmount())
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add);


        System.out.println("*** the final amount becomes ****" + finalAmount);

        // to convert the complete object into int stream we could use the same as follows

        int finalAmountNew = orders.stream()
                .map(Order::getOrderDetails)
                .mapToInt(orderDetails -> orderDetails.getAmount().intValue()).
                sum();

        System.out.println("***** the final amount becomes *****" + finalAmountNew);

        // Return the top N order via amount in descending
        List<Order> topFiveSortedOrder = orders.stream()
                .sorted(
                        Comparator.comparing(
                                order -> order.getOrderDetails().getAmount(),
                                (BigDecimal amountOne, BigDecimal amountTwo) -> amountTwo.compareTo(amountOne)
                        )
                )
                .limit(5)
                .collect(toList());

        System.out.println("******* and the top n orders becomes********");
        topFiveSortedOrder.forEach(System.out::println);

        topFiveSortedOrder = orders.stream().
                sorted(
                        Comparator.comparing( order -> order.getOrderDetails().getAmount(), Comparator.reverseOrder()
                )).limit(5)
                .collect(toList());

        System.out.println("******* and the top n orders becomes********");
        topFiveSortedOrder.forEach(System.out::println);


        topFiveSortedOrder = orders.stream().
                sorted(
                        Comparator.comparing(
                                order -> order.getOrderDetails() == null?  null :order.getOrderDetails().getAmount() ,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                ).limit(5)
                .collect(toList());

        System.out.println("******* and the top n orders becomes********");
        topFiveSortedOrder.forEach(System.out::println);

        Optional<BigDecimal> maxValueOptional = orders.stream().
                map(order -> order.getOrderDetails().getAmount())
                .reduce(BigDecimal::max);
        BigDecimal maxValue = maxValueOptional.orElse(null);

        Optional<BigDecimal> minOrderOptional = orders.stream().
                map(order -> order.getOrderDetails().getAmount())
                        .reduce(BigDecimal::min);

        BigDecimal minValue = minOrderOptional.orElse(null);

        System.out.println("*** and the difference becomes ****** =" + maxValue.subtract(minValue));
        // given a timestap t return the first paid order created sfter t
        // this is the sorted appraoch
        Optional<List<Order>> orderWithRecentCreatedAtOptional = Optional.of(orders.stream().
                filter(order -> order.getStatus() == Status.PAID)
                .sorted(Comparator.comparing(Order::getCreatedAt))
                .filter(order -> order.getCreatedAt().isAfter(instantDefault))
                .limit(1)
                .collect(toList()));

        List<Order> orderWithRecentCreatedAt = orderWithRecentCreatedAtOptional.orElse(null);
        System.out.println("***** order with most recent created at is ******" + orderWithRecentCreatedAt);
        if(orderWithRecentCreatedAt != null){
            orderWithRecentCreatedAt.forEach(System.out::println);
        }
        // this is the non-sorted approach
        orders.stream()
                .filter(order -> order != null && order.getStatus() == Status.PAID)
                .filter(order -> order.getCreatedAt().isAfter(instantDefault))
                .min(Comparator.comparing(Order::getCreatedAt))
                .ifPresent(order -> System.out.println("****** order with created at after t becomes *****" + order));


        List<Order> ordersWithUpdatedAtUnder24hrs = orders.stream()
                .filter(order -> order != null && order.getUpdatedAt() != null && order.getCreatedAt() != null && order.getUpdatedAt().isAfter(order.getCreatedAt()))
                .filter(order -> order.getUpdatedAt().equals(order.getCreatedAt())
                        || order.getCreatedAt().plusSeconds(24*60*60).isAfter(order.getUpdatedAt()))
                        .collect(toList());


        System.out.println(" ****** list of order with updated at within 24 hours of created at ******");
        ordersWithUpdatedAtUnder24hrs.forEach(System.out::println);

        int p = 2;
        int k = 3;
    // PAGE = 4 size of page is arond = 10
        List<Order> orderList = orders.stream()
                .filter(order -> order.getCreatedAt() != null)
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .skip(p*k)
                .limit(k)
                .collect(toList());

        System.out.println("******* list of order with page = 2 and page size = 3 is");
        orderList.forEach(System.out::println);
       // Join all **order IDs** into a single **comma-separated** string with **deterministic ordering**.

        orders.stream().
                map(Order::getOrderId)
                .sorted()
                .reduce((idOne, idTwo) ->  String.join(",", idOne, idTwo)).
                ifPresent(output -> System.out.println("*** the output string becomes ****" + output));


        List<String> promoCodes = orders.stream()
                .filter(order -> order.getOrderDetails() != null && order.getOrderDetails().getAttributes() != null)
                .filter(order -> order.getOrderDetails().getAttributes().getOrDefault("promo", null) != null)
                .map(order -> order.getOrderDetails().getAttributes().getOrDefault("promo", null))
                .sorted()
                .distinct()
                .collect(toList());
        System.out.println(" **** and the promo codes becomes ****");
        promoCodes.forEach(System.out::println);


        System.out.println("***** list of attributes after flattemning becomes ******");
        List<Map.Entry<String, String>> mapAttributes = orders.stream().
                filter(order -> order != null && order.getOrderDetails() != null && order.getOrderDetails().getAttributes() !=null)
                .flatMap(order -> order.getOrderDetails().getAttributes().entrySet().stream())
                .collect(toList());

        mapAttributes.forEach(entry -> System.out.println(entry.getKey() + " : " + entry.getValue()));
        // format all the delivery instances to convert them to  a timezone and sort them


        List<ZonedDateTime> dateTimesZoned = orders.stream()
                .filter(order -> order != null && order.getOrderDetails() != null && order.getOrderDetails().getDeliveryDate() != null)
                .map(order -> ZonedDateTime.ofInstant(order.getOrderDetails().getDeliveryDate(), indiaZoneId))
                .collect(toList());

        dateTimesZoned.forEach(dateTime -> System.out.println(" the zoned date time becomes " + dateTime) );

        // return whether all the orders have strictly positive amounts

        Boolean areOrderStrictlyIncreasing = orders.stream()
                .filter(order -> order != null && order.getOrderDetails() != null)
                .map(order -> order.getOrderDetails().getAmount())
                .noneMatch(amount -> amount == null || amount.signum() == -1);

        System.out.println(" ***** whether all orders are strictly positive amounts becomes *****" + areOrderStrictlyIncreasing);


        Boolean isAnyOrderAmountGreaterThanThreshold = orders.stream()
                .filter(order -> order != null && order.getOrderDetails() != null)
                .map(order -> order.getOrderDetails().getAmount())
                .anyMatch(amount -> amount.compareTo(bigDecimalThreshold) > 0);

        System.out.println(" ***** whether any order amount is greater than threshold becomes *****" + isAnyOrderAmountGreaterThanThreshold);


        List<Order> sortedOrderWithSkip = orders.stream()
                .filter(order -> order != null && order.getOrderDetails() != null && order.getOrderDetails().getAmount() != null)
                .sorted(
                        Comparator.comparing(order -> order.getOrderDetails().getAmount(), Comparator.naturalOrder())
                )
                .skip(4)
                .limit(5)
                .collect(toList());

        System.out.println(" ******* sorted order with skip becomes *******");
        sortedOrderWithSkip.forEach(System.out::println);

        List<BigDecimal> orderAmountsList = orders.stream()
                .filter(order -> order != null && order.getCreatedAt() != null)
                .sorted(Comparator.comparing(Order::getCreatedAt))
                .filter(order -> order.getOrderDetails() != null && order.getOrderDetails().getAmount() != null)
                .map(order -> order.getOrderDetails().getAmount())
                .collect(toList());

        BigDecimal []diff = new BigDecimal[orderAmountsList.size()-1];

        IntStream.rangeClosed(0, orderAmountsList.size()-2)
                .forEach(index -> diff[index] = orderAmountsList.get(index).subtract(orderAmountsList.get(index+1)));

        IntStream.rangeClosed(0, orderAmountsList.size()-2)
                .forEach(index -> System.out.println(" the value of index " + index + " becomes =" + diff[index]));


        List<BigDecimal> amounts =  orders.stream()
                .filter(order -> order != null && order.getOrderDetails() != null && order.getOrderDetails().getAmount() != null)
                .map(order -> order.getOrderDetails().getAmount())
                .sorted(Comparator.comparing(amount -> amount))
                .collect(toList());
        int amountsSize = amounts.size();
        System.out.println(" ******* sorted amounts becomes *******");
        amounts.forEach(System.out::println);
        int nintyPercentIndex = (int)Math.ceil(amountsSize * 0.9) -1;
        IntStream.rangeClosed(nintyPercentIndex, amountsSize-1)
                .forEach(index -> System.out.println(" the 90th percentile amount becomes " + amounts.get(index)));

        // Return all orders that are PAID and have amount > threshold.
        List<Order> orderListWithCondition =  orders.stream()
                .filter(order -> order != null && order.getStatus() == Status.PAID)
                .filter(order -> order.getOrderDetails() != null)
                .filter(order -> order.getOrderDetails().getAmount() != null && order.getOrderDetails().getAmount().compareTo(bigDecimalThreshold)> 0)
                .collect(toList());

        System.out.println(" ******* order list with paid status and amount greater than threshold becomes *******");
        orderListWithCondition.forEach(System.out::println);

        orders.stream()
                .filter(order -> order != null && order.getOrderDetails() != null)
                .max(Comparator.comparing(order -> order.getOrderDetails().getItemCounts()))
                .ifPresent(order -> System.out.println(" order with max item count becomes" + order));

        orders.stream()
                .filter(order -> order != null && order.getOrderDetails() != null)
                .min(Comparator.comparing(order -> order.getOrderDetails().getItemCounts()))
                .ifPresent(order -> System.out.println(" ****** order with minimum order count becomes *****" + order));

        Integer totalCount = orders.stream()
                .filter(order -> order != null && order.getOrderDetails() != null && order.getOrderDetails().getItemCounts() != null)
                .map(order -> order.getOrderDetails().getItemCounts())
                .reduce(0, Integer::sum);

        System.out.println("****** total count for items becomes *****" + totalCount);

        List<String> orderFormattedList = orders.stream()
                .filter(order -> order != null)
                .map(order -> String.format("PRE-%s SUFF%s", order.getOrderId(), order.getOrderId()))
                .collect(toList());
        System.out.println(" ******* formatted order id list becomes *******");
        orderFormattedList.forEach(System.out::println);

    }
}
