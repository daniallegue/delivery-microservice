package nl.tudelft.sem.template.example.controller;

import static nl.tudelft.sem.template.model.Order.StatusEnum;

import java.time.OffsetDateTime;
import nl.tudelft.sem.template.api.DeliveryApi;
import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.IllegalOrderStatusException;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.service.DeliveryService;
import nl.tudelft.sem.template.example.service.OrderService;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.DeliveryPostRequest;
import nl.tudelft.sem.template.model.Issue;
import nl.tudelft.sem.template.model.Location;
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
    DeliveryController(DeliveryService deliveryService,
                       OrderService orderService, AuthorizationService authorizationService) {
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
        try {
            boolean isAuthorized = authorizationService.canUpdateDeliveryDetails(Long.valueOf(authorizationId),
                    Long.valueOf(orderId));
            if (!isAuthorized) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            // Update order status
            orderService.setOrderStatus(orderId, authorizationId, newStatus.replaceAll("\"", ""));
            return ResponseEntity.ok().build();
        } catch (MicroserviceCommunicationException e) {
            // Handle exceptions related to user role retrieval or authorization check
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (OrderNotFoundException | IllegalOrderStatusException e) {
            // Handle specific exceptions related to order processing
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
        try {
            String userRole = authorizationService.getUserRole(Long.valueOf(authorizationId));
            boolean isAuthorized = authorizationService.canViewDeliveryDetails(Long.valueOf(authorizationId),
                    Long.valueOf(orderId));
            if (!isAuthorized) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            StatusEnum status = orderService.getOrderStatus(orderId);
            return ResponseEntity.ok(status.toString());
        } catch (MicroserviceCommunicationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<OffsetDateTime> deliveryOrderOrderIdReadyTimeGet(Integer orderId, Integer authorizationId) {
        try {
            String userRole = authorizationService.getUserRole(Long.valueOf(authorizationId));
            boolean isAuthorized = authorizationService.canViewDeliveryDetails(Long.valueOf(authorizationId),
                    Long.valueOf(orderId));
            if (!isAuthorized) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            OffsetDateTime readyTime = deliveryService.getReadyTime(Long.valueOf(orderId));
            return readyTime == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).build() : ResponseEntity.ok(readyTime);
        } catch (MicroserviceCommunicationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<Void> deliveryOrderOrderIdIssuePut(Integer orderId, Integer authorizationId, Issue issue) {
        try {
            if (!authorizationService.canUpdateDeliveryDetails(Long.valueOf(authorizationId), Long.valueOf(orderId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            deliveryService.addIssueToDelivery(orderId, issue);
            return ResponseEntity.ok().build();
        } catch (MicroserviceCommunicationException | DeliveryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<Void> deliveryOrderOrderIdReadyTimePut(Integer orderId, Integer authorizationId,
                                                                 OffsetDateTime newReadyTime) {
        try {
            String userRole = authorizationService.getUserRole(Long.valueOf(authorizationId));
            boolean isAuthorized = authorizationService.canUpdateDeliveryDetails(Long.valueOf(authorizationId),
                    Long.valueOf(orderId));
            if (!isAuthorized) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            deliveryService.updateReadyTime(Long.valueOf(orderId), newReadyTime);
            return ResponseEntity.ok().build();
        } catch (MicroserviceCommunicationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Retrieves the issues associated with the order/delivery.
     *
     * @param orderId  Unique identifier of the order (required)
     * @param authorizationId Identification of the user who is making the request (required)
     */
    public ResponseEntity<Issue> deliveryOrderOrderIdIssueGet(Integer orderId, Integer authorizationId) {
        try {
            if (!authorizationService.canViewDeliveryDetails(Long.valueOf(authorizationId), Long.valueOf(orderId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Issue issue = deliveryService.retrieveIssueOfDelivery(orderId);
            if (issue == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(issue);
        } catch (MicroserviceCommunicationException | DeliveryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<OffsetDateTime> deliveryOrderOrderIdPickupTimeGet(Integer orderId, Integer authorizationId) {
        try {
            String userRole = authorizationService.getUserRole(Long.valueOf(authorizationId));
            boolean isAuthorized = authorizationService.canViewDeliveryDetails(Long.valueOf(authorizationId),
                    Long.valueOf(orderId));
            if (!isAuthorized) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            OffsetDateTime pickupTime = deliveryService.getPickupTime(Long.valueOf(orderId));
            return pickupTime == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).build() : ResponseEntity.ok(pickupTime);
        } catch (MicroserviceCommunicationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @Override
    public ResponseEntity<Void> deliveryOrderOrderIdPickupTimePut(Integer orderId,
                                                                  Integer authorizationId, OffsetDateTime newPickupTime) {
        try {
            String userRole = authorizationService.getUserRole(Long.valueOf(authorizationId));
            boolean isAuthorized = authorizationService.canUpdateDeliveryDetails(Long.valueOf(authorizationId),
                    Long.valueOf(orderId));

            if (!isAuthorized) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            deliveryService.updatePickupTime(Long.valueOf(orderId), newPickupTime);
            return ResponseEntity.ok().build();

        } catch (MicroserviceCommunicationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @Override
    public ResponseEntity<OffsetDateTime> deliveryOrderOrderIdTodGet(Integer orderId, Integer authorizationId) {
        try {
            String userRole = authorizationService.getUserRole(Long.valueOf(authorizationId));
            boolean isAuthorized = authorizationService.canViewDeliveryDetails(Long.valueOf(authorizationId),
                    Long.valueOf(orderId));
            if (!isAuthorized) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            OffsetDateTime deliveredTime = deliveryService.getDeliveredTime(Long.valueOf(orderId));
            return deliveredTime == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).build() :
                    ResponseEntity.ok(deliveredTime);
        } catch (MicroserviceCommunicationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Override
    public ResponseEntity<Void> deliveryOrderOrderIdTodPut(Integer orderId, Integer authorizationId,
                                                           OffsetDateTime newDeliveredTime) {
        try {
            String userRole = authorizationService.getUserRole(Long.valueOf(authorizationId));
            boolean isAuthorized = authorizationService.canUpdateDeliveryDetails(Long.valueOf(authorizationId),
                    Long.valueOf(orderId));
            if (!isAuthorized) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            deliveryService.updateDeliveredTime(Long.valueOf(orderId), newDeliveredTime);
            return ResponseEntity.ok().build();

        } catch (MicroserviceCommunicationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Override
    public ResponseEntity<OffsetDateTime> deliveryOrderOrderIdEtaGet(Integer orderId, Integer authorizationId) {
        try {
            String userRole = authorizationService.getUserRole(Long.valueOf(authorizationId));
            boolean isAuthorized = authorizationService.canViewDeliveryDetails(Long.valueOf(authorizationId),
                    Long.valueOf(orderId));

            if (!isAuthorized) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            OffsetDateTime eta = deliveryService.getEta(Long.valueOf(orderId));
            return eta == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).build() : ResponseEntity.ok(eta);

        } catch (MicroserviceCommunicationException | RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Returns the default delivery zone.
     *
     * @param authorizationId Identification of the user who is making the request (required)
     * @return Default delivery zone
     * @path GET: /delivery/default-delivery-zone
     */
    @Override
    public ResponseEntity<Integer> deliveryDefaultDeliveryZoneGet(Integer authorizationId) {
        Integer defaultDeliveryZone = Math.toIntExact(deliveryService.getDefaultDeliveryZone());
        return ResponseEntity.ok(defaultDeliveryZone);
    }

    /**
     * Updates the default delivery zone.
     *
     * @param newDeliveryZone Identification of the user who is making the request (required)
     * @param authorizationId Default delivery zone radius (required)
     * @path PUT: /delivery/default-delivery-zone
     */
    @Override
    public ResponseEntity<Void> deliveryDefaultDeliveryZonePut(Integer authorizationId, Integer newDeliveryZone) {
        try {
            if (!authorizationService.getUserRole((long) authorizationId).equals("admin")) {
                ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                return response;
            }
            deliveryService.updateDefaultDeliveryZone(newDeliveryZone);
            return new ResponseEntity<Void>(HttpStatus.OK);
        } catch (MicroserviceCommunicationException e) {
            return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /delivery/order/{order_id}/location.
     *
     * @param orderId The unique identifier of the order. (required)
     * @param authorizationId Identification of the user who is making the request. (required)
     * @return Retrieve the current location for an order.
     */
    @Override
    public ResponseEntity<Location> deliveryOrderOrderIdLocationGet(Integer orderId, Integer authorizationId) {
        try {
            String userRole = authorizationService.getUserRole(Long.valueOf(authorizationId));
            boolean isAuthorized = authorizationService.canViewDeliveryDetails(Long.valueOf(authorizationId),
                    Long.valueOf(orderId));
            if (!isAuthorized) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Long deliveryId = deliveryService.getDeliveryIdByOrderId(orderId.longValue());
            Location liveLocation = deliveryService.calculateLiveLocation(deliveryId);

            return ResponseEntity.ok(liveLocation);

        } catch (MicroserviceCommunicationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Returns the courier assigned to an order.
     *
     * @param orderId Unique identifier of the order (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @path GET: /delivery/order/{order_id}/courier
     */
    @Override
    public ResponseEntity<Integer> deliveryOrderOrderIdCourierGet(Integer orderId, Integer authorizationId) {
        try {
            Integer courierId = Math.toIntExact(deliveryService.getCourierFromOrder(orderId));
            return ResponseEntity.ok(courierId);
        } catch (OrderNotFoundException | CourierNotFoundException e) {
            return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
        }
    }
}

