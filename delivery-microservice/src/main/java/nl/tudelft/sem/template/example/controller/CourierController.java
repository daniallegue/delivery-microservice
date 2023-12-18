package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.api.CourierApi;
import nl.tudelft.sem.template.example.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class CourierController implements CourierApi {
    CourierService courierService;

    @Autowired
    public CourierController(CourierService courierService) {
        this.courierService = courierService;
    }

    /**
     * Returns a list of ids of available orders for a given courier.
     *
     * @param courierId Unique identifier of the courier (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return List of ids of available orders
     */
    @Override
    public ResponseEntity<List<Long>> courierDeliveryCourierIdAvailableOrdersGet(Long courierId, Integer authorizationId) {
        //TODO: handle authorization
        List<Long> availableOrderIds = courierService.getAvailableOrderIds(courierId);
        return ResponseEntity.ok(availableOrderIds);
    }
}
