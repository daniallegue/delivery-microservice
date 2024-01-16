package nl.tudelft.sem.template.example.service.strategy;

import java.util.List;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;

public interface AssignOrderStrategy {

    /**
     * Assigns order to courier based on the selected strategy.
     *
     * @param courierId ID of courier
     * @param orderId ID of order
     * @param availableOrders List of available orders for the courier
     * @throws DeliveryNotFoundException No delivery with id `orderId`
     * @throws NoAvailableOrdersException No available orders for courier with id `courierId`
     */
    void assignOrder(Long courierId, Long orderId, List<Long> availableOrders) throws DeliveryNotFoundException,
            NoAvailableOrdersException;
}
