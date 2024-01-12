package nl.tudelft.sem.template.example;

import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;

@Component
@Transactional
public class TestDatabaseLoader {
    private final DeliveryRepository deliveryRepository;

    private final OrderRepository orderRepository;

    private final VendorRepository vendorRepository;

    //the entities we will save in the database
    Vendor vendor1;
    Order order1;
    Delivery delivery1;

    Vendor vendor2;
    Order order2;
    Delivery delivery2;

    @Autowired
    public TestDatabaseLoader(DeliveryRepository deliveryRepository, OrderRepository orderRepository, VendorRepository vendorRepository){
        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
        this.vendorRepository = vendorRepository;
    }

    public void loadTestData() {
        //save delivery with no vendor address, no issue and no additional fields
        vendor1 = new Vendor(5L, 30L, null, new ArrayList<>());
        order1 = new Order(1234L, 4567L, vendor1, Order.StatusEnum.PENDING, new Location(2.0, 3.0));
        delivery1 = new Delivery();
        delivery1.setOrder(order1);
        deliveryRepository.save(delivery1);

        Issue issue = new Issue("traffic", "There was an accident on the way, so the order will be delivered later");
        vendor2 = new Vendor(9L, 30L, new Location(2.0, 2.0), new ArrayList<>());
        order2 = new Order(1L, 4L, vendor2, Order.StatusEnum.PENDING, new Location(3.0, 3.0));
        delivery2 = new Delivery();
        delivery2.setOrder(order2);
        delivery2.setIssue(issue);
        deliveryRepository.save(delivery2);
    }

    public void clearTestData() {
        deliveryRepository.deleteAll();
    }



}