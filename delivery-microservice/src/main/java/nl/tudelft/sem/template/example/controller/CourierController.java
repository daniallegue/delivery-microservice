package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.api.CourierApi;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
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

    @Override
    public ResponseEntity<List<Long>> courierDeliveryCourierIdAvailableOrdersGet(Long courierId, Integer authorizationId) {
        //TODO: handle authorization

        List<Long> availableOrderIds = courierService.getAvailableOrderIds(courierId);
        return ResponseEntity.ok(availableOrderIds);
    }


    @Override
    public ResponseEntity<Void> courierDeliveryCourierIdAssignAnyOrderPut(Long courierId, Integer authorizationId)  {
        try {
            Long response = courierService.assignCourierToRandomOrder(courierId);
        } catch (DeliveryNotFoundException e) {
            //return ResponseEntity
        }
        return ResponseEntity.ok().build();
    }


}
