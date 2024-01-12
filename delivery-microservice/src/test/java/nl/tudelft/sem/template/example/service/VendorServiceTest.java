package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.VendorHasNoCouriersException;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class VendorServiceTest {

    private VendorRepository vendorRepository;

    private ConfigurationProperties configurationProperties;

    private UsersMicroservice usersMicroservice;

    private VendorService vendorService;
    private CourierService courierService;


    Vendor vendor;

    @BeforeEach
    void setup(){
        vendorRepository = Mockito.mock(VendorRepository.class);
        configurationProperties = new ConfigurationProperties();
        usersMicroservice = Mockito.mock(UsersMicroservice.class);
        courierService = Mockito.mock(CourierService.class);
        vendorService = new VendorService(vendorRepository, configurationProperties, usersMicroservice, courierService);

        //TO DO: change address with the (mocked) one from other microservices
        Location address = new Location(0.0,0.0);
        vendor = new Vendor(1L, configurationProperties.getDefaultDeliveryZone(), address, new ArrayList<>());
        Vendor vendor1 = new Vendor(1L, 5L, address, null);
        List<Long> couriers = new ArrayList<>();
        couriers.add(2L);
        Vendor vendor2 = new Vendor(3L, 7L, address, couriers);
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor1));
        when(vendorRepository.findById(3L)).thenReturn(Optional.of(vendor2));
        when(vendorRepository.existsById(1L)).thenReturn(true);
        when(vendorRepository.existsById(3L)).thenReturn(true);
        when(vendorRepository.existsById(2L)).thenReturn(false);
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

    @Test
    void updateDeliveryZoneInvalidTest() throws VendorNotFoundException {
        Long vendorId = 2L;
        Long newZone = 10L;
        assertThrows(VendorNotFoundException.class, () -> vendorService.updateDeliveryZone(vendorId, newZone));
    }

    @Test
    void updateDeliveryZoneCorrectTest() throws VendorNotFoundException, VendorHasNoCouriersException {
        Long newZone = 10L;
        Location address = new Location(0.0,0.0);
        Vendor newVendor = new Vendor(3L, 10L, address, new ArrayList<>());

        Vendor updated = vendorService.updateDeliveryZone(3L, newZone);
        assertEquals(newVendor.getDeliveryZone(), updated.getDeliveryZone());
        assertEquals(address, updated.getAddress());
    }

    @Test
    void updateDeliveryZoneNoCouriersTest() throws VendorNotFoundException, VendorHasNoCouriersException {
        Long vendorId = 1L;
        assertThrows(VendorHasNoCouriersException.class, () -> vendorService.updateDeliveryZone(vendorId, 30L));
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

    @Test
    void assignCourierTest() throws VendorNotFoundException, CourierNotFoundException {
        Long vendorId = 3L;
        when(courierService.doesCourierExist(2L)).thenReturn(true);
        when(courierService.doesCourierExist(6L)).thenReturn(true);
        Vendor updated = vendorService.assignCourierToVendor(3L, 6L);
        List<Long> couriers = new ArrayList<>();
        couriers.add(2L);
        couriers.add(6L);
        assertEquals(couriers, updated.getCouriers());
        assertDoesNotThrow(() -> vendorService.getAssignedCouriers(vendorId));
        verify(vendorRepository, times(1)).save(any());
    }

    @Test
    void assignCouriersInvalidTest() throws VendorNotFoundException {
        assertThrows(VendorNotFoundException.class, () -> vendorService.assignCourierToVendor(2L, 5L));
    }

    @Test
    void getCouriersTest() throws VendorNotFoundException {
        Long vendorId = 3L;
        List<Long> updated = vendorService.getAssignedCouriers(3L);
        List<Long> couriers = new ArrayList<>();
        couriers.add(2L);
        assertEquals(couriers, updated);
        assertDoesNotThrow(() -> vendorService.getAssignedCouriers(vendorId));
    }

    @Test
    void getCouriersInvalidTest() throws VendorNotFoundException {
        assertThrows(VendorNotFoundException.class, () -> vendorService.getAssignedCouriers(2L));
    }
}
