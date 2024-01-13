package nl.tudelft.sem.template.example.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.example.service.DeliveryService;
import nl.tudelft.sem.template.example.service.OrderService;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Vendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Optional;

public class OrderControllerTest {
    private final OrderRepository orderRepository = Mockito.mock(OrderRepository.class);

    private final OrderService orderService = new OrderService(orderRepository);

    private final DeliveryService deliveryService = Mockito.mock(DeliveryService.class);

    private final AuthorizationService authorizationService = Mockito.mock(AuthorizationService.class);

    private final DeliveryController orderController = new DeliveryController(deliveryService, orderService, authorizationService);

    @BeforeEach
    void setup() {
        Location location = new Location(5.0,1.0);
        Vendor vendor = new Vendor(3L, 9L, location, new ArrayList<>());
        Order order = new Order(1L, 3L, vendor, Order.StatusEnum.PENDING,  location);
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        location = new Location(11.5,7.2);
        vendor = new Vendor(4L, Long.MAX_VALUE, location, new ArrayList<>());
        order = new Order(2L, 1L, vendor, Order.StatusEnum.ACCEPTED,  location);
        Mockito.when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        location = new Location(0.0,0.0);
        vendor = new Vendor(6L, Long.MIN_VALUE, location, new ArrayList<>());
        order = new Order(3L, 0L, vendor, Order.StatusEnum.REJECTED,  location);
        Mockito.when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        location = new Location(Double.MAX_VALUE, Double.MAX_VALUE);
        vendor = new Vendor(1L, 11L, location, new ArrayList<>());
        order = new Order(4L, 5L, vendor, Order.StatusEnum.PREPARING,  location);
        Mockito.when(orderRepository.findById(4L)).thenReturn(Optional.of(order));

        location = new Location(5.0, 13.0);
        vendor = new Vendor(5L, 10L, location, new ArrayList<>());
        order = new Order(5L, 6L, vendor, Order.StatusEnum.GIVEN_TO_COURIER,  location);
        Mockito.when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        location = new Location(1.0, 8.0);
        vendor = new Vendor(11L, 8L, location, new ArrayList<>());
        order = new Order(6L, 5L, vendor, Order.StatusEnum.ON_TRANSIT,  location);
        Mockito.when(orderRepository.findById(6L)).thenReturn(Optional.of(order));

        location = new Location(2.0, 3.0);
        vendor = new Vendor(10L, 6L, location, new ArrayList<>());
        order = new Order(7L, 4L, vendor, Order.StatusEnum.DELIVERED,  location);
        Mockito.when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
    }

    @Test
    void getOrderStatusPendingTest() {
        String status = orderController.deliveryOrderOrderIdStatusGet(1, 1).getBody();
        assertThat(status).isEqualTo("Pending");
    }

    @Test
    void getOrderStatusAcceptedTest() {
        String status = orderController.deliveryOrderOrderIdStatusGet(2, 1).getBody();
        assertThat(status).isEqualTo("Accepted");
    }

    @Test
    void getOrderStatusRejectedTest() {
        String status = orderController.deliveryOrderOrderIdStatusGet(3, 1).getBody();
        assertThat(status).isEqualTo("Rejected");
    }
    @Test
    void getOrderStatusPreparingTest() {
        String status = orderController.deliveryOrderOrderIdStatusGet(4, 1).getBody();
        assertThat(status).isEqualTo("Preparing");
    }

    @Test
    void getOrderStatusGivenToCourierTest() {
        String status = orderController.deliveryOrderOrderIdStatusGet(5, 1).getBody();
        assertThat(status).isEqualTo("Given_To_Courier");
    }

    @Test
    void getOrderStatusOnTransitTest() {
        String status = orderController.deliveryOrderOrderIdStatusGet(6, 1).getBody();
        assertThat(status).isEqualTo("On_Transit");
    }

    @Test
    void getOrderStatusDeliveredTest() {
        String status = orderController.deliveryOrderOrderIdStatusGet(7, 1).getBody();
        assertThat(status).isEqualTo("Delivered");
    }

    @Test
    void orderNotFoundTest() {
        assertThat(orderController.deliveryOrderOrderIdStatusGet(8, 1).getBody()).isEqualTo("Order id not found");
    }

