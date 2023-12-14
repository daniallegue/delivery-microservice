package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
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
}
