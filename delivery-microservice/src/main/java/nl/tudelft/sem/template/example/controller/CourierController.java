package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.api.CourierApi;
import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.service.CourierService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CourierController implements CourierApi {
    CourierService courierService;

    /**
     * Simple constructor that handles dependency injection of the service.
     *
     * @param courierService Instance of CourierService to handle the logic
     */
    @Autowired
    public CourierController(CourierService courierService) {
        this.courierService = courierService;
    }

    /**
     * Returns a text format of the order's string.
     *
     * @path GET: GET /courier/delivery/{courier_id}/available-orders
     * @param courierId Unique identifier of the courier (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return list of Order Ids which are available to the courier
     */
    @Override
    public ResponseEntity<List<Long>> courierDeliveryCourierIdAvailableOrdersGet(Long courierId, Integer authorizationId) {
        //TODO: handle authorization
        List<Long> availableOrderIds = courierService.getAvailableOrderIds(courierId);
        return ResponseEntity.ok(availableOrderIds);
    }


    /**
     * Returns a text format of the order's string.
     *
     * @path PUT: /courier/delivery/{courier_id}/assign-any-order
     * @param courierId Unique identifier of the courier (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return Response code
     */
    @Override
    public ResponseEntity<Void> courierDeliveryCourierIdAssignAnyOrderPut(Long courierId, Integer authorizationId)  {
        try {
            courierService.assignCourierToRandomOrder(courierId);
        } catch (DeliveryNotFoundException | NoAvailableOrdersException e) {
            return (ResponseEntity<Void>) ResponseEntity.status(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Assigns a specific order to a courier.
     *
     * @path PUT: /courier/delivery/{courier_id}/assign/{order_id}
     * @param courierId Unique identifier of the courier (required)
     * @param orderId Unique identifier of the delivery to be assigned (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return ResponseEntity with appropriate status code
     */
    @Override
    public ResponseEntity<Void> courierDeliveryCourierIdAssignOrderIdPut(Long courierId, Long orderId, Integer authorizationId) {
        try {
            courierService.assignCourierToSpecificOrder(courierId, orderId);
            return ResponseEntity.ok().build();
        } catch (DeliveryNotFoundException | CourierNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