    @Test
    void putOrderStatusPendingToPendingTest() {
        String newStatus = "Pending";
        ResponseEntity<Void> response = orderController.deliveryOrderOrderIdStatusPut(1, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(1, 1).getBody();

        assertThat(status).isEqualTo("Pending");
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void putOrderStatusPendingToAcceptedTest() {
        String newStatus = "Accepted";
        ResponseEntity<Void> response = orderController.deliveryOrderOrderIdStatusPut(1, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(1, 1).getBody();

        assertThat(status).isEqualTo("Accepted");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void putOrderStatusPendingToRejectedTest() {
        String newStatus = "Rejected";
        ResponseEntity<Void> response = orderController.deliveryOrderOrderIdStatusPut(1, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(1, 1).getBody();

        assertThat(status).isEqualTo("Rejected");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void putOrderStatusAcceptedToPreparingTest() {
        String newStatus = "Preparing";
        ResponseEntity<Void> response = orderController.deliveryOrderOrderIdStatusPut(2, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(2, 1).getBody();

        assertThat(status).isEqualTo("Preparing");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void putOrderStatusPreparingToGivenToCourierTest() {
        String newStatus = "Given_To_Courier";
        ResponseEntity<Void> response =  orderController.deliveryOrderOrderIdStatusPut(4, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(4, 1).getBody();

        assertThat(status).isEqualTo("Given_To_Courier");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void putOrderStatusGivenToCourierToOnTransitTest() {
        String newStatus = "On_Transit";
        ResponseEntity<Void> response = orderController.deliveryOrderOrderIdStatusPut(5, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(5, 1).getBody();

        assertThat(status).isEqualTo("On_Transit");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void putOrderStatusOnTransitToDeliveredTest() {
        String newStatus = "Delivered";
        ResponseEntity<Void> response = orderController.deliveryOrderOrderIdStatusPut(6, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(6, 1).getBody();

        assertThat(status).isEqualTo("Delivered");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void putOrderStatusPendingToPreparingTest() {
        String newStatus = "Preparing";
        ResponseEntity<Void> response = orderController.deliveryOrderOrderIdStatusPut(1, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(1, 1).getBody();

        assertThat(status).isEqualTo("Pending");
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void putOrderStatusRejectedToPreparingTest() {
        String newStatus = "Preparing";
        ResponseEntity<Void> response = orderController.deliveryOrderOrderIdStatusPut(3, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(3, 1).getBody();

        assertThat(status).isEqualTo("Rejected");
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void putOrderStatusAcceptedToElseTest() {
        String newStatus = "Pending";
        ResponseEntity<Void> response = orderController.deliveryOrderOrderIdStatusPut(2, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(2, 1).getBody();

        assertThat(status).isEqualTo("Accepted");
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void putOrderStatusWrongId() {
        assertThat(orderController.deliveryOrderOrderIdStatusPut(10, 1, "Accepted").getStatusCode()).isEqualTo(HttpStatus.valueOf(400));
    }
    @Test
    void putOrderStatusPreparingToOnTransitTest() {
        String newStatus = "On_Transit";
        orderController.deliveryOrderOrderIdStatusPut(4, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(4, 1).getBody();
        assertThat(status).isEqualTo("Preparing");
    }

    @Test
    void putOrderStatusGivenToCourierToDeliveredTest() {
        String newStatus = "Delivered";
        orderController.deliveryOrderOrderIdStatusPut(5, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(5, 1).getBody();
        assertThat(status).isEqualTo("Given_To_Courier");
    }
    @Test
    void putOrderStatusOnTransitToGivenToCourierTest() {
        String newStatus = "Given_To_Courier";
        orderController.deliveryOrderOrderIdStatusPut(6, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(6, 1).getBody();
        assertThat(status).isEqualTo("On_Transit");
    }

    @Test
    void putOrderStatusDeliveredToOnTransitTest() {
        String newStatus = "On_Transit";
        orderController.deliveryOrderOrderIdStatusPut(7, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(7, 1).getBody();
        assertThat(status).isEqualTo("Delivered");
        assertThat(orderController.deliveryOrderOrderIdStatusPut(5, 1, "Accepted").getStatusCode()).isEqualTo(HttpStatus.valueOf(400));
    }
}
