package nl.tudelft.sem.template.example.repository;

import nl.tudelft.sem.template.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    /**
     * Finds a delivery based on the orderId of the order within the Delivery.
     *
     * @param orderId - The order id of from the Delivery.
     * @return A delivery.
     */
    Delivery findDeliveryByOrder_OrderId(Long orderId);
}
