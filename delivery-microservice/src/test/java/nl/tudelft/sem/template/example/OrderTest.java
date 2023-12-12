package nl.tudelft.sem.template.example;

import static org.assertj.core.api.Assertions.assertThat;

import nl.tudelft.sem.template.example.controller.OrderController;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.example.service.OrderService;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Vendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderTest {
    @Mock
    OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
    OrderService orderService = new OrderService(orderRepository);
    OrderController orderController = new OrderController(orderService);
    List<Order> orderList = new ArrayList<>();

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
    }

    @Test
    void getOrderStatusTest() {
        String status = orderController.deliveryOrderOrderIdStatusGet(1, 1).getBody();
        assertThat(status).isEqualTo("Pending");

        status = orderController.deliveryOrderOrderIdStatusGet(2, 1).getBody();
        assertThat(status).isEqualTo("Accepted");

        status = orderController.deliveryOrderOrderIdStatusGet(3, 1).getBody();
        assertThat(status).isEqualTo("Rejected");

        status = orderController.deliveryOrderOrderIdStatusGet(4, 1).getBody();
        assertThat(status).isEqualTo("Preparing");

        assertThat(orderController.deliveryOrderOrderIdStatusGet(5, 1).getBody())
                .isEqualTo("Order id not found");
    }

    @Test
    void putOrderStatusPendingToPendingTest() {
        String newStatus = "Pending";
        orderController.deliveryOrderOrderIdStatusPut(1, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(1, 1).getBody();
        assertThat(status).isEqualTo("Pending");
    }

    @Test
    void putOrderStatusPendingToAccepted() {
        String newStatus = "Accepted";
        orderController.deliveryOrderOrderIdStatusPut(1, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(1, 1).getBody();
        assertThat(status).isEqualTo("Accepted");
    }

    @Test
    void putOrderStatusPendingToRejected() {
        String newStatus = "Rejected";
        orderController.deliveryOrderOrderIdStatusPut(1, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(1, 1).getBody();
        assertThat(status).isEqualTo("Rejected");
    }

    @Test
    void putOrderStatusAcceptedToPreparingTest() {
        String newStatus = "Preparing";
        orderController.deliveryOrderOrderIdStatusPut(2, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(2, 1).getBody();
        assertThat(status).isEqualTo("Preparing");
    }

    @Test
    void putOrderStatusPendingToPreparingTest() {
        String newStatus = "Preparing";
        orderController.deliveryOrderOrderIdStatusPut(1, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(1, 1).getBody();
        assertThat(status).isEqualTo("Pending");
    }

    @Test
    void putOrderStatusRejectedToPreparingTest() {
        String newStatus = "Preparing";
        orderController.deliveryOrderOrderIdStatusPut(3, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(3, 1).getBody();
        assertThat(status).isEqualTo("Rejected");
    }

    @Test
    void putOrderStatusAcceptedToElseTest() {
        String newStatus = "Pending";
        orderController.deliveryOrderOrderIdStatusPut(2, 1, newStatus);
        String status = orderController.deliveryOrderOrderIdStatusGet(2, 1).getBody();
        assertThat(status).isEqualTo("Accepted");
    }

    @Test
    void putOrderStatusWrongId() {
        assertThat(orderController.deliveryOrderOrderIdStatusPut(5, 1, "Accepted").getStatusCode()).isEqualTo(HttpStatus.valueOf(400));
    }
}
