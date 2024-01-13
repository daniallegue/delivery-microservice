package nl.tudelft.sem.template.example.service.strategy;

import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.service.CourierService;

import java.util.List;

public interface AssignOrderStrategy {
    void assignOrder(Long courierId, Long orderId, List<Long> availableOrders) throws DeliveryNotFoundException, NoAvailableOrdersException, OrderNotFoundException, CourierNotFoundException;
}
