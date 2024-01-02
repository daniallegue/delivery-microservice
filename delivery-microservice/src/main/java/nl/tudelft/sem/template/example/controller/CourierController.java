package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.api.CourierApi;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;
import nl.tudelft.sem.template.example.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

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

<<<<<<< 7937d327656075ee98835cd3ba6f7a20b11bcc85

    /**
     * Returns a text format of the order's string.
     *
     * @path GET: GET /courier/delivery/{courier_id}/available-orders
     * @param courierId Unique identifier of the courier (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return list of Order Ids which are available to the courier
=======
    /**
     * Returns a list of ids of available orders for a given courier.
     *
     * @param courierId Unique identifier of the courier (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return List of ids of available orders
>>>>>>> 7de2aa83e33fba199d935d97cadcc1a6a3d24eef
     */
    @Override
    public ResponseEntity<List<Long>> courierDeliveryCourierIdAvailableOrdersGet(Long courierId, Integer authorizationId) {
        //TODO: handle authorization
        List<Long> availableOrderIds = courierService.getAvailableOrderIds(courierId);
        return ResponseEntity.ok(availableOrderIds);
<<<<<<< 7937d327656075ee98835cd3ba6f7a20b11bcc85
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
=======
>>>>>>> 7de2aa83e33fba199d935d97cadcc1a6a3d24eef
    }
}
