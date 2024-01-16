package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.api.CourierApi;
import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.*;
import nl.tudelft.sem.template.example.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CourierController implements CourierApi {
    CourierService courierService;
    AuthorizationService authorizationService;

    /**
     * Simple constructor that handles dependency injection of the service.
     *
     * @param courierService Instance of CourierService to handle the logic
     */
    @Autowired
    public CourierController(CourierService courierService, AuthorizationService authorizationService) {
        this.courierService = courierService;
        this.authorizationService = authorizationService;
    }

    /**
     * Returns a text format of the order's string.
     *
     * @param courierId       Unique identifier of the courier (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return list of Order Ids which are available to the courier
     * @path GET: GET /courier/delivery/{courier_id}/available-orders
     */
    @Override
    public ResponseEntity<List<Long>> courierDeliveryCourierIdAvailableOrdersGet(Long courierId, Integer authorizationId) {
        try {
            if (!authorizationService.canViewCourierAnalytics(authorizationId, Math.toIntExact(courierId))) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            List<Long> availableOrderIds = courierService.getAvailableOrderIds(courierId);
            return ResponseEntity.ok(availableOrderIds);
        } catch (MicroserviceCommunicationException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }


    /**
     * Assigns a random order to a courier.
     *
     * @param courierId       Unique identifier of the courier (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return Response code
     * @path PUT: /courier/delivery/{courier_id}/assign-any-order
     */
    @Override
    public ResponseEntity<Void> courierDeliveryCourierIdAssignAnyOrderPut(Long courierId, Integer authorizationId) {
        try {
            courierService.assignCourierToRandomOrder(courierId);
        } catch (DeliveryNotFoundException | NoAvailableOrdersException | OrderNotFoundException |
                 CourierNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Assigns a specific order to a courier.
     *
     * @param courierId       Unique identifier of the courier (required)
     * @param orderId         Unique identifier of the delivery to be assigned (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return ResponseEntity with appropriate status code
     * @path PUT: /courier/delivery/{courier_id}/assign/{order_id}
     */
    @Override
    public ResponseEntity<Void> courierDeliveryCourierIdAssignOrderIdPut(Long courierId,
                                                                         Long orderId, Integer authorizationId) {
        try {
            courierService.assignCourierToSpecificOrder(courierId, orderId);
            return ResponseEntity.ok().build();
        } catch (DeliveryNotFoundException | NoAvailableOrdersException | OrderNotFoundException |
                 CourierNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
