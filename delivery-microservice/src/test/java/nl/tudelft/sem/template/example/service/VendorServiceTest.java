package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.external.UsersMicroservice;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Vendor;
import org.assertj.core.api.Assertions;
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

    private UsersMicroservice usersMicroservice;

    private VendorService vendorService;


    Vendor vendor;

    @BeforeEach
    void setup(){
        vendorRepository = Mockito.mock(VendorRepository.class);
        configurationProperties = new ConfigurationProperties();
        usersMicroservice = Mockito.mock(UsersMicroservice.class);
        vendorService = new VendorService(vendorRepository, configurationProperties, usersMicroservice);

        //TO DO: change address with the (mocked) one from other microservices
        Location address = new Location(0.0,0.0);
        vendor = new Vendor(1L, configurationProperties.getDefaultDeliveryZone(), address, new ArrayList<>());
    }

    @Test
    void testFindVendorOrCreateWhenVendorExists() throws MicroserviceCommunicationException {
        Long vendorId = 1L;
        when(vendorRepository.existsById(vendorId)).thenReturn(true);
        when(vendorRepository.findById(vendorId)).thenReturn(Optional.ofNullable(vendor));
        Vendor resultingVendor = vendorService.findVendorOrCreate(vendorId);

        verify(vendorRepository, never()).save(any());
        assertEquals(vendor, resultingVendor);
    }

    @Test
    void testFindVendorOrCreateWithNewVendor() throws MicroserviceCommunicationException {
        Long vendorId = 1L;

        when(vendorRepository.existsById(vendorId)).thenReturn(false);
        when(vendorRepository.findById(vendorId)).thenReturn(Optional.ofNullable(vendor));
        when(usersMicroservice.getVendorLocation(anyLong())).thenReturn(Optional.of(new Location(4.0, 5.0)));

        Vendor resultingVendor = vendorService.findVendorOrCreate(vendorId);

        verify(vendorRepository, times(1)).save(any());
        assertNotNull(resultingVendor);
    }

    @Test
    void testFindVendorOrCreateWithNewVendorFaultyMicroserviceCommunication(){
        Long vendorId = 1L;

        when(vendorRepository.existsById(vendorId)).thenReturn(false);
        when(vendorRepository.findById(vendorId)).thenReturn(Optional.ofNullable(vendor));
        when(usersMicroservice.getVendorLocation(anyLong())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> vendorService.findVendorOrCreate(vendorId))
                .isInstanceOf(MicroserviceCommunicationException.class);

        verify(vendorRepository, never()).save(any());
    }
}
