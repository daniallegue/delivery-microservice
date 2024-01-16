package nl.tudelft.sem.template.example.service.strategy;


import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.model.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class RandomOrderStrategy implements AssignOrderStrategy {

    private final DeliveryRepository deliveryRepository;

    @Autowired
    public RandomOrderStrategy(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    /**
     * Assigns a random order to the courier and saves it in the repository.
     *
     * @param courierId ID of courier
     * @param orderId ID of order
     * @param availableOrders List of available orders for the courier
     * @throws DeliveryNotFoundException No delivery with id `orderId`
     * @throws NoAvailableOrdersException No available orders for courier with id `courierId`
     */
    @Override
    public void assignOrder(Long courierId, Long orderId, List<Long> availableOrders) throws DeliveryNotFoundException,
            NoAvailableOrdersException {
        if (availableOrders.isEmpty()) {
            throw new NoAvailableOrdersException("No orders available for courier with id: " + courierId);
        }

        orderId = availableOrders.get(0);
        Delivery deliveryToUpdate = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        Optional<Delivery> deliveryOptional = deliveryRepository.findById(deliveryToUpdate.getId());

        if (deliveryOptional.isEmpty()) {
            throw new DeliveryNotFoundException("Delivery id not found");
        }

        Delivery delivery = deliveryOptional.get();
        delivery.setCourierId(courierId);
        deliveryRepository.save(delivery);
    }

}
