package nl.tudelft.sem.template.example.service.strategy;

import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.model.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RandomOrderStrategy implements AssignOrderStrategy{

    private final DeliveryRepository deliveryRepository;

    @Autowired
    public RandomOrderStrategy(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }
    @Override
    public void assignOrder(Long courierId, Long orderId, List<Long> availableOrders) throws DeliveryNotFoundException, NoAvailableOrdersException, OrderNotFoundException, CourierNotFoundException {
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
