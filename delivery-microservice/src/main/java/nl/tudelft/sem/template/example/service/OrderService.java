package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.exception.IllegalOrderStatusException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static nl.tudelft.sem.template.model.Order.StatusEnum;

@Service
public class OrderService {

    OrderRepository orderRepository;

    /**
     * Simple constructor handling dependency injection.
     *
     * @param orderRepository JPA repository holding the orders
     */
    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Returns status of a given error.
     *
     * @param orderId Unique identifier of the order (required)
     * @return StatusEnum containing the status of the order
     * @throws OrderNotFoundException if the order was not found
     */
    public Order.StatusEnum getOrderStatus(Integer orderId) throws OrderNotFoundException {
        Optional<Order> orderOptional = orderRepository.findById(Long.valueOf(orderId));
        if (orderOptional.isEmpty()) {
            throw new OrderNotFoundException("Order id not found");
        }
        return orderOptional.get().getStatus();
    }

    /**
     * Checks whether the new status follows a desired flow, respecting
     * certain rules, and if so changes and updates the order in the repository.
     * If not, it throws an exception.
     *
     * @param orderId Unique identifier of the order (required)
     * @param orderStatusString String format of the new status
     * @throws IllegalOrderStatusException if status doesn't respect the flow
     *     or status string is not available
     * @throws OrderNotFoundException if order was not found
     */
    public void setOrderStatus(Integer orderId, String orderStatusString)
            throws IllegalOrderStatusException, OrderNotFoundException {
        Optional<Order> orderOptional = orderRepository.findById(Long.valueOf(orderId));
        if (orderOptional.isEmpty()) {
            throw new OrderNotFoundException("Order id not found");
        }
        Order order = orderOptional.get();
        StatusEnum newStatus = StatusEnum.fromValue(orderStatusString);
        assertStatusFlowIsCorrect(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    /**
     * Checks, given the current status of the order, if the order can
     * or can't reach the second status, following a logical set of rules.
     * A status can go from:
     * Pending -> Accepted
     * Pending -> Rejected
     * Accepted -> Preparing
     *
     * @param oldStatus Current status of an order
     * @param newStatus New status of an order
     * @throws IllegalOrderStatusException if status doesn't respect the flow
     */
    private void assertStatusFlowIsCorrect(StatusEnum oldStatus, StatusEnum newStatus)
            throws IllegalOrderStatusException {
        switch (oldStatus) {
            case PENDING -> {
                if (newStatus != StatusEnum.ACCEPTED && newStatus != StatusEnum.REJECTED) {
                    throw new IllegalOrderStatusException("Error! Order status cant go from PENDING to "
                            + newStatus.toString().toUpperCase() + ".");
                }
            }
            case REJECTED -> {
                throw new IllegalOrderStatusException("Error! Order status can't change after being REJECTED");
            }
            case ACCEPTED -> {
                if (newStatus != StatusEnum.PREPARING) {
                    throw new IllegalOrderStatusException("Error! Order status can't change from ACCEPTED to "
                            + newStatus.toString().toUpperCase() + ".");
                }
            }
            default -> { }
            //TODO(@rgherasa): Finish rest of the status flows (i.e. GivenToCourier, InTransit, Delivered).
        }
    }
}
