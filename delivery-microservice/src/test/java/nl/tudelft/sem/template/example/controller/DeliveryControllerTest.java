package nl.tudelft.sem.template.example.controller;
import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.*;
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
import static org.mockito.ArgumentMatchers.anyLong;
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

    OffsetDateTime time;

    @BeforeEach
    void setup() throws MicroserviceCommunicationException {
        dummyDeliveryPostRequest =  new DeliveryPostRequest();
        dummyDeliveryPostRequest.setVendorId(1);
        dummyDeliveryPostRequest.setOrderId(123);
        dummyDeliveryPostRequest.setCustomerId(456);

        deliveryService = Mockito.mock(DeliveryService.class);
        authorizationService = Mockito.mock(AuthorizationService.class);
        orderService = Mockito.mock(OrderService.class);
        authorizationService = Mockito.mock(AuthorizationService.class);
        deliveryController = new DeliveryController(deliveryService, orderService, authorizationService);

        // Default authorization behavior
        when(authorizationService.getUserRole(anyInt())).thenReturn("customer");
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenReturn(true);
        when(authorizationService.canUpdateDeliveryDetails(anyInt(), anyInt())).thenReturn(true);

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

        time = OffsetDateTime.now();
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
    public void testDeliveryOrderOrderIdStatusPut_Success() throws MicroserviceCommunicationException, OrderNotFoundException, IllegalOrderStatusException {
        when(authorizationService.canUpdateDeliveryDetails(anyInt(), anyInt())).thenReturn(true);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdStatusPut(123, 1, "DELIVERED");

        verify(authorizationService).canUpdateDeliveryDetails(1, 123);
        verify(orderService).setOrderStatus(123, 1, "DELIVERED");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdStatusPut_Forbidden() throws MicroserviceCommunicationException, OrderNotFoundException, IllegalOrderStatusException {
        when(authorizationService.canUpdateDeliveryDetails(anyInt(), anyInt())).thenReturn(false);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdStatusPut(123, 1, "DELIVERED");

        verify(authorizationService).canUpdateDeliveryDetails(1, 123);
        verify(orderService, never()).setOrderStatus(anyInt(), anyInt(), anyString());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdStatusPut_InternalServerErrorOnMicroserviceCommunicationException() throws MicroserviceCommunicationException, OrderNotFoundException, IllegalOrderStatusException {
        when(authorizationService.canUpdateDeliveryDetails(anyInt(), anyInt())).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdStatusPut(123, 1, "DELIVERED");

        verify(authorizationService).canUpdateDeliveryDetails(1, 123);
        verify(orderService, never()).setOrderStatus(anyInt(), anyInt(), anyString());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdStatusPut_BadRequestOnOrderNotFoundException() throws MicroserviceCommunicationException, OrderNotFoundException, IllegalOrderStatusException {
        when(authorizationService.canUpdateDeliveryDetails(anyInt(), anyInt())).thenReturn(true);
        doThrow(OrderNotFoundException.class).when(orderService).setOrderStatus(anyInt(), anyInt(), anyString());

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdStatusPut(123, 1, "DELIVERED");

        verify(authorizationService).canUpdateDeliveryDetails(1, 123);
        verify(orderService).setOrderStatus(123, 1, "DELIVERED");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdStatusPut_BadRequestOnIllegalOrderStatusException() throws MicroserviceCommunicationException, OrderNotFoundException, IllegalOrderStatusException {
        when(authorizationService.canUpdateDeliveryDetails(anyInt(), anyInt())).thenReturn(true);
        doThrow(IllegalOrderStatusException.class).when(orderService).setOrderStatus(anyInt(), anyInt(), anyString());

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdStatusPut(123, 1, "INVALID_STATUS");

        verify(authorizationService).canUpdateDeliveryDetails(1, 123);
        verify(orderService).setOrderStatus(123, 1, "INVALID_STATUS");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdStatusGet_Success() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenReturn(true);
        when(orderService.getOrderStatus(123)).thenReturn(Order.StatusEnum.DELIVERED);

        ResponseEntity<String> response = deliveryController.deliveryOrderOrderIdStatusGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        verify(orderService).getOrderStatus(123);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delivered", response.getBody());
    }

    @Test
    public void testDeliveryOrderOrderIdStatusGet_Forbidden() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenReturn(false);

        ResponseEntity<String> response = deliveryController.deliveryOrderOrderIdStatusGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        verify(orderService, never()).getOrderStatus(anyInt());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdStatusGet_InternalServerErrorOnMicroserviceCommunicationException() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<String> response = deliveryController.deliveryOrderOrderIdStatusGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdStatusGet_BadRequestOnOrderNotFoundException() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenReturn(true);
        when(orderService.getOrderStatus(123)).thenThrow(OrderNotFoundException.class);

        ResponseEntity<String> response = deliveryController.deliveryOrderOrderIdStatusGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        verify(orderService).getOrderStatus(123);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    public void testDeliveryOrderOrderIdReadyTimeGet_Success() throws Exception {
        when(deliveryService.getReadyTime(123L)).thenReturn(time);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdReadyTimeGet(123, 1);

        verify(deliveryService).getReadyTime(123L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(time, response.getBody());
    }

    @Test
    public void testDeliveryOrderOrderIdReadyTimeGet_Forbidden() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenReturn(false);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdReadyTimeGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        verify(deliveryService, never()).getReadyTime(anyLong());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdReadyTimeGet_InternalServerErrorOnMicroserviceCommunicationException() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdReadyTimeGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdReadyTimeGet_NotFound() throws Exception {
        when(deliveryService.getReadyTime(123L)).thenThrow(new OrderNotFoundException("Order not found"));

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdReadyTimeGet(123, 1);

        verify(deliveryService).getReadyTime(123L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdReadyTimeGet_NotFoundOnOrderNotFoundException() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenReturn(true);
        when(deliveryService.getReadyTime(123L)).thenThrow(OrderNotFoundException.class);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdReadyTimeGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        verify(deliveryService).getReadyTime(123L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdReadyTimeGet_BadRequestOnIllegalArgumentException() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenReturn(true);
        when(deliveryService.getReadyTime(123L)).thenThrow(IllegalArgumentException.class);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdReadyTimeGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        verify(deliveryService).getReadyTime(123L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdReadyTimePut_Success() throws Exception {
        doNothing().when(deliveryService).updateReadyTime(123L, time);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdReadyTimePut(123, 1, time);

        verify(deliveryService).updateReadyTime(123L, time);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdReadyTimePut_Forbidden() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canUpdateDeliveryDetails(anyInt(), anyInt())).thenReturn(false);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdReadyTimePut(123, 1, OffsetDateTime.now());

        verify(authorizationService).canUpdateDeliveryDetails(1, 123);
        verify(deliveryService, never()).updateReadyTime(anyLong(), any(OffsetDateTime.class));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdReadyTimePut_InternalServerErrorOnMicroserviceCommunicationException() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canUpdateDeliveryDetails(anyInt(), anyInt())).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdReadyTimePut(123, 1, OffsetDateTime.now());

        verify(authorizationService).canUpdateDeliveryDetails(1, 123);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdReadyTimePut_BAD_REQUEST() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canUpdateDeliveryDetails(anyInt(), anyInt())).thenThrow(IllegalArgumentException.class);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdReadyTimePut(123, 1, OffsetDateTime.now());

        verify(authorizationService).canUpdateDeliveryDetails(1, 123);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
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
        when(authorizationService.getUserRole(2)).thenReturn(AuthorizationService.ADMIN);
        ResponseEntity<Void> response = deliveryController.deliveryDefaultDeliveryZonePut(25, 2);
        verify(deliveryService).updateDefaultDeliveryZone(25);
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
    public void testDeliveryOrderOrderIdPickupTimeGet_Forbidden() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenReturn(false);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdPickupTimeGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        verify(deliveryService, never()).updateReadyTime(anyLong(), any(OffsetDateTime.class));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdPickupTimeGett_InternalServerErrorOnMicroserviceCommunicationException() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdPickupTimeGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
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
    public void testDeliveryOrderOrderIdPickupTimePut_Forbidden() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canUpdateDeliveryDetails(anyInt(), anyInt())).thenReturn(false);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdPickupTimePut(123, 1, OffsetDateTime.now());

        verify(authorizationService).canUpdateDeliveryDetails(1, 123);
        verify(deliveryService, never()).updateReadyTime(anyLong(), any(OffsetDateTime.class));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdPickupTimePut_InternalServerErrorOnMicroserviceCommunicationException() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canUpdateDeliveryDetails(anyInt(), anyInt())).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdPickupTimePut(123, 1, OffsetDateTime.now());

        verify(authorizationService).canUpdateDeliveryDetails(1, 123);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
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
    public void testDeliveryOrderOrderIdTodGet_Forbidden() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenReturn(false);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdTodGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        verify(deliveryService, never()).updateReadyTime(anyLong(), any(OffsetDateTime.class));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdTodGet_InternalServerErrorOnMicroserviceCommunicationException() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdTodGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
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
    public void testDeliveryOrderOrderIdTodPut_Forbidden() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canUpdateDeliveryDetails(anyInt(), anyInt())).thenReturn(false);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdTodPut(123, 1, OffsetDateTime.now());

        verify(authorizationService).canUpdateDeliveryDetails(1, 123);
        verify(deliveryService, never()).updateReadyTime(anyLong(), any(OffsetDateTime.class));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdTodPut_InternalServerErrorOnMicroserviceCommunicationException() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canUpdateDeliveryDetails(anyInt(), anyInt())).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<Void> response = deliveryController.deliveryOrderOrderIdTodPut(123, 1, OffsetDateTime.now());

        verify(authorizationService).canUpdateDeliveryDetails(1, 123);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
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
    public void testDeliveryOrderOrderIdEtaGet_Forbidden() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenReturn(false);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdEtaGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        verify(deliveryService, never()).updateReadyTime(anyLong(), any(OffsetDateTime.class));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdEtaGet_InternalServerErrorOnMicroserviceCommunicationException() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(anyInt(), anyInt())).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<OffsetDateTime> response = deliveryController.deliveryOrderOrderIdEtaGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
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
    public void testGetIssueNotFound() throws Exception {
        Mockito.when(authorizationService.canViewDeliveryDetails(any(), any())).thenReturn(true);
        Mockito.when(deliveryService.retrieveIssueOfDelivery(any())).thenReturn(null);

        ResponseEntity<Issue> response = deliveryController.deliveryOrderOrderIdIssueGet(1234, 4567);

        verify(deliveryService,times(1)).retrieveIssueOfDelivery(any());
        verify(authorizationService, times(1)).canViewDeliveryDetails(any(), any());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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
    public void getDefaultDeliveryZoneTest_Success() throws MicroserviceCommunicationException {
        int defaultDeliveryZone = 5;
        when(deliveryService.getDefaultDeliveryZone()).thenReturn(Long.valueOf(defaultDeliveryZone));

        ResponseEntity<Integer> response = deliveryController.deliveryDefaultDeliveryZoneGet(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void updateDefaultDeliveryZoneUnauthorizedTest() throws MicroserviceCommunicationException {
        when(authorizationService.getUserRole(2)).thenReturn(AuthorizationService.CUSTOMER);

        ResponseEntity<Void> response = deliveryController.deliveryDefaultDeliveryZonePut(25, 2);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void updateDefaultDeliveryZoneMiscommunicationTest() throws MicroserviceCommunicationException {
        when(authorizationService.getUserRole(5)).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<Void> response = deliveryController.deliveryDefaultDeliveryZonePut(25, 5);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdLocationGet_Success() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(deliveryService.getDeliveryIdByOrderId(123L)).thenReturn(456L);
        Location liveLocation = new Location(1.0, 2.0);
        when(deliveryService.calculateLiveLocation(456L)).thenReturn(liveLocation);

        ResponseEntity<Location> response = deliveryController.deliveryOrderOrderIdLocationGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        verify(deliveryService).getDeliveryIdByOrderId(123L);
        verify(deliveryService).calculateLiveLocation(456L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(liveLocation, response.getBody());
    }

    @Test
    public void testDeliveryOrderOrderIdLocationGet_Unauthorized() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(authorizationService.canViewDeliveryDetails(1, 123)).thenReturn(false);

        ResponseEntity<Location> response = deliveryController.deliveryOrderOrderIdLocationGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        verify(deliveryService, never()).getDeliveryIdByOrderId(anyLong());
        verify(deliveryService, never()).calculateLiveLocation(anyLong());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdLocationGet_InternalServerErrorOnMicroserviceCommunicationException() throws MicroserviceCommunicationException {
        when(authorizationService.getUserRole(anyInt())).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<Location> response = deliveryController.deliveryOrderOrderIdLocationGet(123, 1);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeliveryOrderOrderIdLocationGet_NotFoundOnOrderNotFoundException() throws MicroserviceCommunicationException, OrderNotFoundException {
        when(deliveryService.getDeliveryIdByOrderId(123L)).thenThrow(OrderNotFoundException.class);

        ResponseEntity<Location> response = deliveryController.deliveryOrderOrderIdLocationGet(123, 1);

        verify(authorizationService).canViewDeliveryDetails(1, 123);
        verify(deliveryService).getDeliveryIdByOrderId(123L);
        verify(deliveryService, never()).calculateLiveLocation(anyLong());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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
