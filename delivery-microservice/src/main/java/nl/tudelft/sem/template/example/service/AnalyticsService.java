package nl.tudelft.sem.template.example.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
import nl.tudelft.sem.template.example.exception.IllegalOrderStatusException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.exception.RatingNotFoundException;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.Issue;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Rating;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;





@Service
public class AnalyticsService {
    private final DeliveryRepository deliveryRepository;
    private final CourierService courierService;

    private final VendorRepository vendorRepository;
    private final OrderRepository orderRepository;
    private final DeliveryService deliveryService;

    @Autowired
    public AnalyticsService(DeliveryRepository deliveryRepository, CourierService courierService,
                            VendorRepository vendorRepository, OrderRepository orderRepository, DeliveryService deliveryService) {
        this.deliveryRepository = deliveryRepository;
        this.courierService = courierService;
        this.vendorRepository = vendorRepository;
        this.orderRepository = orderRepository;
        this.deliveryService = deliveryService;
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

        deliveries = deliveries.stream()
                .filter(d -> d.getOrder() != null && d.getOrder().getStatus() == Order.StatusEnum.DELIVERED)
                .collect(Collectors.toList());

        List<LocalDate> deliveredDates = deliveries.stream()
                .map(delivery -> delivery.getTime().getDeliveredTime().toLocalDate())
                .collect(Collectors.toList());

        Map<LocalDate, Long> deliveriesPerDay = deliveredDates.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        double averageDeliveries = deliveriesPerDay.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        return (int) Math.round(averageDeliveries);
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

    /**
     * Calculates the efficiency of a specified courier.
     * @param courierId The unique identifier of the courier.
     * @return an integer that describes courier's efficiency
     * @throws CourierNotFoundException If the courier with the given ID does not exist.
     */
    public Integer getCourierEfficiency(Long courierId) throws CourierNotFoundException {

        if (!courierService.doesCourierExist(courierId)) {
            throw new CourierNotFoundException("Courier with id " + courierId + " does not exist.");
        }
        List<Delivery> deliveries = deliveryRepository.findByCourierId(courierId);
        List<Delivery> successfulDeliveries = successfulDeliveries(deliveries);

        Duration totalDuration = successfulDeliveries.stream()
                .map(delivery -> Duration.between(delivery.getTime().getPickUpTime(), delivery.getTime().getDeliveredTime()))
                .reduce(Duration::plus)
                .orElse(Duration.ZERO);
        long totalSeconds = totalDuration.toSeconds();

        double totalDistance = successfulDeliveries.stream()
                .map(delivery -> (deliveryService.calculateDistance(delivery.getOrder().getVendor().getAddress(), delivery.getOrder().getDestination())))
                .mapToDouble(Double::doubleValue)
                .sum();

        return (int) (totalDistance*100000 / totalSeconds);

    }

    /**
     * Calculates the average time for one delivery in seconds for a given vendor
     * @param vendorId The id of the vendor (required)
     * @return the time of average delivery
     * @throws VendorNotFoundException if the vendor with the given id does not exist
     */
    public Integer getVendorAverage(Long vendorId) throws VendorNotFoundException {

        if (!vendorRepository.existsById(vendorId)) {
            throw new VendorNotFoundException("Vendor with id " + vendorId + " does not exist.");
        }
        List<Order> orders = orderRepository.findOrdersByVendorId(vendorId);
        List<Delivery> deliveries = new ArrayList<>();

        for (Order order : orders) {
            Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(order.getOrderId());
            if (delivery != null) {
                deliveries.add(delivery);
            }
        }

        List<Delivery> successfulDeliveries = successfulDeliveries(deliveries);

        Duration totalDuration = successfulDeliveries.stream()
                .map(delivery -> Duration.between(delivery.getTime().getPickUpTime(), delivery.getTime().getDeliveredTime()))
                .reduce(Duration::plus)
                .orElse(Duration.ZERO);
        long totalSeconds = totalDuration.toSeconds();

        return (int) totalSeconds / successfulDeliveries.size();

    }

    /**
     * getting successful deliveries.
     * @param deliveries given deliveries
     * @return returns deliveries that have status delivered
     */
    public List<Delivery> successfulDeliveries(List<Delivery> deliveries) {
        return deliveries.stream()
                .filter(delivery -> delivery.getOrder().getStatus() == Order.StatusEnum.DELIVERED)
                .collect(Collectors.toList());
    }
}
