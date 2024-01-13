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
@Service
public class SpecificOrderStrategy implements AssignOrderStrategy{
    private final CourierService courierService;

    @Autowired
    public SpecificOrderStrategy(CourierService courierService) {
        this.courierService = courierService;
    }
    @Override
    public void assignOrder(Long courierId, Long orderId) throws DeliveryNotFoundException, NoAvailableOrdersException, OrderNotFoundException, CourierNotFoundException {
        Delivery delivery = courierService.getDeliveryRepository().findDeliveryByOrder_OrderId(orderId);

        if (delivery == null) {
            throw new OrderNotFoundException("Order with id " + orderId + " was not found.");
        }

        if (!courierService.doesCourierExist(courierId)) {
            throw new CourierNotFoundException("Courier with id " + courierId + " not found.");
        }

        delivery.setCourierId(courierId);
        courierService.getDeliveryRepository().save(delivery);
    }
}
