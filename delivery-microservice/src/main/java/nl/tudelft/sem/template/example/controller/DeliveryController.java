package nl.tudelft.sem.template.example.controller;

import static nl.tudelft.sem.template.model.Order.StatusEnum;

import nl.tudelft.sem.template.api.DeliveryApi;
import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.IllegalOrderStatusException;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.service.DeliveryService;
import nl.tudelft.sem.template.example.service.OrderService;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.DeliveryPostRequest;
import nl.tudelft.sem.template.model.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DeliveryController implements DeliveryApi {

    DeliveryService deliveryService;

    OrderService orderService;

    AuthorizationService authorizationService;

    /**
     * Simple constructor that handles dependency injection of the service.
     *
     * @param deliveryService Instance of DeliveryService to handle the logic
     * @param orderService    Instance of OrderService to handle the logic
     */

    @Autowired
    DeliveryController(DeliveryService deliveryService, OrderService orderService, AuthorizationService authorizationService) {
        this.deliveryService = deliveryService;
        this.orderService = orderService;
        this.authorizationService = authorizationService;
    }

    @Override
    public ResponseEntity<Void> deliveryPost(@RequestHeader(value = "authorizationId") Integer authorizationId,
                                             @RequestBody DeliveryPostRequest deliveryPostRequest) {
        try {
            Delivery delivery = deliveryService.createDelivery(deliveryPostRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println(e.toString());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * If user has required permissions, the order's status is changed
     * to the one found in the body.
     *
     * @param orderId         Unique identifier of the order (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @param newStatus       (required) - New status of order
     * @return Response code
     * @path PUT: /delivery/order/{order_id}/status
     */
    @Override
    public ResponseEntity<Void> deliveryOrderOrderIdStatusPut(Integer orderId, Integer authorizationId, String newStatus) {
        //TODO(@mmadara): Handle authorization.
        try {
            orderService.setOrderStatus(orderId, newStatus);
            return ResponseEntity.ok().build();
        } catch (OrderNotFoundException | IllegalOrderStatusException e) {
            //TODO: Decide what to do with error message.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Returns a text format of the order's string.
     *
     * @param orderId         Unique identifier of the order (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return String form of order's status
     * @path GET: /delivery/order/{order_id}/status
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

    @Override
    public ResponseEntity<Void> deliveryOrderOrderIdIssuePut(Integer orderId, Integer authorizationId, Issue issue){
        try{
            if(!authorizationService.canUpdateDeliveryDetails(Long.valueOf(authorizationId),Long.valueOf(orderId)))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            deliveryService.addIssueToDelivery(orderId, issue);
            return ResponseEntity.ok().build();
        } catch (MicroserviceCommunicationException | DeliveryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<Issue> deliveryOrderOrderIdIssueGet(Integer orderId, Integer authorizationId){
        try{
            if(!authorizationService.canViewDeliveryDetails(Long.valueOf(authorizationId),Long.valueOf(orderId)))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            Issue issue = deliveryService.retrieveIssueOfDelivery(orderId);
            if(issue == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(issue);
        } catch (MicroserviceCommunicationException | DeliveryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
