package nl.tudelft.sem.template.example.service.strategy;

import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.service.CourierService;
import nl.tudelft.sem.template.model.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class SpecificOrderStrategy implements AssignOrderStrategy{
    private final DeliveryRepository deliveryRepository;

    @Autowired
    public SpecificOrderStrategy(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }
    @Override
    public void assignOrder(Long courierId, Long orderId, List<Long> availableOrders) throws DeliveryNotFoundException, NoAvailableOrdersException, OrderNotFoundException, CourierNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);

        if (delivery == null) {
            throw new OrderNotFoundException("Order with id " + orderId + " was not found.");
        }

        delivery.setCourierId(courierId);
        deliveryRepository.save(delivery);
    }
}
