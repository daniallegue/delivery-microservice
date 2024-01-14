package nl.tudelft.sem.template.example.controller;
import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.exception.OrderAlreadyExistsException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.service.DeliveryService;
import nl.tudelft.sem.template.example.service.OrderService;
import nl.tudelft.sem.template.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class DeliveryControllerTest {


    private DeliveryService deliveryService;

    private OrderService orderService;

    private AuthorizationService authorizationService;

    private DeliveryController deliveryController;

    private DeliveryPostRequest dummyDeliveryPostRequest;

    Vendor vendor1;
    Order order1;
    Delivery delivery1;

    Vendor vendor2;
    Order order2;
    Delivery delivery2;

    Issue issue;


    @BeforeEach
    void setup() throws MicroserviceCommunicationException {
        dummyDeliveryPostRequest =  new DeliveryPostRequest();
        dummyDeliveryPostRequest.setVendorId(1);
        dummyDeliveryPostRequest.setOrderId(123);
        dummyDeliveryPostRequest.setCustomerId(456);
        //dummyDeliveryPostRequest.setDestination(new Location(4.0, 5.0));

        deliveryService = Mockito.mock(DeliveryService.class);
        authorizationService = Mockito.mock(AuthorizationService.class);
        orderService = Mockito.mock(OrderService.class);
        authorizationService = Mockito.mock(AuthorizationService.class);
        deliveryController = new DeliveryController(deliveryService, orderService, authorizationService);

        // Default authorization behavior
        when(authorizationService.getUserRole(anyLong())).thenReturn("customer");
        when(authorizationService.canViewDeliveryDetails(anyLong(), anyLong())).thenReturn(true);
        when(authorizationService.canUpdateDeliveryDetails(anyLong(), anyLong())).thenReturn(true);

        vendor1 = new Vendor(5L, 30L, null, new ArrayList<>());
        order1 = new Order(1234L, 4567L, vendor1, Order.StatusEnum.PENDING, new Location(2.0, 3.0));
        delivery1 = new Delivery();
        delivery1.setId(44444L);
        delivery1.setOrder(order1);

        Issue issue = new Issue("traffic", "There was an accident on the way, so the order will be delivered later");
        vendor2 = new Vendor(9L, 30L, new Location(2.0, 2.0), new ArrayList<>());
        order2 = new Order(1L, 4L, vendor2, Order.StatusEnum.PENDING, new Location(3.0, 3.0));
        delivery2 = new Delivery();
        delivery2.setOrder(order2);
        delivery2.setId(222L);
        delivery2.setIssue(issue);
    }

    @Test
    public void testDeliveryPostSuccess() throws Exception {
        Mockito.when(deliveryService.createDelivery(dummyDeliveryPostRequest))
                .thenReturn(new Delivery());

        ResponseEntity<Void> response = deliveryController.deliveryPost(1, dummyDeliveryPostRequest);

        verify(deliveryService).createDelivery(dummyDeliveryPostRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
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
    public void testPutIssueSuccess() throws Exception {
        Mockito.when(authorizationService.canUpdateDeliveryDetails(any(), any())).thenReturn(true);
        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdIssuePut(1234, 4567, issue);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deliveryService, times(1)).addIssueToDelivery(any(), any());
        verify(authorizationService, times(1)).canUpdateDeliveryDetails(any(), any());
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
    public void testPutIssueBadMicroserviceCommunication() throws Exception {
        Mockito.when(authorizationService.canUpdateDeliveryDetails(any(), any())).thenThrow(MicroserviceCommunicationException.class);
        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdIssuePut(1234, 4567, issue);
        assertEquals(400, response.getStatusCodeValue());
        verify(authorizationService, times(1)).canUpdateDeliveryDetails(any(), any());
        verify(deliveryService, never()).addIssueToDelivery(any(), any());
    }

    @Test
    public void testPutIssueForbidden() throws Exception {
        Mockito.when(authorizationService.canUpdateDeliveryDetails(any(), any())).thenReturn(false);
        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdIssuePut(1234, 4567, issue);
        assertEquals(403, response.getStatusCodeValue());
        verify(authorizationService, times(1)).canUpdateDeliveryDetails(any(), any());
        verify(deliveryService, never()).addIssueToDelivery(any(), any());
    }

    @Test
    public void testGetIssueSuccess() throws Exception {
        Mockito.when(authorizationService.canViewDeliveryDetails(any(), any())).thenReturn(true);
        Mockito.when(deliveryService.retrieveIssueOfDelivery(any())).thenReturn(new Issue("traffic", "There was an accident on the way, so the order will be delivered later"));

        ResponseEntity<Issue> response = deliveryController.deliveryOrderOrderIdIssueGet(1234, 4567);

        verify(deliveryService,times(1)).retrieveIssueOfDelivery(any());
        verify(authorizationService, times(1)).canViewDeliveryDetails(any(), any());

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

    @Test
    public void testGetIssueBadMicroserviceCommunication() throws Exception {
        Mockito.when(authorizationService.canViewDeliveryDetails(any(), any())).thenThrow(MicroserviceCommunicationException.class);
        ResponseEntity<Issue> response = deliveryController.deliveryOrderOrderIdIssueGet(1234, 4567);
        assertEquals(400, response.getStatusCodeValue());
        verify(authorizationService, times(1)).canViewDeliveryDetails(any(), any());
        verify(deliveryService, never()).retrieveIssueOfDelivery(any());
    }

    @Test
    public void testGetIssueForbidden() throws Exception {
        Mockito.when(authorizationService.canViewDeliveryDetails(any(), any())).thenReturn(false);
        ResponseEntity<Issue> response = deliveryController.deliveryOrderOrderIdIssueGet(1234, 4567);
        assertEquals(403, response.getStatusCodeValue());
        verify(authorizationService, times(1)).canViewDeliveryDetails(any(), any());
        verify(deliveryService, never()).retrieveIssueOfDelivery(any());
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

    @Test
    public void deliveryOrderOrderIdCourierGetSuccessfulTest() throws OrderNotFoundException, CourierNotFoundException {
        when(deliveryService.getCourierFromOrder(1)).thenReturn(2L);

        ResponseEntity<Integer> response = deliveryController.deliveryOrderOrderIdCourierGet(1, 1);
        assertEquals(2, response.getBody());
    }

    @Test
    public void deliveryOrderOrderIdCourierGetOrderNotFoundTest() throws OrderNotFoundException, CourierNotFoundException {
        when(deliveryService.getCourierFromOrder(1)).thenThrow(OrderNotFoundException.class);

        ResponseEntity<Integer> response = deliveryController.deliveryOrderOrderIdCourierGet(1, 1);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void deliveryOrderOrderIdCourierGetNoCourierTest() throws OrderNotFoundException, CourierNotFoundException {
        when(deliveryService.getCourierFromOrder(1)).thenThrow(CourierNotFoundException.class);

        ResponseEntity<Integer> response = deliveryController.deliveryOrderOrderIdCourierGet(1, 1);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}
