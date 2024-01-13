package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.Application;
import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.external.UsersMicroservice;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Vendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = Application.class)
public class VendorServiceTestRealRepository {
    private final UsersMicroservice usersMicroservice;

    private final VendorRepository vendorRepository;

    private final VendorService vendorService;
    private final CourierService courierService;

    private final ConfigurationProperties configurationProperties;

    @Autowired
    public VendorServiceTestRealRepository(VendorRepository vendorRepository, ConfigurationProperties configurationProperties, CourierService courierService){
        this.vendorRepository = vendorRepository;
        this.usersMicroservice = Mockito.mock(UsersMicroservice.class);
        this.configurationProperties = configurationProperties;
        this.courierService = courierService;
        this.vendorService = new VendorService(vendorRepository, configurationProperties, usersMicroservice, courierService);
    }

    @BeforeEach
    void setup(){
        Vendor vendor1 = new Vendor(4L, 30L, new Location(3.0, 4.0), new ArrayList<>());
        Vendor vendor2 = new Vendor(5L, 30L, null, null);

        vendorRepository.save(vendor1);
        vendorRepository.save(vendor2);
    }

    @Test
    void testVendorWithIdDoesNotExist() throws MicroserviceCommunicationException {
        when(usersMicroservice.getVendorLocation(any())).thenReturn(Optional.of(new Location(2.0, 2.0)));
        Vendor vendor = vendorService.findVendorOrCreate(100L);

        assertThat(vendor.getId()).isEqualTo(100L);
        assertThat(vendor.getAddress()).isEqualTo(new Location(2.0, 2.0));
        assertThat(vendor.getCouriers().size()).isEqualTo(0);
        assertThat(vendor.getDeliveryZone()).isEqualTo(configurationProperties.getDefaultDeliveryZone());
    }

}