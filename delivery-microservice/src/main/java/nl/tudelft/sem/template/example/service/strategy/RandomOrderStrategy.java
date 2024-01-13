package nl.tudelft.sem.template.example.service.strategy;

import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.service.CourierService;
import nl.tudelft.sem.template.model.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RandomOrderStrategy implements AssignOrderStrategy{

    private final CourierService courierService;

    @Autowired
    public RandomOrderStrategy(CourierService courierService) {
        this.courierService = courierService;
    }
    @Override
    public void assignOrder(Long courierId, Long orderId) throws DeliveryNotFoundException, NoAvailableOrdersException, OrderNotFoundException, CourierNotFoundException {
        List<Long> availableOrders = courierService.getAvailableOrderIds(courierId);

        if (availableOrders.isEmpty()) {
            throw new NoAvailableOrdersException("No orders available for courier with id: " + courierId);
        }

        orderId = availableOrders.get(0);
        Delivery deliveryToUpdate = courierService.getDeliveryRepository().findDeliveryByOrder_OrderId(orderId);
        Optional<Delivery> deliveryOptional = courierService.getDeliveryRepository().findById(deliveryToUpdate.getId());

        if (deliveryOptional.isEmpty()) {
            throw new DeliveryNotFoundException("Delivery id not found");
        }

        Delivery delivery = deliveryOptional.get();
        delivery.setCourierId(courierId);
        courierService.getDeliveryRepository().save(delivery);
    }
}
