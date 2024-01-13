package nl.tudelft.sem.template.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Getter;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;
import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.example.service.strategy.AssignOrderContext;
import nl.tudelft.sem.template.example.service.strategy.RandomOrderStrategy;
import nl.tudelft.sem.template.example.service.strategy.SpecificOrderStrategy;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class CourierService {
    @Getter
    DeliveryRepository deliveryRepository;
    VendorRepository vendorRepository;
    private final List<Long> courierList = new ArrayList<>();
    AssignOrderContext assignOrderContext = new AssignOrderContext();

    /**
     * Constructor for handling dependency injection.
     *
     * @param deliveryRepository JPA repository holding the deliveries
     * @param vendorRepository JPA repository holding the vendors
     */
    @Autowired
    public CourierService(DeliveryRepository deliveryRepository, VendorRepository vendorRepository) {
        this.deliveryRepository = deliveryRepository;
        this.vendorRepository = vendorRepository;
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
     * Checks whether a courier exists
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
            throws OrderNotFoundException, CourierNotFoundException, DeliveryNotFoundException, NoAvailableOrdersException {
        assignOrderContext.setAssignOrderStrategy(new SpecificOrderStrategy(this));
        assignOrderContext.assignOrder(courierId, orderId);
    }


    /**
     * Assigns a courier to a random available order.
     *
     * @param courierId Unique identifier of the courier (required)
     */
    public void assignCourierToRandomOrder(Long courierId) throws DeliveryNotFoundException, NoAvailableOrdersException, OrderNotFoundException, CourierNotFoundException {
        assignOrderContext.setAssignOrderStrategy(new RandomOrderStrategy(this));
        assignOrderContext.assignOrder(courierId, null);
    }

}
