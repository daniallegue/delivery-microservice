package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.exception.RatingNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.Rating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AnalyticsService {
    private final DeliveryRepository deliveryRepository;

    @Autowired
    public AnalyticsService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    /**
     * Saves a rating for a specific order.
     *
     * @param rating The rating object to be saved.
     * @param orderId The unique identifier of the order to which the rating is to be associated.
     * @return The saved rating object.
     * @throws RatingNotFoundException If the rating is not found in the repository.
     * @throws OrderNotFoundException If no order is found with the given ID.
     * @throws DeliveryNotFoundException If no delivery is found for the given order ID.
     */
    public Rating saveRating(Rating rating, Long orderId) throws OrderNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with id " + orderId + " was not found.");
        }


        delivery.setRating(rating);

        return delivery.getRating();
    }

    /**
     * Retrieves the rating associated with a specific order ID.
     *
     * @param orderId The unique identifier of the order for which the rating is to be retrieved.
     * @return The rating associated with the specified order.
     * @throws RatingNotFoundException If no rating is found for the specified order ID.
     * @throws OrderNotFoundException If no order is found with the given ID.
     * @throws DeliveryNotFoundException If no delivery is found for the given order ID.
     */
    public Rating getRatingByOrderId(Long orderId) throws RatingNotFoundException, OrderNotFoundException {

        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with id " + orderId + " was not found.");
        }


        Rating rating = delivery.getRating();
        if (rating == null) {
            throw new RatingNotFoundException("Rating for order id " + orderId + " was not found.");
        }
        return rating;
    }

}
