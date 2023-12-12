package nl.tudelft.sem.template.example.controller;

import static nl.tudelft.sem.template.model.Order.StatusEnum;

import nl.tudelft.sem.template.api.DeliveryApi;
import nl.tudelft.sem.template.example.exception.IllegalOrderStatusException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController implements DeliveryApi {
    OrderService orderService;

    /**
     * Simple constructor that handles dependency injection of the service.
     *
     * @param orderService Instance of OrderService to handle the logic
     */
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * If user has required permissions, the order's status is changed
     * to the one found in the body.
     *
     * @path PUT: /delivery/order/{order_id}/status
     * @param orderId         Unique identifier of the order (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @param newStatus       (required) - New status of order.
     * @return Response code.
     */
    @Override
    public ResponseEntity<Void> deliveryOrderOrderIdStatusPut(Integer orderId, Integer authorizationId, String newStatus) {
        //TODO(@mmadara): Handle authorization.
        try {
            orderService.setOrderStatus(orderId, newStatus);
        } catch (OrderNotFoundException | IllegalOrderStatusException e) {
            //TODO: Decide what to do with error message.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Returns a text format of the order's string.
     *
     * @path GET: /delivery/order/{order_id}/status
     * @param orderId         Unique identifier of the order (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return String form of order's status
     */
    @Override
    public ResponseEntity<String> deliveryOrderOrderIdStatusGet(Integer orderId, Integer authorizationId) {
        //TODO(@mmadara): Handle authorization.
        try {
            StatusEnum status = orderService.getOrderStatus(orderId);
            return ResponseEntity.ok(status.toString());
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}