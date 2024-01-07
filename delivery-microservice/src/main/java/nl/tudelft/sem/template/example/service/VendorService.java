package nl.tudelft.sem.template.example.service;

import java.util.ArrayList;
import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.exception.VendorHasNoCouriersException;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
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


    /**
     * Retrieves the delivery zone radius from the vendor with the given vendorId.
     *
     * @param vendorId The id of the vendor.
     * @return The delivery zone from the radius.
     */
    public long getDeliveryZone(Long vendorId) throws VendorNotFoundException {
        if (!vendorRepository.existsById(vendorId)) {
            throw new VendorNotFoundException("Vendor was not found");
        }
        Vendor vendor = vendorRepository.findById(vendorId).get();
        //Check for Null/Default delivery zone
        return vendor.getDeliveryZone();
    }

    /**
     * Updates the delivery zone radius from the vendor with the given vendorId.
     * Only vendor with its own couriers are able to update their delivery zones.
     *
     * @param vendorId The id of the vendor.
     * @return The delivery zone from the radius.
     */
    public Vendor updateDeliveryZone(Long vendorId, Long deliveryZone)
            throws VendorNotFoundException, VendorHasNoCouriersException {
        if (!vendorRepository.existsById(vendorId)) {
            throw new VendorNotFoundException("Vendor was not found");
        }
        Vendor vendor = vendorRepository.findById(vendorId).get();

        if (vendor.getCouriers() == null || vendor.getCouriers().size() < 1) {
            throw new VendorHasNoCouriersException("Vendor must have their own set of couriers");
        }

        vendor.setDeliveryZone(deliveryZone);
        vendorRepository.save(vendor);
        return vendor;
    }
}
