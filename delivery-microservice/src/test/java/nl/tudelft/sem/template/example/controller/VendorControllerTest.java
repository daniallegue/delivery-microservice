package nl.tudelft.sem.template.example.controller;


import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.exception.VendorHasNoCouriersException;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.example.service.VendorService;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Vendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


public class VendorControllerTest {

    private final VendorRepository vendorRepository = Mockito.mock(VendorRepository.class);

    private final VendorService vendorService = Mockito.mock(VendorService.class);
    private final AuthorizationService authorizationService = Mockito.mock(AuthorizationService.class);
    private final VendorController vendorController = new VendorController(vendorService, authorizationService);

    public Vendor vendor1;
    public Vendor vendor2;
    public Vendor updated;

    @BeforeEach
    void setup() throws VendorNotFoundException {
        Location address = new Location(0.0,0.0);
        List<Long> couriers = new ArrayList<>();
        couriers.add(1L);
        vendor1 = new Vendor(2L, 5L, address, couriers);
        vendor2 = new Vendor(3L, 7L, address, couriers);
        updated = new  Vendor(2L, 10L, address, couriers);
    }

    @Test
    void getDeliveryZoneSuccessfulTest() throws VendorNotFoundException {
        Integer authorizationId = 1;
        when(vendorService.getDeliveryZone(2L)).thenReturn(5L);
        when(vendorRepository.existsById(2L)).thenReturn(true);

        ResponseEntity<Integer> response = vendorController.vendorDeliveryVendorIdDeliveryZoneGet(2, 1);
        assertEquals(response.getBody(), 5);
    }

    @Test
    void getDeliveryZoneNotFound() throws VendorNotFoundException {
        Integer authorizationId = 1;
        when(vendorService.getDeliveryZone(2L)).thenThrow(VendorNotFoundException.class);
        when(vendorRepository.existsById(2L)).thenReturn(false);

        ResponseEntity<Integer> response = vendorController.vendorDeliveryVendorIdDeliveryZoneGet(2, 1);
        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }


    @Test
    void updateDeliveryZoneSuccessfulTest() throws VendorNotFoundException, VendorHasNoCouriersException, MicroserviceCommunicationException {
        when(vendorRepository.findById(2L)).thenReturn(Optional.ofNullable(vendor1));
        when(vendorRepository.existsById(2L)).thenReturn(true);
        when(vendorService.updateDeliveryZone(2L, 10L)).thenReturn(updated);
        when(authorizationService.getUserRole(1L)).thenReturn(authorizationService.VENDOR);

        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdDeliveryZonePut(2, 10, 1);
        Vendor updatedVendor = response.getBody();
        assertEquals(vendor1.getId(), updatedVendor.getId());
        assertEquals(updatedVendor.getDeliveryZone(), 10L);
        assertEquals(updatedVendor.getAddress(), vendor1.getAddress());
        assertEquals(updatedVendor.getCouriers(), vendor1.getCouriers());
    }

    @Test
    void updateDeliveryZoneUnauthorizedTest() throws VendorNotFoundException, VendorHasNoCouriersException, MicroserviceCommunicationException {
        when(vendorRepository.findById(2L)).thenReturn(Optional.ofNullable(vendor1));
        when(vendorRepository.existsById(2L)).thenReturn(true);
        when(vendorService.updateDeliveryZone(2L, 10L)).thenReturn(updated);
        when(authorizationService.getUserRole(1L)).thenReturn(authorizationService.CUSTOMER);

        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdDeliveryZonePut(2, 10, 1);
        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void updateDeliveryZoneNotFoundTest() throws VendorNotFoundException, VendorHasNoCouriersException, MicroserviceCommunicationException {
        when(vendorRepository.existsById(2L)).thenReturn(false);
        when(vendorService.updateDeliveryZone(2L, 10L)).thenThrow(VendorNotFoundException.class);
        when(authorizationService.getUserRole(1L)).thenReturn(authorizationService.ADMIN);

        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdDeliveryZonePut(2, 10, 1);
        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    void updateDeliveryZoneNoCouriersTest() throws VendorNotFoundException, VendorHasNoCouriersException, MicroserviceCommunicationException {
        when(vendorRepository.findById(2L)).thenReturn(Optional.ofNullable(vendor1));
        when(vendorRepository.existsById(2L)).thenReturn(true);
        when(vendorService.updateDeliveryZone(2L, 10L)).thenThrow(VendorHasNoCouriersException.class);
        when(authorizationService.getUserRole(1L)).thenReturn(authorizationService.ADMIN);

        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdDeliveryZonePut(2, 10, 1);
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateDeliveryZoneMiscommunicationTest() throws MicroserviceCommunicationException {
        when(vendorRepository.findById(2L)).thenReturn(Optional.ofNullable(vendor1));
        when(vendorRepository.existsById(2L)).thenReturn(true);
        when(authorizationService.getUserRole(1L)).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdDeliveryZonePut(2, 10, 1);
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }






}
