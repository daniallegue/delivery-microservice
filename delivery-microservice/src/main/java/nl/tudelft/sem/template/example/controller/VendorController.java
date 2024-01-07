package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.api.VendorApi;
import nl.tudelft.sem.template.example.exception.VendorHasNoCouriersException;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
import nl.tudelft.sem.template.example.service.VendorService;
import nl.tudelft.sem.template.model.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class VendorController implements VendorApi {
    VendorService vendorService;

    /**
     * Simple constructor that handles dependency injection of the service.
     *
     * @param vendorService Instance of VendorService to handle the logic
     */
    @Autowired
    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    /**
     * Returns the delivery zone of a specific vendor.
     *
     * @path GET: GET /vendor/delivery/{vendor_id}/delivery-zone:
     * @param vendorId Unique identifier of the vendor (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return the delivery zone radius of the vendor
     */
    @Override
    public ResponseEntity<Integer> vendorDeliveryVendorIdDeliveryZoneGet(Integer vendorId, Integer authorizationId) {
        //TODO: handle authorization
        try {
            Integer deliveryZone = (int) vendorService.getDeliveryZone((long) vendorId);
            return ResponseEntity.ok(deliveryZone);
        } catch (VendorNotFoundException e) {
            return (ResponseEntity<Integer>) ResponseEntity.status(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Updates the delivery zone of a specific vendor.
     *
     * @path PUT: GET /vendor/delivery/{vendor_id}/delivery-zone:
     * @param vendorId Unique identifier of the vendor (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @param deliveryZone The body of the request
     * @return the delivery zone radius of the vendor
     */
    @Override
    public ResponseEntity<Vendor> vendorDeliveryVendorIdDeliveryZonePut(Integer vendorId,
                                                                        Integer deliveryZone, Integer authorizationId) {
        //TODO: handle authorization
        try {
            Vendor vendor = vendorService.updateDeliveryZone((long) vendorId, (long) deliveryZone);
            return ResponseEntity.ok(vendor);
        } catch (VendorNotFoundException | VendorHasNoCouriersException e) {
            return (ResponseEntity<Vendor>) ResponseEntity.status(HttpStatus.NOT_FOUND);
        }
    }
}