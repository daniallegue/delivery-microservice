package nl.tudelft.sem.template.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.exception.VendorHasNoCouriersException;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
import nl.tudelft.sem.template.example.external.UsersMicroservice;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class VendorService {

    VendorRepository vendorRepository;
    ConfigurationProperties configurationProperties;

    UsersMicroservice usersMicroservice;
    CourierService courierService;

    /**
     * Constructor for the Service allowing dependency injection.
     *
     * @param vendorRepository The JPA repository holding the Vendor entities.
     * @param configurationProperties The configurations holding the delivery zone.
     */
    @Autowired
    public VendorService(VendorRepository vendorRepository, ConfigurationProperties configurationProperties,
                  UsersMicroservice usersMicroservice, CourierService courierService) {
        this.vendorRepository = vendorRepository;
        this.configurationProperties = configurationProperties;
        this.usersMicroservice = usersMicroservice;
        this.courierService = courierService;
    }

    /**
     * Retrieves a vendor from the repository if one exists with the given
     * vendorId, or creates a new one by making a call to the endpoint of
     * the Users microservice.
     *
     * @param vendorId The id of the vendor.
     * @return The vendor with the given id.
     */
    public Vendor findVendorOrCreate(Long vendorId) throws MicroserviceCommunicationException {
        try {
            if (!vendorRepository.existsById(vendorId)) {
                createVendor(vendorId);
            }
            return vendorRepository.findById(vendorId).orElse(null);
        } catch (MicroserviceCommunicationException e) {
            throw new MicroserviceCommunicationException("The vendor address could not be retrieved");
        }
    }

    /**
     * Creates a vendor instance in the repository.
     *
     * @param vendorId The id of the vendor.
     */
    public void createVendor(Long vendorId) throws MicroserviceCommunicationException {
        Vendor newVendor = new Vendor();
        newVendor.setId(vendorId);
        newVendor.setDeliveryZone(configurationProperties.getDefaultDeliveryZone());
        newVendor.setCouriers(new ArrayList<>());

        Optional<Location> vendorAddress = usersMicroservice.getVendorLocation(vendorId);
        if (vendorAddress.isPresent()) {
            newVendor.setAddress(vendorAddress.get());
            vendorRepository.save(newVendor);
        } else {
            throw new MicroserviceCommunicationException("The vendor address could not be retrieved");
        }
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

    /**
     * Assigns courier to the given vendor.
     *
     * @param vendorId The id of the vendor.
     * @param courierId the id of the courier
     * @return the updated vendor
     * @throws VendorNotFoundException throws exception if vendor does not exist
     */
    public Vendor assignCourierToVendor(Long vendorId, Long courierId)
            throws VendorNotFoundException, CourierNotFoundException {
        if (!vendorRepository.existsById(vendorId)) {
            throw new VendorNotFoundException("Vendor was not found");
        }
        if (!courierService.doesCourierExist(courierId)) {
            throw new CourierNotFoundException("Courier was not found");
        }
        Vendor vendor = vendorRepository.findById(vendorId).get();
        List<Long> currentCouriers = vendor.getCouriers();
        currentCouriers.add(courierId);
        vendor.setCouriers(currentCouriers);
        vendorRepository.save(vendor);
        return vendor;

    }

    /**
     * Gets a list of assigned courier ids to a given vendor.
     *
     * @param vendorId The id of the vendor
     * @return list of assigned courier ids or empty list if vendor does not have couriers
     * @throws VendorNotFoundException throws exception if vendor was not found
     */
    public List<Long> getAssignedCouriers(Long vendorId) throws VendorNotFoundException {
        if (!vendorRepository.existsById(vendorId)) {
            throw new VendorNotFoundException("Vendor was not found");
        }
        Vendor vendor = vendorRepository.findById(vendorId).get();
        return vendor.getCouriers();
    }

    /**
     * Method that retrieves a Vendor's address.
     *
     * @param vendorId The id of the Vendor we want to get the address of.
     * @return The address of the Vendor.
     * @throws VendorNotFoundException If the Vendor with the specified id does not exist.
     * @throws MicroserviceCommunicationException If the Vendor does not have an address saved, it
     *     means that there was a problem with retrieving it from the Users Microservice.
     */
    public Location getVendorLocation(Integer vendorId) throws VendorNotFoundException,
            MicroserviceCommunicationException {
        Optional<Vendor> vendor =  vendorRepository.findById(Long.valueOf(vendorId));
        if (vendor.isEmpty()) {
            throw new VendorNotFoundException("Vendor with id: " + vendorId + " was not found!");
        }
        Location location = vendor.get().getAddress();
        if (location == null) {
            throw new MicroserviceCommunicationException("The vendor address was never saved");
        }

        return location;
    }
}
