package nl.tudelft.sem.template.example.service;


import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.IllegalOrderStatusException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.exception.RatingNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
public class AnalyticsServiceTest {

    private DeliveryRepository deliveryRepository;
    private OrderRepository orderRepository;
    private AnalyticsService analyticsService;
    private CourierService courierService;
    private List<Delivery> mockDeliveries;
    private Rating rating;
    private Order order;
    private Delivery delivery;
    @BeforeEach
    void setUp() {
        deliveryRepository = Mockito.mock(DeliveryRepository.class);
        courierService = Mockito.mock(CourierService.class);
        analyticsService = new AnalyticsService(deliveryRepository, courierService);
        orderRepository = Mockito.mock(OrderRepository.class);

        rating = new Rating();
        rating.setComment("Fine");
        rating.setGrade(3);

        order = new Order();
        order.setOrderId((long) 10);
        order.setStatus(Order.StatusEnum.DELIVERED);

        delivery = new Delivery();
        delivery.setId((long) 20);
        delivery.setOrder(order);
        delivery.setRating(rating);

        mockDeliveries = new ArrayList<>();
        OffsetDateTime[] deliveryTimes = new OffsetDateTime[] {
                OffsetDateTime.parse("2024-01-01T10:00:00Z"), // 1st of January
                OffsetDateTime.parse("2024-01-01T12:00:00Z"), // 1st of January
                OffsetDateTime.parse("2024-01-01T11:00:00Z"), // 1st of January
                OffsetDateTime.parse("2024-01-03T15:00:00Z"), // 3rd of January
                OffsetDateTime.parse("2024-02-03T15:00:00Z") // 3rd of February
        };


        for (int i = 0; i < 5; i++) {
            Delivery delivery = new Delivery();
            delivery.setId((long) i);
            Order order = new Order();
            order.setOrderId((long) i + 10);
            order.setStatus(Order.StatusEnum.DELIVERED);
//            order.setStatus(i % 2 == 0 ? Order.StatusEnum.DELIVERED : Order.StatusEnum.ON_TRANSIT);

            Time time = new Time();
            time.setDeliveredTime(deliveryTimes[i % deliveryTimes.length]);
            delivery.setTime(time);

            Issue issue = null;
            if (i % 2 == 0) {
                issue = new Issue();
                issue.setDescription("Issue " + i);
            }
            delivery.setOrder(order);
            delivery.setIssue(issue);
            delivery.setCourierId(1L);

            mockDeliveries.add(delivery);
        }

    }

    @Test
    void testSaveRating() throws OrderNotFoundException, RatingNotFoundException, IllegalOrderStatusException {

        Rating newRating = new Rating(5, "good");

        when(orderRepository.findById((long) 10)).thenReturn(Optional.of(order));
        when(deliveryRepository.findDeliveryByOrder_OrderId((long) 10)).thenReturn(delivery);
        when(deliveryRepository.findById((long) 20)).thenReturn(Optional.of(delivery));

        Rating savedRating = analyticsService.saveRating(newRating, (long) 10);
        assertEquals(5, savedRating.getGrade());
        assertEquals("good", savedRating.getComment());
        assertEquals(analyticsService.getRatingByOrderId((long) 10), newRating);

    }

    @Test
    void testSaveRatingNotDelivered() throws OrderNotFoundException, RatingNotFoundException, IllegalOrderStatusException {
        order.setStatus(Order.StatusEnum.ACCEPTED);

        when(orderRepository.findById((long) 10)).thenReturn(Optional.of(order));
        when(deliveryRepository.findDeliveryByOrder_OrderId((long) 10)).thenReturn(delivery);
        when(deliveryRepository.findById((long) 20)).thenReturn(Optional.of(delivery));

        assertThrows( IllegalOrderStatusException.class, () -> analyticsService.saveRating(rating, (long) 10));
    }

    @Test
    void testSaveRatingOrderNotFound() {
        Rating rating = new Rating();
        when(orderRepository.findById(anyLong())).thenReturn(null);
        assertThrows(OrderNotFoundException.class, () -> analyticsService.saveRating(rating, (long) 10));
    }


