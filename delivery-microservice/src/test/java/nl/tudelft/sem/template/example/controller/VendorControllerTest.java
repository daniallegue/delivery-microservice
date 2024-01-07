package nl.tudelft.sem.template.example.controller;


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
    private final VendorController vendorController = new VendorController(vendorService);

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

//    @Test
//    void getDeliveryZoneNotFoundTest() throws VendorNotFoundException {
//        Integer authorizationId = 1;
//        when(vendorService.getDeliveryZone(3L)).thenThrow(VendorNotFoundException.class);
//        when(vendorRepository.existsById(3L)).thenReturn(false);
//
//        ResponseEntity<Integer> response = vendorController.vendorDeliveryVendorIdDeliveryZoneGet(3, 1);
//        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
//    }


    @Test
    void updateDeliveryZoneSuccessfulTest() throws VendorNotFoundException, VendorHasNoCouriersException {
        when(vendorRepository.findById(2L)).thenReturn(Optional.ofNullable(vendor1));
        when(vendorRepository.existsById(2L)).thenReturn(true);
        when(vendorService.updateDeliveryZone(2L, 10L)).thenReturn(updated);

        ResponseEntity<Vendor> response = vendorController.vendorDeliveryVendorIdDeliveryZonePut(2, 10, 1);
        Vendor updatedVendor = response.getBody();
        assertEquals(vendor1.getId(), updatedVendor.getId());
        assertEquals(updatedVendor.getDeliveryZone(), 10L);
        assertEquals(updatedVendor.getAddress(), vendor1.getAddress());
        assertEquals(updatedVendor.getCouriers(), vendor1.getCouriers());
    }

}
