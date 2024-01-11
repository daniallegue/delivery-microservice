package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.OrderAlreadyExistsException;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.DeliveryPostRequest;
import nl.tudelft.sem.template.model.Issue;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Order;
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
     * Add an issue to a Delivery, for cases such as bad traffic conditions.
     *
     * @param orderId The id of the order corresponding to the Delivery
     * @param issue   The issue to be added to the Delivery
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
     */
    public Long getDefaultDeliveryZone() {
        return configurationProperties.getDefaultDeliveryZone();
    }


}
