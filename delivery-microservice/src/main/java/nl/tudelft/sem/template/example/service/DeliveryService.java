package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.exception.OrderAlreadyExistsException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class DeliveryService {
    DeliveryRepository deliveryRepository;
    OrderRepository orderRepository;
    VendorRepository vendorRepository;
    VendorService vendorService;

    /**
     * Constructor for the Delivery Service that allow dependency injection.
     *
     * @param deliveryRepository The repository where Delivery objects are saved in.
     * @param orderRepository The repository where Order objects are saved in.
     * @param vendorRepository The repository where Vendor objects are saved in.
     * @param vendorService The service that handles the vendor interaction logic.
     */
    @Autowired
    DeliveryService(DeliveryRepository deliveryRepository, OrderRepository orderRepository,
                    VendorRepository vendorRepository, VendorService vendorService) {
        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
        this.vendorRepository = vendorRepository;
        this.vendorService = vendorService;
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

    public OffsetDateTime getReadyTime(Long orderId) throws OrderNotFoundException {
        // Fetch delivery using the repository
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with ID: " + orderId + " not found.");
        }

        // Extract and return the ready time
        Time time = delivery.getTime();
        return time != null ? time.getReadyTime() : null;
    }

    public void updateReadyTime(Long orderId, OffsetDateTime newReadyTime) throws OrderNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with ID: " + orderId + " not found.");
        }

        Time time = delivery.getTime();
        if (time == null) {
            time = new Time();
            delivery.setTime(time);
        }
        time.setReadyTime(newReadyTime);
        deliveryRepository.save(delivery);
    }

    public OffsetDateTime getPickupTime(Long orderId) throws OrderNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with ID: " + orderId + " not found.");
        }

        Time time = delivery.getTime();
        return time != null ? time.getPickUpTime() : null;
    }

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

    public OffsetDateTime getDeliveredTime(Long orderId) throws OrderNotFoundException {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with ID: " + orderId + " not found.");
        }

        Time time = delivery.getTime();
        return time != null ? time.getDeliveredTime() : null;
    }

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

    public OffsetDateTime getEta(Long orderId) throws OrderNotFoundException {
        // Fetch the delivery using the repository
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with ID: " + orderId + " not found.");
        }

        OffsetDateTime eta = calculateEstimatedTime(delivery.getOrder().getVendor().getAddress(), delivery.getOrder().getDestination());

        return eta;
    }

    private OffsetDateTime calculateEstimatedTime(Location vendorLocation, Location destination) {
        // TODO: Implement specific computation of the estimated time of arrival.
        long estimatedTravelDurationInMinutes = 30; // Example fixed duration
        return OffsetDateTime.now().plusMinutes(estimatedTravelDurationInMinutes);
    }





}
