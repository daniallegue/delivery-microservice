package nl.tudelft.sem.template.example.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.OrderAlreadyExistsException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.DeliveryPostRequest;
import nl.tudelft.sem.template.model.Issue;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Time;
import nl.tudelft.sem.template.model.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeliveryService {
    DeliveryRepository deliveryRepository;
    OrderRepository orderRepository;
    VendorRepository vendorRepository;
    VendorService vendorService;

    ConfigurationProperties configurationProperties;

    /**
     * Constructor for the Delivery Service that allow dependency injection.
     *
     * @param deliveryRepository The repository where Delivery objects are saved in.
     * @param orderRepository The repository where Order objects are saved in.
     * @param vendorRepository The repository where Vendor objects are saved in.
     * @param vendorService The service that handles the vendor interaction logic.
     * @param configurationProperties The configuration properties of the whole microservice
     */
    @Autowired
    DeliveryService(DeliveryRepository deliveryRepository, OrderRepository orderRepository,
                    VendorRepository vendorRepository, VendorService vendorService,
                    ConfigurationProperties configurationProperties) {
        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
        this.vendorRepository = vendorRepository;
        this.vendorService = vendorService;
        this.configurationProperties = configurationProperties;
    }

    /**
     * Creates a delivery with the body from the request.
     *
     * @param deliveryPostRequest The basic elements of a delivery object.
     * @return The delivery that was created.
     * @throws VendorNotFoundException If the retrieval of vendor (i.e. problem in the communication of the microservices)
     */
    public Delivery createDelivery(DeliveryPostRequest deliveryPostRequest) throws Exception {
        Vendor vendor = vendorService.findVendorOrCreate(Long.valueOf(deliveryPostRequest.getVendorId()));

        if (vendor == null) {
            throw new VendorNotFoundException("Vendor was not found");
        }

        if (orderRepository.existsById(Long.valueOf(deliveryPostRequest.getOrderId()))) {
            throw new OrderAlreadyExistsException("The order id already exists");
        }

        Location destination = deliveryPostRequest.getDestination();
        Order order = new Order(Long.valueOf(deliveryPostRequest.getOrderId()),
                Long.valueOf(deliveryPostRequest.getCustomerId()),
                vendor,
                Order.StatusEnum.PENDING,
                destination
        );

        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery = deliveryRepository.save(delivery);
        return delivery;
    }

    /**
     * Retrieves the ready time for a delivery.
     *
     * @param orderId The id of the order corresponding to the Delivery
     * @throws OrderNotFoundException If the retrieval of order (i.e. problem in the communication of the microservices)
     *
     */
    public OffsetDateTime getReadyTime(Long orderId) throws OrderNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with ID: " + orderId + " not found.");
        }

        // Extract and return the ready time
        Time time = delivery.getTime();
        return time != null ? time.getReadyTime() : null;
    }

    /**
     * Updates the ready time of an order.
     *
     * @param  orderId The id of the order corresponding to the Delivery
     * @param newReadyTime the new ready time
     * @throws OrderNotFoundException when the order was not present in the repository
     */
    public void updateReadyTime(Long orderId, OffsetDateTime newReadyTime) throws OrderNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with ID: " + orderId + " not found.");
        }

        Time time = delivery.getTime() == null ? new Time() : delivery.getTime();
        delivery.setTime(time);
        time.setReadyTime(newReadyTime);
        deliveryRepository.save(delivery);
    }

    /**
     * Retrieves the pickup time for an order.
     *
     * @param orderId The id of the order corresponding to the Delivery
     * @return pickup time for the delivery
     * @throws OrderNotFoundException when the order was not present in the repository
     *
     */
    public OffsetDateTime getPickupTime(Long orderId) throws OrderNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with ID: " + orderId + " not found.");
        }

        Time time = delivery.getTime();
        return time != null ? time.getPickUpTime() : null;
    }

    /**
     * Updates the pickup time of an order.
     *
     * @param orderId The id of the order corresponding to the Delivery
     * @param newPickUpTime the new pickup time
     * @throws OrderNotFoundException when the order was not present in the repository
     */
    public void updatePickupTime(Long orderId, OffsetDateTime newPickUpTime) throws OrderNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with ID: " + orderId + " not found.");
        }

        Time time = delivery.getTime();
        if (time == null) {
            time = new Time();
            delivery.setTime(time);
        }
        time.setPickUpTime(newPickUpTime);
        deliveryRepository.save(delivery);
    }

    /**
     * Retrieves the time taken for an order to be delivered.
     *
     * @param orderId The id of the order corresponding to the Delivery
     * @return time to deliver an order
     * @throws OrderNotFoundException when the order was not present in the repository
     */
    public OffsetDateTime getDeliveredTime(Long orderId) throws OrderNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with ID: " + orderId + " not found.");
        }

        Time time = delivery.getTime();
        return time != null ? time.getDeliveredTime() : null;
    }

    /**
     * Updates the time for an order to be delivered.
     *
     * @param orderId The id of the order corresponding to the Delivery
     * @param newDeliveredTime new time to deliver an order
     * @throws OrderNotFoundException when the order was not present in the repository
     */
    public void updateDeliveredTime(Long orderId, OffsetDateTime newDeliveredTime) throws OrderNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with ID: " + orderId + " not found.");
        }

        Time time = delivery.getTime();
        if (time == null) {
            time = new Time();
            delivery.setTime(time);
        }
        time.setDeliveredTime(newDeliveredTime);
        deliveryRepository.save(delivery);
    }

    /**
     * Retrieves the ETA (Estimated Time of Arrival).
     *
     * @param orderId The id of the order corresponding to the Delivery
     * @return ETA
     * @throws OrderNotFoundException when the order was not present in the repository
     */
    public OffsetDateTime getEta(Long orderId) throws OrderNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with ID: " + orderId + " not found.");
        }

        OffsetDateTime eta = calculateEstimatedTime(delivery.getOrder().getVendor().getAddress(),
                delivery.getOrder().getDestination());
        return eta;
    }

    /**
     * Computes ETA.
     *
     * @param vendorLocation Location of the vendor
     * @param destination Destination of delivery
     * @return ETA
     */
    private OffsetDateTime calculateEstimatedTime(Location vendorLocation, Location destination) {
        // TODO: Implement specific computation of the estimated time of arrival.
        long estimatedTravelDurationInMinutes = 30;
        return OffsetDateTime.now().plusMinutes(estimatedTravelDurationInMinutes);
    }

    /**
     * Add an issue to a Delivery, for cases such as bad traffic conditions.
     *
     * @param orderId The id of the order corresponding to the Delivery
     *
     * @param issue The issue to be added to the Delivery
     * @throws DeliveryNotFoundException when the delivery was not present in the repository
     */
    public void addIssueToDelivery(Integer orderId, Issue issue) throws DeliveryNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(Long.valueOf(orderId));
        if (delivery == null) {
            throw new DeliveryNotFoundException("Delivery with order id " + orderId + " was not found");
        }
        delivery.setIssue(issue);
        deliveryRepository.save(delivery);
    }

    /**
     * Retrieves the issue related to a Delivery, if one is found.
     *
     * @param orderId The id of the order within the delivery.
     * @return The issue of a delivery.
     * @throws DeliveryNotFoundException If the delivery with that order was not found.
     */
    public Issue retrieveIssueOfDelivery(Integer orderId) throws DeliveryNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(Long.valueOf(orderId));
        if (delivery == null) {
            throw new DeliveryNotFoundException("Delivery with order id " + orderId + " was not found");
        }
        return delivery.getIssue();
    }

    /**
     * Updates the default delivery zone from Configuration Properties.
     *
     * @param newDeliveryZone the value to set the new delivery zone to
     */
    public void updateDefaultDeliveryZone(Integer newDeliveryZone) {
        configurationProperties.setDefaultDeliveryZone(newDeliveryZone);
    }

    /**
     * Retrieves the default delivery zone from Configuration Properties.
     *
     * @return default delivery zone
     */
    public Long getDefaultDeliveryZone() {
        return configurationProperties.getDefaultDeliveryZone();
    }

    /**
     * Calculates the distance between 2 locations.
     *
     * @param start The start location(vendor).
     * @param end The end location(destination).
     * @return The distance between the 2 points.
     */
    private double calculateDistance(Location start, Location end) {
        double latDifference = end.getLatitude() - start.getLatitude();
        double lonDifference = end.getLongitude() - start.getLongitude();
        return Math.sqrt(latDifference * latDifference + lonDifference * lonDifference);
    }

    private static final double COURIER_SPEED = 1; // meter per second

    /**
     * Estimates the current position.
     *
     * @param start The start location.
     * @param end The end location.
     * @param pickupTime The time the order was picked up.
     * @param currentTime The current time.
     * @return Returns the current location of the order/courier.
     */
    public Location estimatePosition(Location start, Location end, OffsetDateTime pickupTime, OffsetDateTime currentTime) {
        if (pickupTime == null || pickupTime.isAfter(currentTime)) {
            return start; // Courier hasn't started the delivery yet
        }

        double distance = calculateDistance(start, end);
        long elapsedTimeInSeconds = Duration.between(pickupTime, currentTime).getSeconds();

        double totalTravelTimeInSeconds = distance / COURIER_SPEED;

        elapsedTimeInSeconds = Math.min(elapsedTimeInSeconds, (long) totalTravelTimeInSeconds);

        double journeyFraction = elapsedTimeInSeconds / totalTravelTimeInSeconds;

        double estimatedLatitude = linearInterpolation(start.getLatitude(), end.getLatitude(), journeyFraction);
        double estimatedLongitude = linearInterpolation(start.getLongitude(), end.getLongitude(), journeyFraction);

        return new Location(estimatedLatitude, estimatedLongitude);
    }

    /**
     * Interpolates 2 locations.
     *
     * @param start The start location
     * @param end The end location
     * @param fraction The fraction at which to interpolate between the start and the end values.
     * @return The interpolated value between start and end based on the given fraction.
     */
    public double linearInterpolation(double start, double end, double fraction) {
        return start + (end - start) * fraction;
    }

    /**
     * Calculates current location.
     *
     * @param deliveryId The unique ID of the delivery.
     * @return The current live location of the delivery.
     * @throws OrderNotFoundException OrderNotFoundException If the delivery with the specified ID is not found.
     */
    public Location calculateLiveLocation(Long deliveryId) throws OrderNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(deliveryId);
        if (delivery == null) {
            throw new OrderNotFoundException("Delivery with ID: " + deliveryId + " not found.");
        }

        Order order = delivery.getOrder();
        Location vendorLocation = order.getVendor().getAddress();
        Location destination = order.getDestination();
        OffsetDateTime pickupTime = delivery.getTime().getPickUpTime();
        OffsetDateTime currentTime = OffsetDateTime.now();

        return estimatePosition(vendorLocation, destination, pickupTime, currentTime);
    }

    /**
     * Retrieves the delivery for an order.
     *
     * @param orderId The unique ID of the order.
     * @return The delivery ID associated with the given order ID.
     * @throws OrderNotFoundException If the order with the specified ID is not found.
     */
    public Long getDeliveryIdByOrderId(Long orderId) throws OrderNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery != null) {
            return delivery.getId();
        } else {
            throw new OrderNotFoundException("Order with ID: " + orderId + " not found.");
        }
    }

    /**
     * Returns the courier assigned to an order with id: orderId.
     *
     * @param orderId The id of the order within the delivery.
     * @return id of the courier assigned to the order
     */
    public Long getCourierFromOrder(Integer orderId) throws OrderNotFoundException, CourierNotFoundException {
        if (!orderRepository.existsById((long) orderId)) {
            throw new OrderNotFoundException("Order not found");
        }
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId((long) orderId);
        if (delivery.getCourierId() == null) {
            throw new CourierNotFoundException("No courier assigned");
        }
        return delivery.getCourierId();
    }
}
