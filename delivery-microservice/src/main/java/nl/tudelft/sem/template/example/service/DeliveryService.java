package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.model.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeliveryService {

    DeliveryRepository deliveryRepository;

    /**
     * Simple constructor handling dependency injection.
     *
     * @param deliveryRepository JPA repository holding the deliveries
     */
    @Autowired
    public DeliveryService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    /**
     * Returns status of a given error.
     *
     *
     * @return List of Delivery containing all the deliveries currently in the repository
     */
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    /**
     * Checks whether the new status follows a desired flow, respecting
     * certain rules, and if so changes and updates the order in the repository.
     * If not, it throws an exception.
     *
     * @param deliveryId Unique identifier of the delivery (required)
     * @param courierId Unique identifier of the courier being assigned (required)
     * @throws DeliveryNotFoundException if delivery was not found
     */
    public void setCourier(Integer deliveryId, Long courierId)
            throws DeliveryNotFoundException {
        Optional<Delivery> deliveryOptional = deliveryRepository.findById(Long.valueOf(deliveryId));
        if (deliveryOptional.isEmpty()) {
            throw new DeliveryNotFoundException("Delivery id not found");
        }
        //Handle courier not found
        Delivery delivery = deliveryOptional.get();
        delivery.setCourierId(courierId);
        deliveryRepository.save(delivery);
    }


}
