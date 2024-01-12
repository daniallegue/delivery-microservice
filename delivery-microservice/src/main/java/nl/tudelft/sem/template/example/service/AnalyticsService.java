package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.exception.*;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Rating;
import nl.tudelft.sem.template.model.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class AnalyticsService {
    private final DeliveryRepository deliveryRepository;
    private final CourierService courierService;

    @Autowired
    public AnalyticsService(DeliveryRepository deliveryRepository, CourierService courierService) {
        this.deliveryRepository = deliveryRepository;
        this.courierService = courierService;
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
    public Rating saveRating(Rating rating, Long orderId) throws OrderNotFoundException, IllegalOrderStatusException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with id " + orderId + " was not found.");
        }

        if (!delivery.getOrder().getStatus().equals(Order.StatusEnum.DELIVERED)) {
            throw new IllegalOrderStatusException("Only delivered orders can be rated.");
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

    /**
     * Calculates the average number of deliveries per day for a specified courier.
     *
     * @param courierId The unique identifier of the courier.
     * @return An integer representing the average number of deliveries per day.
     * @throws CourierNotFoundException If the courier with the given ID does not exist.
     */
    public int getDeliveriesPerDay(Long courierId) throws CourierNotFoundException {
        if (!courierService.doesCourierExist(courierId)) {
            throw new CourierNotFoundException("Courier with id " + courierId + " does not exist.");
        }
        List<Delivery> deliveries = deliveryRepository.findByCourierId(courierId);

        int averageDeliveries = (int) Math.round(deliveries.size()/7.0);
        return averageDeliveries;
    }

    /**
     * Calculates the average number of deliveries per day for a specified courier.
     *
     * @param courierId The unique identifier of the courier.
     * @return An integer representing the average number of deliveries per day.
     * @throws CourierNotFoundException If the courier with the given ID does not exist.
     */
    public int getSuccessfulDeliveries(Long courierId) throws CourierNotFoundException {
        if (!courierService.doesCourierExist(courierId)) {
            throw new CourierNotFoundException("Courier with id " + courierId + " does not exist.");
        }
        List<Delivery> deliveries = deliveryRepository.findByCourierId(courierId);

        int successfulDeliveries = (int) deliveries.stream()
                .filter(delivery -> delivery.getOrder().getStatus() == Order.StatusEnum.DELIVERED)
                .count();
        return successfulDeliveries;
    }

    /**
     * Retrieves a list of issue descriptions encountered by a specific courier during deliveries.
     *
     * @param courierId The unique identifier of the courier.
     * @return A list of strings, each describing an issue encountered by the courier.
     * @throws CourierNotFoundException If the courier with the given ID does not exist.
     */
    public List<String> getCourierIssues(Long courierId) throws CourierNotFoundException {

        if (!courierService.doesCourierExist(courierId)) {
            throw new CourierNotFoundException("Courier with id " + courierId + " does not exist.");
        }
        List<Delivery> deliveries = deliveryRepository.findByCourierId(courierId);

        List<String> issues = deliveries.stream()
                .map(Delivery::getIssue)
                .filter(Objects::nonNull)
                .map(Issue::getDescription)
                .collect(Collectors.toList());
        return issues;
    }
}
