package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.api.DeliveryApi;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
public class DummyController implements DeliveryApi {
    OrderRepository or;

    @Autowired
    DummyController(OrderRepository orderRepository){
        or = orderRepository;
    }

    @Override
    public ResponseEntity<Integer> deliveryOrderOrderIdCourierGet(@PathVariable("order_id") Integer orderId, @RequestHeader(value = "authorizationId", required = true) Integer authorizationId) {
        System.out.println("Accessed the endpoint");
        return ResponseEntity.status(HttpStatus.OK).body(4444);
    }

    @Override
    public ResponseEntity<String> deliveryOrderOrderIdStatusGet(@PathVariable("order_id") Integer orderId, @RequestHeader(value = "authorizationId") Integer authorizationId) {
        Order o = new Order();
        o.setOrderId(Long.valueOf(orderId));
        o.setStatus(Order.StatusEnum.PENDING);
        Vendor vendor = new Vendor();
        vendor.setId(3L);
        Location location = new Location();
        location.setLatitude(3.0);
        location.setLongitude(4.0);
        vendor.setAddress(location);
        vendor.setCouriers(new ArrayList<>());
        vendor.setDeliveryZone(9L);
        o.setCustomerId(4L);
        o.setVendor(vendor);
        o.setDestination(location);
        or.save(o);
        return ResponseEntity.ok(o.getStatus().toString());
    }

    @GetMapping("/delivery/order/id/{order_id}")
    public ResponseEntity<Order> getOrderByIdd(@PathVariable("order_id") Integer orderId, @RequestHeader(value = "authorizationId") Integer authorizationId) {
        return ResponseEntity.ok(or.findOrderByOrderId(Long.valueOf(orderId)));
    }

}
