package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.api.DeliveryApi;
import nl.tudelft.sem.template.example.service.DeliveryService;
import nl.tudelft.sem.template.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeliveryController implements DeliveryApi {
    DeliveryService deliveryService;

    @Autowired
    DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Override
    public ResponseEntity<Void> deliveryPost(@RequestHeader(value = "authorizationId") Integer authorizationId,
                                             @RequestBody DeliveryPostRequest deliveryPostRequest) {
        try {
            Delivery delivery = deliveryService.createDelivery(deliveryPostRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("bla");
            return ResponseEntity.badRequest().build();
        }
    }

}
