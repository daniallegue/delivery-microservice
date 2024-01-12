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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


public class VendorControllerTest {

    private final VendorRepository vendorRepository = Mockito.mock(VendorRepository.class);

    private final VendorService vendorService = Mockito.mock(VendorService.class);
    private final AuthorizationService authorizationService = Mockito.mock(AuthorizationService.class);
    private final VendorController vendorController = new VendorController(vendorService, authorizationService);

    public Vendor vendor1;
    public Vendor vendor2;
    public Vendor updated;
    public List<Long> couriers;

    @BeforeEach
    void setup() throws VendorNotFoundException {
        Location address = new Location(0.0,0.0);
        couriers = new ArrayList<>();
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
        assertEquals(5, response.getBody());
    }

    @Test
    void getDeliveryZoneNotFound() throws VendorNotFoundException {
        Integer authorizationId = 1;
        when(vendorService.getDeliveryZone(2L)).thenThrow(VendorNotFoundException.class);
        when(vendorRepository.existsById(2L)).thenReturn(false);

        ResponseEntity<Integer> response = vendorController.vendorDeliveryVendorIdDeliveryZoneGet(2, 1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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
        assertEquals(10L, updatedVendor.getDeliveryZone());
        assertEquals(vendor1.getAddress(), updatedVendor.getAddress());
        assertEquals(vendor1.getCouriers(), updatedVendor.getCouriers());
    }

    @Test
    void updateDeliveryZoneUnauthorizedTest() throws VendorNotFoundException, VendorHasNoCouriersException, MicroserviceCommunicationException {
        when(vendorRepository.findById(2L)).thenReturn(Optional.ofNullable(vendor1));
        when(vendorRepository.existsById(2L)).thenReturn(true);
        when(vendorService.updateDeliveryZone(2L, 10L)).thenReturn(updated);
        when(authorizationService.getUserRole(1L)).thenReturn(authorizationService.CUSTOMER);
        when(authorizationService.cannotUpdateVendorDeliveryZone(1L)).thenReturn(true);

        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdDeliveryZonePut(2, 10, 1);
        assertEquals( HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void updateDeliveryZoneNotFoundTest() throws VendorNotFoundException, VendorHasNoCouriersException, MicroserviceCommunicationException {
        when(vendorRepository.existsById(2L)).thenReturn(false);
        when(vendorService.updateDeliveryZone(2L, 10L)).thenThrow(VendorNotFoundException.class);
        when(authorizationService.getUserRole(1L)).thenReturn(authorizationService.ADMIN);

        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdDeliveryZonePut(2, 10, 1);
        assertEquals( HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateDeliveryZoneNoCouriersTest() throws VendorNotFoundException, VendorHasNoCouriersException, MicroserviceCommunicationException {
        when(vendorRepository.findById(2L)).thenReturn(Optional.ofNullable(vendor1));
        when(vendorRepository.existsById(2L)).thenReturn(true);
        when(vendorService.updateDeliveryZone(2L, 10L)).thenThrow(VendorHasNoCouriersException.class);
        when(authorizationService.getUserRole(1L)).thenReturn(authorizationService.ADMIN);

        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdDeliveryZonePut(2, 10, 1);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateDeliveryZoneMiscommunicationTest() throws MicroserviceCommunicationException {
        when(vendorRepository.findById(2L)).thenReturn(Optional.ofNullable(vendor1));
        when(vendorRepository.existsById(2L)).thenReturn(true);
        when(authorizationService.cannotUpdateVendorDeliveryZone(1L)).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdDeliveryZonePut(2, 10, 1);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void assignCourierVendorDoesNotExistTest() throws VendorNotFoundException, MicroserviceCommunicationException {
        when(vendorService.assignCourierToVendor(2L, 10L)).thenThrow(VendorNotFoundException.class);
        when(authorizationService.getUserRole(1L)).thenReturn(authorizationService.ADMIN);

        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdAssignCourierIdPut(2, 10, 1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    @Test
    void assignCourierUnauthorizedTest() throws MicroserviceCommunicationException {
        when(authorizationService.getUserRole(1L)).thenReturn("customer");

        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdAssignCourierIdPut(2, 10, 1);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void assignCourierTest() throws MicroserviceCommunicationException, VendorNotFoundException {
        when(authorizationService.getUserRole(1L)).thenReturn(authorizationService.ADMIN);
        when(vendorService.assignCourierToVendor(2L, 5L)).thenReturn(vendor1);
        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdAssignCourierIdPut(2, 5, 1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void assignCourierMiscommunicationTest() throws MicroserviceCommunicationException {
        when(authorizationService.getUserRole(1L)).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdAssignCourierIdPut(2, 10, 1);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getCouriersVendorDoesNotExistTest() throws VendorNotFoundException, MicroserviceCommunicationException {
        when(vendorService.getAssignedCouriers(2L)).thenThrow(VendorNotFoundException.class);
        when(authorizationService.getUserRole(1L)).thenReturn(authorizationService.ADMIN);

        ResponseEntity<List<Long>> response = vendorController.vendorDeliveryVendorIdCouriersGet(2, 1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCouriersUnauthorizedTest() throws MicroserviceCommunicationException {
        when(authorizationService.getUserRole(1L)).thenReturn("customer");

        ResponseEntity<List<Long>> response = vendorController.vendorDeliveryVendorIdCouriersGet(2, 1);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getCouriersTest() throws MicroserviceCommunicationException, VendorNotFoundException {
        when(authorizationService.getUserRole(1L)).thenReturn(authorizationService.VENDOR);
        when(vendorService.getAssignedCouriers(2L)).thenReturn(couriers);

        ResponseEntity<List<Long>> response = vendorController.vendorDeliveryVendorIdCouriersGet(2, 1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getCouriersMiscommunicationTest() throws MicroserviceCommunicationException {
        when(authorizationService.getUserRole(1L)).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<List<Long>> response = vendorController.vendorDeliveryVendorIdCouriersGet(2, 1);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
