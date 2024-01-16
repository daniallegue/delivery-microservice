package nl.tudelft.sem.template.example.service.strategy;

import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.model.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificOrderStrategy implements AssignOrderStrategy {
    private final DeliveryRepository deliveryRepository;

    @Autowired
    public SpecificOrderStrategy(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    /**
     * Assigns specific order to courier and saves to repository.
     *
     * @param courierId       ID of courier
     * @param orderId         ID of order
     * @param availableOrders List of available orders for the courier
     * @throws DeliveryNotFoundException No delivery with id `orderId`
     */
    @Override
    public void assignOrder(Long courierId, Long orderId, List<Long> availableOrders) throws DeliveryNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);

        if (delivery == null) {
            throw new DeliveryNotFoundException("Delivery with order id " + orderId + " was not found.");
        }

        delivery.setCourierId(courierId);
        deliveryRepository.save(delivery);
    }
}
