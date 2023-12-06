package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.api.DeliveryApi;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController implements DeliveryApi {
    OrderRepository orderRepository;

    @Autowired
    public DummyController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

//    @Override
//    public ResponseEntity<String> deliveryOrderOrderIdStatusGet(@PathVariable("order_id") Integer orderId, @RequestHeader(value = "authorizationId", required = true) Integer authorizationId) {
//        Order o = new Order();
//        o.setId(Long.valueOf(orderId));
//        o.setStatus(Order.StatusEnum.PENDING);
//        orderRepository.save(o);
//        return ResponseEntity.ok(o.getStatus().toString());
//    }
//
//    @GetMapping("/delivery/order/id/{order_id}")
//    public ResponseEntity<Order> getOrderByIdd(@PathVariable("order_id") Long orderId) {
//        return ResponseEntity.ok(orderRepository.findOrderById(orderId));
//    }
}