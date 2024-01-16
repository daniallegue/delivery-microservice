package nl.tudelft.sem.template.example.service;


import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.IllegalOrderStatusException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.exception.RatingNotFoundException;
import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.exception.*;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    private VendorRepository vendorRepository;
    private List<Delivery> mockDeliveries;
    private DeliveryService deliveryService;
    private Rating rating;
    private Order order;
    private Delivery delivery;
    @BeforeEach
    void setUp() {
        deliveryRepository = Mockito.mock(DeliveryRepository.class);
        courierService = Mockito.mock(CourierService.class);
        vendorRepository = Mockito.mock(VendorRepository.class);
        orderRepository = Mockito.mock(OrderRepository.class);
        deliveryService = new DeliveryService(deliveryRepository, orderRepository, vendorRepository, Mockito.mock(VendorService.class), Mockito.mock(ConfigurationProperties.class));

        analyticsService = new AnalyticsService(deliveryRepository, courierService, vendorRepository, orderRepository, deliveryService);

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

    @Test
    void testGetCourierEfficiencySuccess() throws CourierNotFoundException {
        Long courierId = 1L;
        when(courierService.doesCourierExist(courierId)).thenReturn(true);
        List<Delivery> deliveries = createMockDeliveries();
        when(deliveryRepository.findByCourierId(courierId)).thenReturn(deliveries);
        int result = analyticsService.getCourierEfficiency(courierId);

        assertEquals(163, result);
    }

    @Test
    void testGetCourierEfficiencyCourierNotFound() {
        Long courierId = 1L;

        when(courierService.doesCourierExist(courierId)).thenReturn(false);
        assertThrows(CourierNotFoundException.class, () -> analyticsService.getCourierEfficiency(courierId));
    }

    @Test
    void testGetVendorAverage() throws VendorNotFoundException {

        Order order1 = new Order();
        order1.setOrderId(1L);
        Order order2 = new Order();
        order2.setOrderId(2L);
        Order order3 = new Order();
        order3.setOrderId(3L);
        List<Order> mockOrders = new ArrayList<>();

        mockOrders.add(order1);
        mockOrders.add(order2);
        mockOrders.add(order3);

        List<Delivery> deliveries = createMockDeliveries();

        when(vendorRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findOrdersByVendorId(1L)).thenReturn(mockOrders);
        when(deliveryRepository.findDeliveryByOrder_OrderId(1L)).thenReturn(deliveries.get(0));
        when(deliveryRepository.findDeliveryByOrder_OrderId(2L)).thenReturn(deliveries.get(1));
        when(deliveryRepository.findDeliveryByOrder_OrderId(3L)).thenReturn(deliveries.get(2));

        int result = analyticsService.getVendorAverage(1L);
        assertEquals(5400, result);

    }

    @Test
    void testGetVendorAverageVendorNotFound() {
        Long vendorId = 1L;

        when(vendorRepository.existsById(vendorId)).thenReturn(false);
        assertThrows(VendorNotFoundException.class, () -> analyticsService.getVendorAverage(vendorId));
    }

    private List<Delivery> createMockDeliveries() {
        List<Delivery> deliveries = new ArrayList<>();

        Delivery delivery1 = new Delivery();
        delivery1.setId(1L);
        Order order1 = new Order();
        order1.setOrderId(1L);
        order1.setStatus(Order.StatusEnum.DELIVERED);
        Vendor vendor1 = new Vendor();
        vendor1.setAddress(new Location(0.0, 0.0));
        order1.setVendor(vendor1);
        Location destination1 = new Location(5.0, 6.0);
        order1.setDestination(destination1);
        Time time1 = new Time();
        time1.setPickUpTime(OffsetDateTime.parse("2024-01-01T10:00:00Z"));
        time1.setDeliveredTime(OffsetDateTime.parse("2024-01-01T11:00:00Z"));
        delivery1.setTime(time1);
        delivery1.setOrder(order1);
        deliveries.add(delivery1);

        Delivery delivery2 = new Delivery();
        delivery2.setId(2L);
        Order order2 = new Order();
        order2.setOrderId(2L);
        Vendor vendor2 = new Vendor();
        order2.setStatus(Order.StatusEnum.DELIVERED);
        vendor2.setAddress(new Location(-2.0, -1.0));
        order2.setVendor(vendor2);
        Location destination2 = new Location(5.0, 6.0);
        destination2 = new Location(5.0, 6.0);
        order2.setDestination(destination2);
        Time time2 = new Time();
        time2.setPickUpTime(OffsetDateTime.parse("2024-01-01T10:00:00Z"));
        time2.setDeliveredTime(OffsetDateTime.parse("2024-01-01T12:00:00Z"));
        delivery2.setTime(time2);
        delivery2.setOrder(order2);
        deliveries.add(delivery2);

        Delivery delivery3 = new Delivery();
        delivery3.setId(3L);
        Order order3 = new Order();
        order3.setOrderId(3L);
        order3.setStatus(Order.StatusEnum.ON_TRANSIT);
        Vendor vendor3 = new Vendor();
        vendor3.setAddress(new Location(-1.0, -1.0));
        order3.setVendor(vendor3);
        Location destination3 = new Location(5.0, 6.0);
        order3.setDestination(destination3);
        Time time3 = new Time();
        time3.setPickUpTime(OffsetDateTime.parse("2024-01-01T10:00:00Z"));
        time3.setDeliveredTime(OffsetDateTime.parse("2024-01-01T12:00:00Z"));
        delivery3.setTime(time3);
        delivery3.setOrder(order3);
        deliveries.add(delivery3);

        return deliveries;
    }
}
