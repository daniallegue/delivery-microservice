package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.api.DeliveryApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DummyController implements DeliveryApi {

    @Override
    public ResponseEntity<Integer> deliveryOrderOrderIdCourierGet(@PathVariable("order_id") Integer orderId, @RequestHeader(value = "authorizationId", required = true) Integer authorizationId) {
        System.out.println("Accessed the endpoint");
        return ResponseEntity.status(HttpStatus.OK).body(4444);
    }
}
