package nl.tudelft.sem.template.example.controller;
import nl.tudelft.sem.template.example.exception.OrderAlreadyExistsException;
import nl.tudelft.sem.template.example.service.DeliveryService;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.DeliveryPostRequest;
import nl.tudelft.sem.template.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class DeliveryControllerTest {


    private DeliveryService deliveryService;
    private DeliveryController deliveryController;

    private DeliveryPostRequest dummyDeliveryPostRequest;


    @BeforeEach
    void setup(){
        dummyDeliveryPostRequest =  new DeliveryPostRequest();
        dummyDeliveryPostRequest.setVendorId(1);
        dummyDeliveryPostRequest.setOrderId(123);
        dummyDeliveryPostRequest.setCustomerId(456);
        dummyDeliveryPostRequest.setDestination(new Location(4.0, 5.0));

        deliveryService = Mockito.mock(DeliveryService.class);
        deliveryController = new DeliveryController(deliveryService);
    }

    @Test
    public void testDeliveryPostSuccess() throws Exception {
        Mockito.when(deliveryService.createDelivery(dummyDeliveryPostRequest))
                .thenReturn(new Delivery());

        ResponseEntity<Void> response = deliveryController.deliveryPost(1, dummyDeliveryPostRequest);

        verify(deliveryService).createDelivery(dummyDeliveryPostRequest);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testDeliveryPostBadRequest() throws Exception {
        when(deliveryService.createDelivery(dummyDeliveryPostRequest))
                .thenThrow(OrderAlreadyExistsException.class);

        ResponseEntity<Void> response = deliveryController.deliveryPost(1, dummyDeliveryPostRequest);

        verify(deliveryService).createDelivery(dummyDeliveryPostRequest);
        
        assertEquals(400, response.getStatusCodeValue());
    }
}
