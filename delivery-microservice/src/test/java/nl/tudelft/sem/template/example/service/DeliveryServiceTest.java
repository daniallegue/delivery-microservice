package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.exception.*;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.*;
import org.assertj.core.api.Assertions;
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
        configurationProperties.setDefaultDeliveryZone(10L);

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

        this.deliveryService = new DeliveryService(deliveryRepository, orderRepository, vendorRepository, vendorService, configurationProperties);


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
    void getDefaultDeliveryZone() {
        Long defaultDeliveryZone = deliveryService.getDefaultDeliveryZone();
        assertEquals(10L, defaultDeliveryZone);
    }

    @Test
    void setDefaultDeliveryZone() {
        Long defaultDeliveryZone = deliveryService.getDefaultDeliveryZone();
        assertEquals(10L, defaultDeliveryZone);

        Long newZone = 45L;
        deliveryService.updateDefaultDeliveryZone(newZone.intValue());
        assertEquals(newZone, deliveryService.getDefaultDeliveryZone());
    }

    @Test
    void retrieveIssueOfDeliveryWorks() throws DeliveryNotFoundException {
        Issue deliveryIssue = new Issue("traffic", "There was an accident on the way, so the order will be delivered later");
        Delivery delivery = new Delivery();
        delivery.setId(1L);
        delivery.setIssue(deliveryIssue);
        Mockito.when(deliveryRepository.findDeliveryByOrder_OrderId(anyLong())).thenReturn(delivery);
        Issue issue = deliveryService.retrieveIssueOfDelivery(1);
        assertNotNull(issue);
    }

    @Test
    void retrieveIssueOfDeliveryThrowsException() {
        Mockito.when(deliveryRepository.findDeliveryByOrder_OrderId(any())).thenReturn(null);
        Assertions.assertThatThrownBy(() -> deliveryService.retrieveIssueOfDelivery(6))
                .isInstanceOf(DeliveryNotFoundException.class);
    }

    @Test
    void getCourierFromOrderSuccessfulTest() throws OrderNotFoundException, CourierNotFoundException {
        Vendor vendor1 = new Vendor(5L, 30L, null, new ArrayList<>());
        Order order1 = new Order(1L, 4567L, vendor1, Order.StatusEnum.PENDING, new Location(2.0, 3.0));
        Delivery delivery = new Delivery();
        delivery.setOrder(order1);
        delivery.setCourierId(2L);
        when(orderRepository.existsById(1L)).thenReturn(true);
        when(deliveryRepository.findDeliveryByOrder_OrderId((1L))).thenReturn(delivery);

        Long id = deliveryService.getCourierFromOrder(Math.toIntExact(1L));
        assertEquals(2L, id);
    }

    @Test
    void getCourierFromOrderNotFoundTest() throws OrderNotFoundException, CourierNotFoundException {
        when(orderRepository.existsById(1L)).thenReturn(false);
        assertThrows(OrderNotFoundException.class, () -> deliveryService.getCourierFromOrder((int) 1));
    }

    @Test
    void getCourierFromOrderNoCourierTest() throws OrderNotFoundException, CourierNotFoundException {
        Vendor vendor1 = new Vendor(5L, 30L, null, new ArrayList<>());
        Order order1 = new Order(1L, 4567L, vendor1, Order.StatusEnum.PENDING, new Location(2.0, 3.0));
        Delivery delivery = new Delivery();
        delivery.setOrder(order1);
        when(orderRepository.existsById(1L)).thenReturn(true);
        when(deliveryRepository.findDeliveryByOrder_OrderId((1L))).thenReturn(delivery);

        assertThrows(CourierNotFoundException.class, () -> deliveryService.getCourierFromOrder((int) 1L));
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
    void testUpdateReadyTimeSuccessTimeIsNull() throws OrderNotFoundException {
        OffsetDateTime newReadyTime = OffsetDateTime.now();

        Delivery mockDelivery = new Delivery();
        mockDelivery.setId(1L);
        mockDelivery.setOrder(new Order());

        when(deliveryRepository.findDeliveryByOrder_OrderId(anyLong())).thenReturn(mockDelivery);
        when(deliveryRepository.save(any())).thenReturn(mockDelivery);

        deliveryService.updateReadyTime(1L, newReadyTime);

        verify(deliveryRepository).save(any());

        assertEquals(newReadyTime, mockDelivery.getTime().getReadyTime());
    }

    @Test
    void testPickUpTimeSuccessTimeIsNull() throws OrderNotFoundException {
        OffsetDateTime newPickUpTime = OffsetDateTime.now();

        Delivery mockDelivery = new Delivery();
        mockDelivery.setId(1L);
        mockDelivery.setOrder(new Order());

        when(deliveryRepository.findDeliveryByOrder_OrderId(anyLong())).thenReturn(mockDelivery);
        when(deliveryRepository.save(any())).thenReturn(mockDelivery);

        deliveryService.updatePickupTime(1L, newPickUpTime);

        verify(deliveryRepository).save(any());

        assertEquals(newPickUpTime, mockDelivery.getTime().getPickUpTime());
    }

    @Test
    void testDeliveredTimeSuccessTimeIsNull() throws OrderNotFoundException {
        OffsetDateTime newDeliveredTime = OffsetDateTime.now();

        Delivery mockDelivery = new Delivery();
        mockDelivery.setId(1L);
        mockDelivery.setOrder(new Order());

        when(deliveryRepository.findDeliveryByOrder_OrderId(anyLong())).thenReturn(mockDelivery);
        when(deliveryRepository.save(any())).thenReturn(mockDelivery);

        deliveryService.updateDeliveredTime(1L, newDeliveredTime);

        verify(deliveryRepository).save(any());

        assertEquals(newDeliveredTime, mockDelivery.getTime().getDeliveredTime());
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
    @Test
    void testAddIssueToDeliverySuccess() throws DeliveryNotFoundException {
        Integer orderId = 123;
        Issue issue = new Issue("type", "description");

        Delivery mockDelivery = new Delivery();
        mockDelivery.setId(1L); // Set ID or any necessary fields
        mockDelivery.setOrder(new Order()); // Set Order or any necessary fields

        when(deliveryRepository.findDeliveryByOrder_OrderId(anyLong())).thenReturn(mockDelivery);

        assertDoesNotThrow(() -> deliveryService.addIssueToDelivery(orderId, issue));

        Mockito.verify(deliveryRepository).findDeliveryByOrder_OrderId(Long.valueOf(orderId));
        assertEquals(issue, mockDelivery.getIssue());
        Mockito.verify(deliveryRepository).save(mockDelivery);
    }

    @Test
    void testAddIssueToDeliveryDeliveryNotFound() {
        Integer orderId = 123;
        Issue issue = new Issue("type", "description");

        when(deliveryRepository.findDeliveryByOrder_OrderId(anyLong())).thenReturn(null);
        DeliveryNotFoundException exception = assertThrows(DeliveryNotFoundException.class,
                () -> deliveryService.addIssueToDelivery(orderId, issue));

        Mockito.verify(deliveryRepository).findDeliveryByOrder_OrderId(Long.valueOf(orderId));

        assertEquals("Delivery with order id " + orderId + " was not found", exception.getMessage());
        Mockito.verifyNoMoreInteractions(deliveryRepository);
    }

}
