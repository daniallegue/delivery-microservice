package nl.tudelft.sem.template.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;

import nl.tudelft.sem.template.example.exception.IllegalOrderStatusException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Vendor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OrderServiceTest {

    private final OrderRepository orderRepository = Mockito.mock(OrderRepository.class);

    private final OrderService orderService = new OrderService(orderRepository);

    Order order1, order2, order3, order4, order5, order6, order7;

    @BeforeEach
    void setup() {
        Location location1 = new Location(5.0, 1.0);
        Vendor vendor1 = new Vendor(3L, 9L, location1, new ArrayList<>());
        order1 = new Order(1L, 3L, vendor1, Order.StatusEnum.PENDING, location1);
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order1));

        Location location2 = new Location(11.5, 7.2);
        Vendor vendor2 = new Vendor(4L, Long.MAX_VALUE, location2, new ArrayList<>());
        order2 = new Order(2L, 1L, vendor2, Order.StatusEnum.ACCEPTED, location2);
        Mockito.when(orderRepository.findById(2L)).thenReturn(Optional.of(order2));

        Location location3 = new Location(0.0, 0.0);
        Vendor vendor3 = new Vendor(6L, Long.MIN_VALUE, location3, new ArrayList<>());
        order3 = new Order(3L, 0L, vendor3, Order.StatusEnum.REJECTED, location3);
        Mockito.when(orderRepository.findById(3L)).thenReturn(Optional.of(order3));

        Location location4 = new Location(Double.MAX_VALUE, Double.MAX_VALUE);
        Vendor vendor4 = new Vendor(1L, 11L, location4, new ArrayList<>());
        order4 = new Order(4L, 5L, vendor4, Order.StatusEnum.PREPARING, location4);
        Mockito.when(orderRepository.findById(4L)).thenReturn(Optional.of(order4));

        Location location5 = new Location(5.0, 13.0);
        Vendor vendor5 = new Vendor(5L, 10L, location5, new ArrayList<>());
        order5 = new Order(5L, 6L, vendor5, Order.StatusEnum.GIVEN_TO_COURIER, location5);
        Mockito.when(orderRepository.findById(5L)).thenReturn(Optional.of(order5));

        Location location6 = new Location(1.0, 8.0);
        Vendor vendor6 = new Vendor(11L, 8L, location6, new ArrayList<>());
        order6 = new Order(6L, 5L, vendor6, Order.StatusEnum.ON_TRANSIT, location6);
        Mockito.when(orderRepository.findById(6L)).thenReturn(Optional.of(order6));

        Location location7 = new Location(2.0, 3.0);
        Vendor vendor7 = new Vendor(10L, 6L, location7, new ArrayList<>());
        order7 = new Order(7L, 4L, vendor7, Order.StatusEnum.DELIVERED, location7);
        Mockito.when(orderRepository.findById(7L)).thenReturn(Optional.of(order7));
    }


    @Test
    public void getOrderStatus_existingOrder_returnsStatus() throws OrderNotFoundException {
        Order.StatusEnum status = orderService.getOrderStatus(1);

        assertThat(status).isEqualTo(Order.StatusEnum.PENDING);
    }

    @Test
    public void getOrderStatus_nonExistingOrder_throwsException() {
        assertThatThrownBy(() -> orderService.getOrderStatus(8))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order id not found");
    }

    @Test
    public void setOrderStatus_pendingToAccepted_validTransition() throws Exception {
        orderService.setOrderStatus(1, "Accepted");

        assertThat(order1.getStatus()).isEqualTo(Order.StatusEnum.ACCEPTED);
        verify(orderRepository).save(order1);
    }

    @Test
    public void setOrderStatus_pendingToRejected_validTransition() throws Exception {
        orderService.setOrderStatus(1, "Rejected");

        assertThat(order1.getStatus()).isEqualTo(Order.StatusEnum.REJECTED);
        verify(orderRepository).save(order1);
    }

    @Test
    public void setOrderStatus_acceptedToPreparing_validTransition() throws Exception {
        orderService.setOrderStatus(2, "Preparing");

        assertThat(order2.getStatus()).isEqualTo(Order.StatusEnum.PREPARING);
        verify(orderRepository).save(order2);
    }

    @Test
    public void setOrderStatus_preparingToGivenToCourier_validTransition() throws Exception {
        orderService.setOrderStatus(4, "Given_To_Courier");

        assertThat(order4.getStatus()).isEqualTo(Order.StatusEnum.GIVEN_TO_COURIER);
        verify(orderRepository).save(order4);
    }

    @Test
    public void setOrderStatus_givenToCourierToOnTransit_validTransition() throws Exception {
        orderService.setOrderStatus(5, "On_Transit");

        assertThat(order5.getStatus()).isEqualTo(Order.StatusEnum.ON_TRANSIT);
        verify(orderRepository).save(order5);
    }

    @Test
    public void setOrderStatus_onTransitToDelivered_validTransition() throws Exception {
        orderService.setOrderStatus(6, "Delivered");

        assertThat(order6.getStatus()).isEqualTo(Order.StatusEnum.DELIVERED);
        verify(orderRepository).save(order6);
    }

    @Test
    public void setOrderStatus_invalidTransition_throwsException() {
        assertThatThrownBy(() -> orderService.setOrderStatus(1, "Preparing"))
                .isInstanceOf(IllegalOrderStatusException.class)
                .hasMessageContaining("Error! Order status cant go from PENDING to PREPARING.");
    }


}
