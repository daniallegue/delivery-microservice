package nl.tudelft.sem.template.example.controller;
import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.exception.OrderAlreadyExistsException;
import nl.tudelft.sem.template.example.service.DeliveryService;
import nl.tudelft.sem.template.example.service.OrderService;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.DeliveryPostRequest;
import nl.tudelft.sem.template.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class DeliveryControllerTest {


    private DeliveryService deliveryService;

    private OrderService orderService;
    private DeliveryController deliveryController;

    private AuthorizationService authorizationService;

    private DeliveryPostRequest dummyDeliveryPostRequest;


    @BeforeEach
    void setup(){
        dummyDeliveryPostRequest =  new DeliveryPostRequest();
        dummyDeliveryPostRequest.setVendorId(1);
        dummyDeliveryPostRequest.setOrderId(123);
        dummyDeliveryPostRequest.setCustomerId(456);
        dummyDeliveryPostRequest.setDestination(new Location(4.0, 5.0));

        deliveryService = Mockito.mock(DeliveryService.class);
        authorizationService = Mockito.mock(AuthorizationService.class);
        orderService = Mockito.mock(OrderService.class);
        deliveryController = new DeliveryController(deliveryService, orderService, authorizationService);
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

    @Test
    public void getDefaultDeliveryZoneTest() {
        when(deliveryService.getDefaultDeliveryZone()).thenReturn(30L);

        ResponseEntity<Integer> defaultZone = deliveryController.deliveryDefaultDeliveryZoneGet(1);
        assertEquals(30, defaultZone.getBody());
    }

    @Test
    public void updateDefaultDeliveryZoneSuccessfulTest() throws MicroserviceCommunicationException {
        when(authorizationService.getUserRole(2L)).thenReturn(authorizationService.ADMIN);
        ResponseEntity<Void> response = deliveryController.deliveryDefaultDeliveryZonePut(25, 2);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void updateDefaultDeliveryZoneUnauthorizedTest() throws MicroserviceCommunicationException {
        when(authorizationService.getUserRole(2L)).thenReturn(authorizationService.CUSTOMER);

        ResponseEntity<Void> response = deliveryController.deliveryDefaultDeliveryZonePut(25, 2);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void updateDefaultDeliveryZoneMiscommunicationTest() throws MicroserviceCommunicationException {
        when(authorizationService.getUserRole(5L)).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<Void> response = deliveryController.deliveryDefaultDeliveryZonePut(25, 5);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
