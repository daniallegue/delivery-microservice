package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.exception.OrderAlreadyExistsException;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
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



}
