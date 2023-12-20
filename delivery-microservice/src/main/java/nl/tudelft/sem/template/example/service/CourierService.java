package nl.tudelft.sem.template.example.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class CourierService {
    DeliveryRepository deliveryRepository;
    VendorRepository vendorRepository;

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
        List<Delivery> allDeliveries = deliveryRepository.findAll()
                .stream()
                .filter(delivery -> delivery.getCourierId() == null)
                .collect(Collectors.toList());

        //get all orders that have status 'accepted'
        List<Order> acceptedOrders =  allDeliveries
                .stream()
                .map(Delivery::getOrder)
                .filter(order -> order.getStatus() == Order.StatusEnum.ACCEPTED)
                .collect(Collectors.toList());

        //check if courier is assigned to a vendor and filter orders if it is assigned
        Long vendorId = checkIfCourierIsAssignedToVendor(courierId);
        if (vendorId != -1) {
            acceptedOrders = acceptedOrders
                    .stream()
                    .filter(order -> vendorId.equals(order.getVendor().getId()))
                    .collect(Collectors.toList());
        } else {

            //filter orders that belong to vendors that have their own couriers
            List<Long> vendorsWithCouriers = getVendorsThatHaveTheirOwnCouriers();
            acceptedOrders = acceptedOrders
                    .stream()
                    .filter(order -> !vendorsWithCouriers.contains(order.getVendor().getId()))
                    .collect(Collectors.toList());
        }

        return  acceptedOrders
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
    public Long checkIfCourierIsAssignedToVendor(long courierId) {

        List<Vendor> allVendors = vendorRepository.findAll();
        for (Vendor vendor : allVendors) {
            for (Long id : vendor.getCouriers()) {
                if (id == courierId) {
                    return vendor.getId();
                }
            }
        }
        return (long) -1;

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

        if(availableOrders.size() <= 0){
            throw new NoAvailableOrdersException("No orders available for courier with id: " + courierId);
        }

        //Assign first order available for now, in the future this
        // will eventually change to a more effective method of choosing random orders
        Long orderId = availableOrders.get(0);

        //Find delivery with required orderId
        Delivery deliveryToUpdate = deliveryRepository.findAll()
                .stream()
                .filter(delivery -> delivery.getOrder().getOrderId().equals(orderId))
                .collect(Collectors.toList()).get(0);


        Optional<Delivery> deliveryOptional = deliveryRepository.findById(deliveryToUpdate.getId());
        if (deliveryOptional.isEmpty()) {
            throw new DeliveryNotFoundException("Delivery id not found");
        }

        Delivery delivery = deliveryOptional.get();
        delivery.setCourierId(courierId);
        deliveryRepository.save(delivery);

    }


}
