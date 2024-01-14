package nl.tudelft.sem.template.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.external.UsersMicroservice;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Vendor;
import org.h2.engine.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;



@Service
public class CourierService {
    DeliveryRepository deliveryRepository;
    VendorRepository vendorRepository;

    UsersMicroservice usersMicroservice;
    private List<Long> courierList = new ArrayList<>();

    /**
     * Constructor for handling dependency injection.
     *
     * @param deliveryRepository JPA repository holding the deliveries
     * @param vendorRepository JPA repository holding the vendors
     * @param usersMicroservice External communication to Users microservice
     */
    @Autowired
    public CourierService(DeliveryRepository deliveryRepository, VendorRepository vendorRepository,
                          UsersMicroservice usersMicroservice) {
        this.deliveryRepository = deliveryRepository;
        this.vendorRepository = vendorRepository;
        this.usersMicroservice = usersMicroservice;
    }

    /**
     * Gets ids of all available orders.
     *
     * @param courierId Unique identifier of the courier (required)
     * @return returns the list of the ids of available orders
     */
    public List<Long> getAvailableOrderIds(Long courierId) {
        List<Order> filteredOrders = deliveryRepository.findAll()
                .stream()
                .filter(delivery -> delivery.getCourierId() == null)
                .map(Delivery::getOrder)
                .filter(order -> order.getStatus() == Order.StatusEnum.ACCEPTED)
                .collect(Collectors.toList());
        try {
            Long vendorId = checkIfCourierIsAssignedToVendor(courierId);
            filteredOrders = filteredOrders
                    .stream()
                    .filter(order -> vendorId.equals(order.getVendor().getId()))
                    .collect(Collectors.toList());
        } catch (CourierNotFoundException e) {
            List<Long> vendorsWithCouriers = getVendorsThatHaveTheirOwnCouriers();
            filteredOrders = filteredOrders
                    .stream()
                    .filter(order -> !vendorsWithCouriers.contains(order.getVendor().getId()))
                    .collect(Collectors.toList());
        }
        return  filteredOrders
                .stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());
    }

    /**
     * Checks if courier is assigned to vendor.
     *
     * @param courierId Unique identifier of the courier (required)
     * @return returns vendor id if courier is assigned to a vendor and -1 otherwise
     */
    public Long checkIfCourierIsAssignedToVendor(long courierId) throws CourierNotFoundException {
        List<Vendor> allVendors = vendorRepository.findAll();
        for (Vendor vendor : allVendors) {
            for (Long id : vendor.getCouriers()) {
                if (id == courierId) {
                    return vendor.getId();
                }
            }
        }
        throw new CourierNotFoundException("Courier does not belong to vendor");
    }

    /**
     * Gets ids of the vendors that have their own couriers.
     *
     * @return returns the list of vendor ids
     */
    public List<Long> getVendorsThatHaveTheirOwnCouriers() {
        return vendorRepository.findAll()
                .stream()
                .filter(vendor -> !vendor.getCouriers().isEmpty())
                .map(Vendor::getId)
                .collect(Collectors.toList());
    }

    /**
     * Assigns a courier to a random available order.
     *
     * @param courierId Unique identifier of the courier (required)
     */
    public void assignCourierToRandomOrder(Long courierId) throws DeliveryNotFoundException, NoAvailableOrdersException {
        List<Long> availableOrders = getAvailableOrderIds(courierId);

        if (availableOrders.size() <= 0) {
            throw new NoAvailableOrdersException("No orders available for courier with id: " + courierId);
        }

        //Assign first order available for now, in the future this
        // will eventually change to a more effective method of choosing random orders
        Long orderId = availableOrders.get(0);

        //Find delivery with required orderId
        Delivery deliveryToUpdate = deliveryRepository.findDeliveryByOrder_OrderId(orderId);


        Optional<Delivery> deliveryOptional = deliveryRepository.findById(deliveryToUpdate.getId());
        if (deliveryOptional.isEmpty()) {
            throw new DeliveryNotFoundException("Delivery id not found");
        }

        Delivery delivery = deliveryOptional.get();
        delivery.setCourierId(courierId);
        deliveryRepository.save(delivery);

    }

    /** Adds a courier with a specific ID to our database.
     *
     * @param courierId Unique identifier of the courier (required)
     */
    public void addCourier(Long courierId) {
        // Check if the courier ID is already known, if not, add it to the list
        if (!courierList.contains(courierId)) {
            courierList.add(courierId);
        }
    }

    /**
     * Checks whether a courier exists.
     *
     * @param courierId Unique identifier of the courier (required)
     * @return returns true if a courier with the specified ID exists in our database
     */
    public boolean doesCourierExist(Long courierId) {
        return courierList.contains(courierId);
    }

    /**
     * Assigns a specific order to a courier.
     *
     * @param courierId Unique identifier of the courier (required)
     * @param orderId Unique identifier of the order to be assigned (required)
     * @throws OrderNotFoundException if the order is not found
     * @throws CourierNotFoundException if the courier is not found
     */
    public void assignCourierToSpecificOrder(Long courierId, Long orderId)
            throws OrderNotFoundException, CourierNotFoundException {

        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (delivery == null) {
            throw new OrderNotFoundException("Order with id " + orderId + " was not found.");
        }

        if (!this.doesCourierExist(courierId)) {
            throw new CourierNotFoundException("Courier with id " + courierId + " not found.");
        }

        delivery.setCourierId(courierId);
        deliveryRepository.save(delivery);
    }

    /**
     * Retrieves all the couriers from UsersMicroservice.
     * This function runs periodically to retrieve continuously all the couriers
     */
    @Scheduled(fixedDelay = 5000)
    public void populateAllCouriers() {
        List<Long> couriers = usersMicroservice.getCourierIds().get();
        if (couriers.size() > 0) {
            for (Long courier : couriers) {
                if (!doesCourierExist(courier)) {
                    addCourier(courier);
                }
            }
        }
    }

}
