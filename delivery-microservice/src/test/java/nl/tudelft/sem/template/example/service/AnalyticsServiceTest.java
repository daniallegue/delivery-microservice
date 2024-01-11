package nl.tudelft.sem.template.example.service;


import nl.tudelft.sem.template.example.exception.IllegalOrderStatusException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Rating;
import nl.tudelft.sem.template.example.exception.RatingNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
public class AnalyticsServiceTest {

    private DeliveryRepository deliveryRepository;
    private OrderRepository orderRepository;
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        deliveryRepository = Mockito.mock(DeliveryRepository.class);
        analyticsService = new AnalyticsService(deliveryRepository);
        orderRepository = Mockito.mock(OrderRepository.class);
    }

    @Test
    void testSaveRating() throws OrderNotFoundException, RatingNotFoundException, IllegalOrderStatusException {
        Rating rating = new Rating();
        rating.setComment("Fine");
        rating.setGrade(3);

        Order order = new Order();
        order.setOrderId((long) 10);
        order.setStatus(Order.StatusEnum.DELIVERED);

        Delivery delivery = new Delivery();
        delivery.setId((long) 20);
        delivery.setOrder(order);

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
        Rating rating = new Rating();
        rating.setComment("Fine");
        rating.setGrade(3);

        Order order = new Order();
        order.setOrderId((long) 10);
        order.setStatus(Order.StatusEnum.ACCEPTED);

        Delivery delivery = new Delivery();
        delivery.setId((long) 20);
        delivery.setOrder(order);

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
        Rating rating = new Rating();
        rating.setGrade(4);
        rating.setComment("Good");


        Order order = new Order();
        order.setOrderId((long) 10);

        Delivery delivery = new Delivery();
        delivery.setId((long) 20);
        delivery.setRating(rating);
        delivery.setOrder(order);

        when(deliveryRepository.findDeliveryByOrder_OrderId((long) 10)).thenReturn(delivery);
        when(deliveryRepository.findById((long) 20)).thenReturn(Optional.of(delivery));

        Rating foundRating = analyticsService.getRatingByOrderId((long) 10);
        assertEquals(4, foundRating.getGrade());
        assertEquals("Good", foundRating.getComment());
        assertEquals(analyticsService.getRatingByOrderId((long) 10), rating);
    }


    @Test
    void testGetRatingByOrderIdNotFound() {
        when(orderRepository.findById((long) 10)).thenReturn(null);
        assertThrows(OrderNotFoundException.class, () -> analyticsService.getRatingByOrderId((long) 10));
    }

}
