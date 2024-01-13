package nl.tudelft.sem.template.example.service.strategy;

import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;

public class AssignOrderContext {
    private AssignOrderStrategy assignOrderStrategy;

    public void setAssignOrderStrategy(AssignOrderStrategy assignOrderStrategy) {
        this.assignOrderStrategy = assignOrderStrategy;
    }

    public void assignOrder(Long courierId, Long orderId) throws DeliveryNotFoundException, NoAvailableOrdersException, OrderNotFoundException, CourierNotFoundException {
        assignOrderStrategy.assignOrder(courierId, orderId);
    }
}
