package nl.tudelft.sem.template.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Optional;

import nl.tudelft.sem.template.example.exception.IllegalOrderStatusException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.model.Order;
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

    @Test
    public void getOrderStatus_existingOrder_returnsStatus() throws OrderNotFoundException {
        Order mockOrder = new Order();
        mockOrder.setStatus(Order.StatusEnum.PENDING);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        Order.StatusEnum status = orderService.getOrderStatus(1);

        assertThat(status).isEqualTo(Order.StatusEnum.PENDING);
    }

    @Test
    public void getOrderStatus_nonExistingOrder_throwsException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderStatus(1))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order id not found");
    }

    @Test
    public void setOrderStatus_pendingToAccepted_validTransition() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setStatus(Order.StatusEnum.PENDING);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        orderService.setOrderStatus(1, "Accepted");

        assertThat(mockOrder.getStatus()).isEqualTo(Order.StatusEnum.ACCEPTED);
        verify(orderRepository).save(mockOrder);
    }

    @Test
    public void setOrderStatus_pendingToRejected_validTransition() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setStatus(Order.StatusEnum.PENDING);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        orderService.setOrderStatus(1, "Rejected");

        assertThat(mockOrder.getStatus()).isEqualTo(Order.StatusEnum.REJECTED);
        verify(orderRepository).save(mockOrder);
    }

    @Test
    public void setOrderStatus_acceptedToPreparing_validTransition() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setStatus(Order.StatusEnum.ACCEPTED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        orderService.setOrderStatus(1, "Preparing");

        assertThat(mockOrder.getStatus()).isEqualTo(Order.StatusEnum.PREPARING);
        verify(orderRepository).save(mockOrder);
    }

    @Test
    public void setOrderStatus_preparingToGivenToCourier_validTransition() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setStatus(Order.StatusEnum.PREPARING);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        orderService.setOrderStatus(1, "Given_To_Courier");

        assertThat(mockOrder.getStatus()).isEqualTo(Order.StatusEnum.GIVEN_TO_COURIER);
        verify(orderRepository).save(mockOrder);
    }

    @Test
    public void setOrderStatus_givenToCourierToOnTransit_validTransition() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setStatus(Order.StatusEnum.GIVEN_TO_COURIER);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        orderService.setOrderStatus(1, "On_Transit");

        assertThat(mockOrder.getStatus()).isEqualTo(Order.StatusEnum.ON_TRANSIT);
        verify(orderRepository).save(mockOrder);
    }

    @Test
    public void setOrderStatus_onTransitToDelivered_validTransition() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setStatus(Order.StatusEnum.ON_TRANSIT);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        orderService.setOrderStatus(1, "Delivered");

        assertThat(mockOrder.getStatus()).isEqualTo(Order.StatusEnum.DELIVERED);
        verify(orderRepository).save(mockOrder);
    }

    @Test
    public void setOrderStatus_invalidTransition_throwsException() {
        Order mockOrder = new Order();
        mockOrder.setStatus(Order.StatusEnum.PENDING);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        assertThatThrownBy(() -> orderService.setOrderStatus(1, "Preparing"))
                .isInstanceOf(IllegalOrderStatusException.class)
                .hasMessageContaining("Error! Order status cant go from PENDING to PREPARING.");
    }


}