    @Test
    void testGetRatingByOrderId() throws RatingNotFoundException, OrderNotFoundException {
        when(deliveryRepository.findDeliveryByOrder_OrderId((long) 10)).thenReturn(delivery);
        when(deliveryRepository.findById((long) 20)).thenReturn(Optional.of(delivery));

        Rating foundRating = analyticsService.getRatingByOrderId((long) 10);
        assertEquals(3, foundRating.getGrade());
        assertEquals("Fine", foundRating.getComment());
        assertEquals(analyticsService.getRatingByOrderId((long) 10), rating);
    }


    @Test
    void testGetRatingByOrderIdNotFound() {
        when(orderRepository.findById((long) 10)).thenReturn(null);
        assertThrows(OrderNotFoundException.class, () -> analyticsService.getRatingByOrderId((long) 10));
    }

    @Test
    void testGetRatingRatingNotFound() {
        delivery.setRating(null);
        when(deliveryRepository.findDeliveryByOrder_OrderId((long) 10)).thenReturn(delivery);
        assertThrows(RatingNotFoundException.class, () -> analyticsService.getRatingByOrderId((long) 10));
    }

    @Test
    void testGetDeliveriesPerDaySuccess() throws CourierNotFoundException {
        Long courierId = 1L;
        Order order1 = new Order();
        Delivery delivery1 = new Delivery();
        Time time = new Time();
        time.setDeliveredTime(OffsetDateTime.parse("2024-01-01T10:00:00Z"));
        order1.setStatus(Order.StatusEnum.ON_TRANSIT);
        delivery1.setOrder(order1);
        delivery1.setTime(time);
        mockDeliveries.add(delivery1);
        Delivery delivery2 = new Delivery();
        delivery2.setOrder(null);
        mockDeliveries.add(delivery2);

        when(courierService.doesCourierExist(courierId)).thenReturn(true);
        when(deliveryRepository.findByCourierId(courierId)).thenReturn(mockDeliveries);

        double result = analyticsService.getDeliveriesPerDay(courierId);
        assertEquals(2.0, result);
    }

    @Test
    void testGetDeliveriesPerDayCourierNotFound() {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(false);

        assertThrows(CourierNotFoundException.class, () -> analyticsService.getDeliveriesPerDay(courierId));
    }

    @Test
    void testGetSuccessfulDeliveriesSuccess() throws CourierNotFoundException {
        Long courierId = 1L;
        Order order1 = new Order();
        Delivery delivery1 = new Delivery();
        order1.setStatus(Order.StatusEnum.ON_TRANSIT);
        delivery1.setOrder(order1);
        mockDeliveries.add(delivery1);

        when(courierService.doesCourierExist(courierId)).thenReturn(true);
        when(deliveryRepository.findByCourierId(courierId)).thenReturn(mockDeliveries);

        int result = analyticsService.getSuccessfulDeliveries(courierId);
        assertEquals(5, result); //the number comes from my implementation of mockDeliveries in setup()
    }

    @Test
    void testGetSuccessfulDeliveriesCourierNotFound() {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(false);
        assertThrows(CourierNotFoundException.class, () -> analyticsService.getSuccessfulDeliveries(courierId));
    }

    @Test
    void testGetCourierIssuesSuccess() throws CourierNotFoundException {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(true);
        Delivery delivery = new Delivery();
        Issue issue = new Issue()
                .typeOfIssue("accident")
                .description("I got into an accident while delivering");
        delivery.setIssue(issue);
        when(deliveryRepository.findByCourierId(courierId)).thenReturn(Arrays.asList(delivery));

        List<String> issues = analyticsService.getCourierIssues(courierId);
        assertEquals(Arrays.asList("I got into an accident while delivering"), issues);
    }

    @Test
    void testGetCourierIssuesCourierNotFound() {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(false);
        assertThrows(CourierNotFoundException.class, () -> analyticsService.getCourierIssues(courierId));
    }
}
