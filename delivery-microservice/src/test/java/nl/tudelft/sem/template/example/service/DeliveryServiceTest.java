package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.exception.OrderAlreadyExistsException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class DeliveryServiceTest {


    private DeliveryRepository deliveryRepository;

    private OrderRepository orderRepository;

    private VendorRepository vendorRepository;

    private VendorService vendorService;

    private DeliveryService deliveryService;

    DeliveryPostRequest dummyDeliveryPostRequest;

    ConfigurationProperties configurationProperties;

    Vendor vendor;

    private Long orderId;
    private Time mockTime;
    private Delivery mockDelivery;

    @BeforeEach
    void setup(){
        configurationProperties = new ConfigurationProperties();

        dummyDeliveryPostRequest =  new DeliveryPostRequest();
        dummyDeliveryPostRequest.setVendorId(1);
        dummyDeliveryPostRequest.setOrderId(123);
        dummyDeliveryPostRequest.setCustomerId(456);
        dummyDeliveryPostRequest.setDestination(new Location(4.0, 5.0));

        vendor =  new Vendor(1L, configurationProperties.getDefaultDeliveryZone(), new Location(1.0,2.0), new ArrayList<>());

        deliveryRepository= Mockito.mock(DeliveryRepository.class);
        vendorRepository = Mockito.mock(VendorRepository.class);
        orderRepository = Mockito.mock(OrderRepository.class);
        vendorService = Mockito.mock(VendorService.class);

        deliveryService = new DeliveryService(deliveryRepository, orderRepository, vendorRepository, vendorService);

        orderId = 123L;
        mockTime = new Time();
        mockDelivery = new Delivery();
        mockDelivery.setTime(mockTime);
    }

    @Test
    void testCreateDeliveryWhenVendorNotFound() throws Exception {
        when(vendorService.findVendorOrCreate(anyLong())).thenReturn(null);
        assertThrows(VendorNotFoundException.class, () -> deliveryService.createDelivery(dummyDeliveryPostRequest));
    }

    @Test
    void testCreateDeliveryWhenOrderAlreadyExists() throws Exception {
        when(vendorService.findVendorOrCreate(anyLong())).thenReturn(vendor);
        when(orderRepository.existsById(123L)).thenReturn(true);

        assertThrows(OrderAlreadyExistsException.class, () -> deliveryService.createDelivery(dummyDeliveryPostRequest));
    }

    @Test
    void testCreateDelivery() throws Exception {
        Mockito.when(vendorService.findVendorOrCreate(anyLong())).thenReturn(vendor);
        Mockito.when(orderRepository.existsById(123L)).thenReturn(false);
        Mockito.when(vendorRepository.save(vendor)).thenReturn(vendor);
        Mockito.when(deliveryRepository.save(any())).thenReturn(new Delivery());
        Delivery result = deliveryService.createDelivery(dummyDeliveryPostRequest);

        assertNotNull(result);
    }

    @Test
    void testGetReadyTimeSuccess() throws OrderNotFoundException {
        OffsetDateTime readyTime = OffsetDateTime.now();
        mockTime.setReadyTime(readyTime);
        mockDelivery.setTime(mockTime);

        when(deliveryRepository.findDeliveryByOrder_OrderId(orderId)).thenReturn(mockDelivery);

        OffsetDateTime result = deliveryService.getReadyTime(orderId);

        assertEquals(readyTime, result);
    }

    @Test
    void testGetReadyTimeOrderNotFound() {
        when(deliveryRepository.findDeliveryByOrder_OrderId(orderId)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, () -> deliveryService.getReadyTime(orderId));
    }

    @Test
    void testUpdateReadyTimeSuccess() throws OrderNotFoundException {
        OffsetDateTime newReadyTime = OffsetDateTime.now().plusHours(1);
        mockDelivery.setTime(mockTime);

        when(deliveryRepository.findDeliveryByOrder_OrderId(orderId)).thenReturn(mockDelivery);

        deliveryService.updateReadyTime(orderId, newReadyTime);

        verify(deliveryRepository).save(mockDelivery);
        assertEquals(newReadyTime, mockDelivery.getTime().getReadyTime());
    }

    @Test
    void testUpdateReadyTimeOrderNotFound() {
        OffsetDateTime newReadyTime = OffsetDateTime.now().plusHours(1);

        when(deliveryRepository.findDeliveryByOrder_OrderId(orderId)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, () -> deliveryService.updateReadyTime(orderId, newReadyTime));
    }

    @Test
    void testGetPickupTimeSuccess() throws OrderNotFoundException {
        OffsetDateTime pickupTime = OffsetDateTime.now();
        mockTime.setPickUpTime(pickupTime);
        mockDelivery.setTime(mockTime);

        when(deliveryRepository.findDeliveryByOrder_OrderId(orderId)).thenReturn(mockDelivery);

        OffsetDateTime result = deliveryService.getPickupTime(orderId);

        assertEquals(pickupTime, result);
    }

    @Test
    void testGetPickupTimeOrderNotFound() {
        when(deliveryRepository.findDeliveryByOrder_OrderId(orderId)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, () -> deliveryService.getPickupTime(orderId));
    }

    @Test
    void testUpdatePickupTimeSuccess() throws OrderNotFoundException {
        OffsetDateTime newPickUpTime = OffsetDateTime.now().plusHours(1);
        mockDelivery.setTime(mockTime);

        when(deliveryRepository.findDeliveryByOrder_OrderId(orderId)).thenReturn(mockDelivery);

        deliveryService.updatePickupTime(orderId, newPickUpTime);

        verify(deliveryRepository).save(mockDelivery);
        assertEquals(newPickUpTime, mockDelivery.getTime().getPickUpTime());
    }

    @Test
    void testUpdatePickupTimeOrderNotFound() {
        OffsetDateTime newPickUpTime = OffsetDateTime.now().plusHours(1);

        when(deliveryRepository.findDeliveryByOrder_OrderId(orderId)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, () -> deliveryService.updatePickupTime(orderId, newPickUpTime));
    }

    @Test
    void testGetDeliveredTimeSuccess() throws OrderNotFoundException {
        OffsetDateTime deliveredTime = OffsetDateTime.now();
        mockTime.setDeliveredTime(deliveredTime);
        mockDelivery.setTime(mockTime);

        when(deliveryRepository.findDeliveryByOrder_OrderId(orderId)).thenReturn(mockDelivery);

        OffsetDateTime result = deliveryService.getDeliveredTime(orderId);

        assertEquals(deliveredTime, result);
    }

    @Test
    void testGetDeliveredTimeOrderNotFound() {
        when(deliveryRepository.findDeliveryByOrder_OrderId(orderId)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, () -> deliveryService.getDeliveredTime(orderId));
    }

    @Test
    void testUpdateDeliveredTimeSuccess() throws OrderNotFoundException {
        OffsetDateTime newDeliveredTime = OffsetDateTime.now().plusHours(2);
        mockDelivery.setTime(mockTime);

        when(deliveryRepository.findDeliveryByOrder_OrderId(orderId)).thenReturn(mockDelivery);

        deliveryService.updateDeliveredTime(orderId, newDeliveredTime);

        verify(deliveryRepository).save(mockDelivery);
        assertEquals(newDeliveredTime, mockDelivery.getTime().getDeliveredTime());
    }

    @Test
    void testUpdateDeliveredTimeOrderNotFound() {
        OffsetDateTime newDeliveredTime = OffsetDateTime.now().plusHours(2);

        when(deliveryRepository.findDeliveryByOrder_OrderId(orderId)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, () -> deliveryService.updateDeliveredTime(orderId, newDeliveredTime));
    }

}
