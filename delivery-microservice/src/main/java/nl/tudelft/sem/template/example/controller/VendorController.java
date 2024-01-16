package nl.tudelft.sem.template.example.controller;

import java.util.List;
import nl.tudelft.sem.template.api.VendorApi;
import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.exception.VendorHasNoCouriersException;
import nl.tudelft.sem.template.example.exception.VendorNotFoundException;
import nl.tudelft.sem.template.example.service.VendorService;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VendorController implements VendorApi {
    VendorService vendorService;
    AuthorizationService authorizationService;

    /**
     * Simple constructor that handles dependency injection of the service.
     *
     * @param vendorService Instance of VendorService to handle the logic
     */
    @Autowired
    public VendorController(VendorService vendorService, AuthorizationService authorizationService) {
        this.vendorService = vendorService;
        this.authorizationService = authorizationService;
    }

    /**
     * Returns the delivery zone of a specific vendor.
     *
     * @param vendorId        Unique identifier of the vendor (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return the delivery zone radius of the vendor
     * @path GET: GET /vendor/delivery/{vendor_id}/delivery-zone:
     */
    @Override
    public ResponseEntity<Integer> vendorDeliveryVendorIdDeliveryZoneGet(Integer vendorId, Integer authorizationId) {
        try {

            Integer deliveryZone = (int) vendorService.getDeliveryZone((long) vendorId);
            return ResponseEntity.ok(deliveryZone);
        } catch (VendorNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Updates the delivery zone of a specific vendor.
     *
     * @param vendorId        Unique identifier of the vendor (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @param deliveryZone    The body of the request
     * @return the delivery zone radius of the vendor
     * @path PUT: GET /vendor/delivery/{vendor_id}/delivery-zone:
     */
    @Override
    public ResponseEntity<Vendor> vendorDeliveryVendorIdDeliveryZonePut(Integer vendorId,
                                                                        Integer deliveryZone, Integer authorizationId) {
        try {
            if (authorizationService.cannotUpdateVendorDeliveryZone(authorizationId)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            Vendor vendor = vendorService.updateDeliveryZone((long) vendorId, (long) deliveryZone);
            return ResponseEntity.ok(vendor);
        } catch (VendorNotFoundException | VendorHasNoCouriersException | MicroserviceCommunicationException e) {
            if (e instanceof VendorNotFoundException) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
    }

    /**
     * Assigns the courier with ID courier_id to the vendor with ID vendor_id.
     *
     * @param vendorId The unique identifier of the order. (required)
     * @param courierId The unique identifier of the courier. (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return the updated vendor
     * @path PUT: /vendor/delivery/{vendor_id}/assign/{courier_id}:
     */
    @Override
    public ResponseEntity<Vendor> vendorDeliveryVendorIdAssignCourierIdPut(Integer vendorId,
                                                                           Integer courierId, Integer authorizationId) {
        try {
            if (!authorizationService.getUserRole(authorizationId).equals(AuthorizationService.ADMIN)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            Vendor vendor = vendorService.assignCourierToVendor((long) vendorId, (long) courierId);
            return ResponseEntity.ok(vendor);
        } catch (VendorNotFoundException | MicroserviceCommunicationException | CourierNotFoundException e) {
            if (e instanceof VendorNotFoundException) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
    }

    /**
     * Retrieves the ids of all couriers assigned to the vendor with the specified ID.
     *
     * @param vendorId The unique identifier of the vendor. (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return list of assigned couriers ids
     * @path GET: /vendor/delivery/{vendor_id}/couriers:
     */
    @Override
    public ResponseEntity<List<Long>> vendorDeliveryVendorIdCouriersGet(Integer vendorId, Integer authorizationId) {
        try {
            if (!authorizationService.getUserRole(authorizationId).equals(AuthorizationService.VENDOR)
                    && !authorizationService.getUserRole( authorizationId).equals(AuthorizationService.ADMIN)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            List<Long> courierIds = vendorService.getAssignedCouriers((long) vendorId);
            return ResponseEntity.ok(courierIds);
        } catch (VendorNotFoundException | MicroserviceCommunicationException e) {
            if (e instanceof VendorNotFoundException) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Override
    public ResponseEntity<Location> vendorDeliveryVendorIdVendorAddressGet(Integer vendorId, Integer authorizationId) {
        try {
            Location location = vendorService.getVendorLocation(vendorId);
            return ResponseEntity.ok(location);
        } catch (VendorNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (MicroserviceCommunicationException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
