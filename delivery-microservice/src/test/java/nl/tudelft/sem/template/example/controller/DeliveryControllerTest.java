package nl.tudelft.sem.template.example.controller;
import nl.tudelft.sem.template.example.exception.OrderAlreadyExistsException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
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


import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class DeliveryControllerTest {


    private DeliveryService deliveryService;

    private OrderService orderService;
    private DeliveryController deliveryController;

    private DeliveryPostRequest dummyDeliveryPostRequest;


    @BeforeEach
    void setup(){
        dummyDeliveryPostRequest =  new DeliveryPostRequest();
        dummyDeliveryPostRequest.setVendorId(1);
        dummyDeliveryPostRequest.setOrderId(123);
        dummyDeliveryPostRequest.setCustomerId(456);
        //dummyDeliveryPostRequest.setDestination(new Location(4.0, 5.0));

        deliveryService = Mockito.mock(DeliveryService.class);
        orderService = Mockito.mock(OrderService.class);
        deliveryController = new DeliveryController(deliveryService, orderService);
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
    public void testDeliveryOrderOrderIdReadyTimeGet_Success() throws Exception {
        OffsetDateTime readyTime = OffsetDateTime.now();
        when(deliveryService.getReadyTime(123L)).thenReturn(readyTime);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdReadyTimeGet(123, 1);

        verify(deliveryService).getReadyTime(123L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(readyTime, response.getBody());
    }

    @Test
    public void testDeliveryOrderOrderIdReadyTimeGet_NotFound() throws Exception {
        when(deliveryService.getReadyTime(123L)).thenThrow(new OrderNotFoundException("Order not found"));

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdReadyTimeGet(123, 1);

        verify(deliveryService).getReadyTime(123L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdReadyTimePut_Success() throws Exception {
        OffsetDateTime newReadyTime = OffsetDateTime.now();
        doNothing().when(deliveryService).updateReadyTime(123L, newReadyTime);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdReadyTimePut(123, 1, newReadyTime);

        verify(deliveryService).updateReadyTime(123L, newReadyTime);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdReadyTimePut_NotFound() throws Exception {
        OffsetDateTime newReadyTime = OffsetDateTime.now();
        doThrow(new OrderNotFoundException("Order not found")).when(deliveryService).updateReadyTime(123L, newReadyTime);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdReadyTimePut(123, 1, newReadyTime);

        verify(deliveryService).updateReadyTime(123L, newReadyTime);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Tests for GET /delivery/order/{order_id}/pickup-time
    @Test
    public void testDeliveryOrderOrderIdPickupTimeGet_Success() throws Exception {
        OffsetDateTime pickupTime = OffsetDateTime.now();
        when(deliveryService.getPickupTime(123L)).thenReturn(pickupTime);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdPickupTimeGet(123, 1);

        verify(deliveryService).getPickupTime(123L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pickupTime, response.getBody());
    }

    @Test
    public void testDeliveryOrderOrderIdPickupTimeGet_NotFound() throws Exception {
        when(deliveryService.getPickupTime(123L)).thenThrow(new OrderNotFoundException("Order not found"));

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdPickupTimeGet(123, 1);

        verify(deliveryService).getPickupTime(123L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Tests for PUT /delivery/order/{order_id}/pickup-time
    @Test
    public void testDeliveryOrderOrderIdPickupTimePut_Success() throws Exception {
        OffsetDateTime newPickupTime = OffsetDateTime.now();
        doNothing().when(deliveryService).updatePickupTime(123L, newPickupTime);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdPickupTimePut(123, 1, newPickupTime);

        verify(deliveryService).updatePickupTime(123L, newPickupTime);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdPickupTimePut_NotFound() throws Exception {
        OffsetDateTime newPickupTime = OffsetDateTime.now();
        doThrow(new OrderNotFoundException("Order not found")).when(deliveryService).updatePickupTime(123L, newPickupTime);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdPickupTimePut(123, 1, newPickupTime);

        verify(deliveryService).updatePickupTime(123L, newPickupTime);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdTodGet_Success() throws Exception {
        OffsetDateTime deliveredTime = OffsetDateTime.now();
        when(deliveryService.getDeliveredTime(123L)).thenReturn(deliveredTime);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdTodGet(123, 1);

        verify(deliveryService).getDeliveredTime(123L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(deliveredTime, response.getBody());
    }

    @Test
    public void testDeliveryOrderOrderIdTodGet_NotFound() throws Exception {
        when(deliveryService.getDeliveredTime(123L)).thenThrow(new OrderNotFoundException("Order not found"));

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdTodGet(123, 1);

        verify(deliveryService).getDeliveredTime(123L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Tests for PUT /delivery/order/{order_id}/tod
    @Test
    public void testDeliveryOrderOrderIdTodPut_Success() throws Exception {
        OffsetDateTime newDeliveredTime = OffsetDateTime.now();
        doNothing().when(deliveryService).updateDeliveredTime(123L, newDeliveredTime);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdTodPut(123, 1, newDeliveredTime);

        verify(deliveryService).updateDeliveredTime(123L, newDeliveredTime);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdTodPut_NotFound() throws Exception {
        OffsetDateTime newDeliveredTime = OffsetDateTime.now();
        doThrow(new OrderNotFoundException("Order not found")).when(deliveryService).updateDeliveredTime(123L, newDeliveredTime);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdTodPut(123, 1, newDeliveredTime);

        verify(deliveryService).updateDeliveredTime(123L, newDeliveredTime);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdEtaGet_Success() throws Exception {
        OffsetDateTime mockEta = OffsetDateTime.now();

        when(deliveryService.getEta(123L)).thenReturn(mockEta);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdEtaGet(123,1 );

        verify(deliveryService).getEta(123L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockEta, response.getBody());
    }

    @Test
    public void testDeliveryOrderOrderIdEtaGet_OrderNotFound() throws Exception {

        when(deliveryService.getEta(123L)).thenThrow(new OrderNotFoundException("Order not found"));

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdEtaGet(123, 1);

        verify(deliveryService).getEta(123L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdEtaGet_Exception() throws Exception {

        when(deliveryService.getEta(123L)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdEtaGet(123, 1);

        verify(deliveryService).getEta(123L);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
