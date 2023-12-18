package nl.tudelft.sem.template.example.service;

import java.util.ArrayList;
import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VendorService {

    VendorRepository vendorRepository;
    ConfigurationProperties configurationProperties;

    /**
     * Constructor for the Service allowing dependency injection.
     *
     * @param vendorRepository The JPA repository holding the Vendor entities.
     * @param configurationProperties The configurations holding the delivery zone.
     */
    @Autowired
    VendorService(VendorRepository vendorRepository, ConfigurationProperties configurationProperties) {
        this.vendorRepository = vendorRepository;
        this.configurationProperties = configurationProperties;
    }

    /**
     * Retrieves a vendor from the repository if one exists with the given
     * vendorId, or creates a new one by making a call to the endpoint of
     * the Users microservice.
     *
     * @param vendorId The id of the vendor.
     * @return The vendor with the given id.
     */
    public Vendor findVendorOrCreate(Long vendorId) {
        if (!vendorRepository.existsById(vendorId)) {
            Vendor newVendor = new Vendor();
            newVendor.setId(vendorId);
            newVendor.setDeliveryZone(configurationProperties.getDefaultDeliveryZone());
            newVendor.setCouriers(new ArrayList<>());

            // TO DO: make use of mocking to get address for vendor from other microservices
            vendorRepository.save(newVendor);
        }

        return vendorRepository.findById(vendorId).orElse(null);
    }
}
