package nl.tudelft.sem.template.example.service.strategy;

import java.util.List;
import lombok.Setter;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;

@Setter
public class AssignOrderContext {
    private AssignOrderStrategy assignOrderStrategy;

    /**
     * Assigns order to courier based on the selected strategy.
     *
     * @param courierId ID of courier
     * @param orderId ID of order
     * @param availableOrders List of available orders for the courier
     * @throws DeliveryNotFoundException No delivery with id `orderId`
     * @throws NoAvailableOrdersException No available orders for courier with id `courierId`
     */
    public void assignOrder(Long courierId, Long orderId, List<Long> availableOrders) throws DeliveryNotFoundException,
            NoAvailableOrdersException {
        assignOrderStrategy.assignOrder(courierId, orderId, availableOrders);
    }
}
