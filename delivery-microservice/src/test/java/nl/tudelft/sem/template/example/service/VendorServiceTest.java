package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Vendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class VendorServiceTest {

    private VendorRepository vendorRepository;

    private ConfigurationProperties configurationProperties;

    private VendorService vendorService;


    Vendor vendor;

    @BeforeEach
    void setup(){
        vendorRepository = Mockito.mock(VendorRepository.class);
        configurationProperties = new ConfigurationProperties();
        vendorService = new VendorService(vendorRepository, configurationProperties);

        //TO DO: change address with the (mocked) one from other microservices
        Location address = new Location(0.0,0.0);
        vendor = new Vendor(1L, configurationProperties.getDefaultDeliveryZone(), address, new ArrayList<>());
        Vendor vendor1 = new Vendor(2L, 5L, address, new ArrayList<>());
        when(vendorRepository.findById(1L)).thenReturn(Optional.ofNullable(vendor1));
        when(vendorRepository.existsById(1L)).thenReturn(true);
        when(vendorRepository.existsById(2L)).thenReturn(false);
    }

    @Test
    void testFindVendorOrCreateWhenVendorExists() {
        Long vendorId = 1L;
        when(vendorRepository.existsById(vendorId)).thenReturn(true);
        when(vendorRepository.findById(vendorId)).thenReturn(Optional.ofNullable(vendor));

        Vendor resultingVendor = vendorService.findVendorOrCreate(vendorId);

        verify(vendorRepository, never()).save(any());
        assertEquals(vendor, resultingVendor);
    }

    @Test
    void testFindVendorOrCreateWithNewVendor() {
        Long vendorId = 1L;

        when(vendorRepository.existsById(vendorId)).thenReturn(false);
        when(vendorRepository.findById(vendorId)).thenReturn(Optional.ofNullable(vendor));

        Vendor resultingVendor = vendorService.findVendorOrCreate(vendorId);

        verify(vendorRepository, times(1)).save(any());
        assertNotNull(resultingVendor);
    }

    @Test
    void getDeliveryZoneCorrectTest() throws VendorNotFoundException {
        Long vendorId = 1L;
        Long result = 5L;
        assertEquals(result, vendorService.getDeliveryZone(1L));
        assertDoesNotThrow(() -> vendorService.getDeliveryZone(vendorId));

    }

    @Test
    void getDeliveryZoneInvalidTest() throws VendorNotFoundException {
        Long vendorId = 2L;
        assertThrows(VendorNotFoundException.class, () -> vendorService.getDeliveryZone(vendorId));
    }

}
