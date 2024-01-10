package nl.tudelft.sem.template.example.controller;

import static nl.tudelft.sem.template.model.Order.StatusEnum;

import nl.tudelft.sem.template.api.DeliveryApi;
import nl.tudelft.sem.template.example.exception.IllegalOrderStatusException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.service.DeliveryService;
import nl.tudelft.sem.template.example.service.OrderService;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.DeliveryPostRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;


@RestController
public class DeliveryController implements DeliveryApi {

    DeliveryService deliveryService;

    OrderService orderService;

    /**
     * Simple constructor that handles dependency injection of the service.
     *
     * @param deliveryService Instance of DeliveryService to handle the logic
     * @param orderService    Instance of OrderService to handle the logic
     */

    @Autowired
    DeliveryController(DeliveryService deliveryService, OrderService orderService) {
        this.deliveryService = deliveryService;
        this.orderService = orderService;
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
    public ResponseEntity<OffsetDateTime> deliveryOrderOrderIdReadyTimeGet(Integer orderId, Integer authorizationId) {
        // TODO: Implement authorization check based on 'authorizationId'

        try {
            OffsetDateTime readyTime = deliveryService.getReadyTime(Long.valueOf(orderId));

            if (readyTime == null) {
                // If there's no ready time set for the order
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(readyTime);
        } catch (OrderNotFoundException e) {
            // This exception should be thrown by your service layer when an order is not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            // This exception should be thrown by your service layer for any invalid inputs
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            // Handle other unexpected exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> deliveryOrderOrderIdReadyTimePut(Integer orderId, Integer authorizationId, OffsetDateTime newReadyTime) {
        // TODO: Implement authorization check based on 'authorizationId'

        try {
            deliveryService.updateReadyTime(Long.valueOf(orderId), newReadyTime);
            return ResponseEntity.ok().build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<OffsetDateTime> deliveryOrderOrderIdPickupTimeGet(Integer orderId, Integer authorizationId) {
        // TODO: Implement authorization check based on 'authorizationId'

        try {
            OffsetDateTime pickupTime = deliveryService.getPickupTime(Long.valueOf(orderId));

            if (pickupTime == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(pickupTime);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> deliveryOrderOrderIdPickupTimePut(Integer orderId, Integer authorizationId, OffsetDateTime newPickupTime) {
        // TODO: Implement authorization check based on 'authorizationId'

        try {
            deliveryService.updatePickupTime(Long.valueOf(orderId), newPickupTime);
            return ResponseEntity.ok().build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<OffsetDateTime> deliveryOrderOrderIdTodGet(Integer orderId, Integer authorizationId) {
        // TODO: Implement authorization check based on 'authorizationId'

        try {
            OffsetDateTime deliveredTime = deliveryService.getDeliveredTime(Long.valueOf(orderId));

            if (deliveredTime == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(deliveredTime);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> deliveryOrderOrderIdTodPut(Integer orderId, Integer authorizationId, OffsetDateTime newDeliveredTime) {
        // TODO: Implement authorization check based on 'authorizationId'

        try {
            deliveryService.updateDeliveredTime(Long.valueOf(orderId), newDeliveredTime);
            return ResponseEntity.ok().build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

//    @Override
//    public ResponseEntity<OffsetDateTime> deliveryOrderOrderIdEtaGet(Integer orderId, Integer authorizationId) {
//        // TODO: Implement authorization check based on 'authorizationId'
//
//        try {
//            OffsetDateTime eta = deliveryService.getEta(Long.valueOf(orderId));
//
//            if (eta == null) {
//                // If there's no ETA set for the order
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//            }
//
//            return ResponseEntity.ok(eta);
//        } catch (OrderNotFoundException e) {
//            // Handle case when order is not found
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (Exception e) {
//            // Handle other unexpected exceptions
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @Override
//    public ResponseEntity<Void> deliveryOrderOrderIdEtaPut(Integer orderId, Integer authorizationId, OffsetDateTime eta) {
//        // TODO: Implement authorization check based on 'authorizationId'
//
//        try {
//            deliveryService.updateEta(Long.valueOf(orderId), eta);
//            return ResponseEntity.ok().build();
//        } catch (OrderNotFoundException e) {
//            // Handle case when order is not found
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (Exception e) {
//            // Handle other unexpected exceptions
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

}
