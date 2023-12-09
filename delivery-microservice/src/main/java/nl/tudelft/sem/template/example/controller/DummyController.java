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
        Location location = new Location(3.0,4.0);
        Vendor vendor = Vendor.builder()
                .id(3L)
                .couriers(new ArrayList<>())
                .address(location)
                .deliveryZone(9L)
                .build();
        Order order = Order.builder()
                .orderId(Long.valueOf(orderId))
                .status(Order.StatusEnum.PENDING)
                .vendor(vendor)
                .destination(location)
                .customerId(5L)
                .build();
        or.save(order);
        return ResponseEntity.ok(order.getStatus().toString());
    }

    @GetMapping("/delivery/order/id/{order_id}")
    public ResponseEntity<Order> getOrderByIdd(@PathVariable("order_id") Integer orderId, @RequestHeader(value = "authorizationId") Integer authorizationId) {
        return ResponseEntity.ok(or.findOrderByOrderId(Long.valueOf(orderId)));
    }

}
