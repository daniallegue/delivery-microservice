package nl.tudelft.sem.template.example.service;


import nl.tudelft.sem.template.example.exception.*;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.Issue;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
        for (int i = 0; i < 5; i++) {
            Delivery delivery = new Delivery();
            delivery.setId((long) i);

            Order order = new Order();
            order.setOrderId((long) i + 10);
            order.setStatus(i % 2 == 0 ? Order.StatusEnum.DELIVERED : Order.StatusEnum.ON_TRANSIT);

            Issue issue = null;
            if (i % 2 == 0) {
                issue = new Issue();
                issue.setDescription("Issue " + i);
            }
            delivery.setOrder(order);
            delivery.setIssue(issue);
            mockDeliveries.add(delivery);
        }
    }

    @Test
    void testSaveRating() throws OrderNotFoundException, RatingNotFoundException, IllegalOrderStatusException {
        when(orderRepository.findById((long) 10)).thenReturn(Optional.of(order));
        when(deliveryRepository.findDeliveryByOrder_OrderId((long) 10)).thenReturn(delivery);
        when(deliveryRepository.findById((long) 20)).thenReturn(Optional.of(delivery));

        Rating savedRating = analyticsService.saveRating(rating, (long) 10);
        assertEquals(3, savedRating.getGrade());
        assertEquals("Fine", savedRating.getComment());
        assertEquals(analyticsService.getRatingByOrderId((long) 10), rating);
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
    void testGetDeliveriesPerDaySuccess() throws CourierNotFoundException, NoDeliveriesException {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(true);
        when(deliveryRepository.findByCourierId(courierId)).thenReturn(mockDeliveries);

        int result = analyticsService.getDeliveriesPerDay(courierId);
        assertEquals(1, result); // the number comes from my implementation of mockDeliveries inn setup() 5/7 = 1
    }

    @Test
    void testGetDeliveriesPerDayCourierNotFound() {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(false);

        assertThrows(CourierNotFoundException.class, () -> analyticsService.getDeliveriesPerDay(courierId));
    }

    @Test
    void testGetDeliveriesPerDayNoDeliveries() {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(true);
        when(deliveryRepository.findByCourierId(courierId)).thenReturn(new ArrayList<>());

        assertThrows(NoDeliveriesException.class, () -> analyticsService.getDeliveriesPerDay(courierId));
    }

    @Test
    void testGetSuccessfulDeliveriesSuccess() throws CourierNotFoundException, NoDeliveriesException {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(true);
        when(deliveryRepository.findByCourierId(courierId)).thenReturn(mockDeliveries);

        int result = analyticsService.getSuccessfulDeliveries(courierId);
        assertEquals(3, result); //the number comes from my implementation of mockDeliveries inn setup()
    }

    @Test
    void testGetSuccessfulDeliveriesCourierNotFound() {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(false);
        assertThrows(CourierNotFoundException.class, () -> analyticsService.getSuccessfulDeliveries(courierId));
    }

    @Test
    void testGetSuccessfulDeliveriesNoDeliveries() {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(true);
        when(deliveryRepository.findByCourierId(courierId)).thenReturn(List.of());
        assertThrows(NoDeliveriesException.class, () -> analyticsService.getSuccessfulDeliveries(courierId));
    }
    @Test
    void testGetCourierIssuesSuccess() throws CourierNotFoundException, NoDeliveriesException {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(true);
        when(deliveryRepository.findByCourierId(courierId)).thenReturn(mockDeliveries);

        List<String> issues = analyticsService.getCourierIssues(courierId);
        assertNotNull(issues);
    }

    @Test
    void testGetCourierIssuesCourierNotFound() {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(false);
        assertThrows(CourierNotFoundException.class, () -> analyticsService.getCourierIssues(courierId));
    }

    @Test
    void testGetCourierIssuesNoDeliveries() {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(true);
        when(deliveryRepository.findByCourierId(courierId)).thenReturn(List.of());
        assertThrows(NoDeliveriesException.class, () -> analyticsService.getCourierIssues(courierId));
    }
}
