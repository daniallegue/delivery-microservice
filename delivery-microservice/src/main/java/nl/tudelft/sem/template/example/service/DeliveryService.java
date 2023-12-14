package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.exception.OrderAlreadyExistsException;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeliveryService {
    DeliveryRepository deliveryRepository;
    OrderRepository orderRepository;
    VendorRepository vendorRepository;
    VendorService vendorService;

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
}
