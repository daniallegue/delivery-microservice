package nl.tudelft.sem.template.example.authorization;

import java.util.Optional;
import java.util.Objects;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.external.UsersMicroservice;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.model.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.Collections.replaceAll;

@Service
public class AuthorizationService {
    private final UsersMicroservice usersMicroservice;

    private final DeliveryRepository deliveryRepository;

    public static final String ADMIN = "admin";
    public static final String CUSTOMER = "customer";
    public static final String VENDOR = "vendor";
    public static final String COURIER = "courier";


    /**
     * Constructor of the AuthorizationService.
     *
     * @param usersMicroservice The microservice responsible for retrieving user-related information.
     * @param deliveryRepository The repository for accessing delivery-related data.
     */
    @Autowired
    public AuthorizationService(UsersMicroservice usersMicroservice, DeliveryRepository deliveryRepository) {
        this.usersMicroservice = usersMicroservice;
        this.deliveryRepository = deliveryRepository;
    }

    /**
     * Checks whether the user making the call to the endpoint is involved in the specified order.
     *
     * @param authorizationId The id of the user making the request.
     * @param role The role of the user with the provided id.
     * @param orderId The id of the order for which the request is made.
     * @return {@code true} if the user is involved in the order; otherwise, {@code false}.
     */
    public Boolean isInvolvedInOrder(Long authorizationId, String role, Long orderId) {
        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        if (role.equals("admin")) {
            return true;
        }
        switch (role) {
            case CUSTOMER -> {
                return delivery.getOrder().getCustomerId().equals(authorizationId);
            }
            case VENDOR -> {
                return delivery.getOrder().getVendor().getId().equals(authorizationId);
            }
            case COURIER -> {
                Long deliveryCourierId = delivery.getCourierId();
                return deliveryCourierId != null && deliveryCourierId.equals(authorizationId);
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * Retrieves the role of the user with the specified authorization id from the user microservice.
     *
     * @param authorizationId The id of the user for whom the role is retrieved.
     * @return The role of the user.
     * @throws MicroserviceCommunicationException If communication with the user microservice fails
     *         or if the user type could not be found.
     */
    public String getUserRole(Long authorizationId) throws MicroserviceCommunicationException {
        Optional<String> userType = usersMicroservice.getUserType(authorizationId);
        if (userType.isEmpty()) {
            throw new MicroserviceCommunicationException("User type could not be found");
        }
        return userType.get().replaceAll("\"", "");
    }

    /**
     * Checks whether the user with the specified authorization id can view delivery details for the given order.
     *
     * @param authorizationId The id of the user for whom the permission is checked.
     * @param orderId The id of the order for which the permission is checked.
     * @return {@code true} if the user is allowed to view delivery details; otherwise, {@code false}.
     * @throws MicroserviceCommunicationException If communication with the user microservice fails
     *         or if the user type could not be found.
     */
    public Boolean canViewDeliveryDetails(Long authorizationId, Long orderId) throws MicroserviceCommunicationException {
        String role = getUserRole(authorizationId);
        return isInvolvedInOrder(authorizationId, role, orderId);
    }

    /**
     * Checks whether the user with the specified authorization id has permission to update delivery details for the given order.
     *
     * @param authorizationId The id of the user for whom the permission is checked.
     * @param orderId The id of the order for which the permission is checked.
     * @return {@code true} if the user is allowed to update delivery details; otherwise, {@code false}.
     * @throws MicroserviceCommunicationException If communication with the user microservice fails
     *         or if the user type could not be found.
     */
    public Boolean canUpdateDeliveryDetails(Long authorizationId, Long orderId) throws MicroserviceCommunicationException {
        String role = getUserRole(authorizationId);
        return !role.equals(CUSTOMER) && isInvolvedInOrder(authorizationId, role, orderId);
    }

    /**
     * Checks whether the user with the specified authorization id has permission to view courier analytics or is an admin.
     *
     * @param authorizationId The id of the user for whom the permission is checked.
     * @param courierId The id of the courier for whom analytics are viewed.
     * @return {@code true} if the user is allowed to view courier analytics; otherwise, {@code false}.
     * @throws MicroserviceCommunicationException If communication with the user microservice fails
     *         or if the user type could not be found.
     */
    public Boolean canViewCourierAnalytics(Long authorizationId, Long courierId) throws MicroserviceCommunicationException {
        String role = getUserRole(authorizationId);
        if (role.equals(ADMIN)) {
            return true;
        }
        return role.equals(COURIER) && Objects.equals(authorizationId, courierId);
    }

    /**
     * Checks whether the user with the specified authorization id has permission to change the rating of the given order.
     *
     * @param authorizationId The id of the user for whom the permission is checked.
     * @param orderId The id of the order for which the rating can be changed.
     * @return {@code true} if the user is allowed to change the order rating; otherwise, {@code false}.
     * @throws MicroserviceCommunicationException If communication with the user microservice fails
     *         or if the user type could not be found.
     */
    public Boolean canChangeOrderRating(Long authorizationId, Long orderId) throws MicroserviceCommunicationException {
        String role = getUserRole(authorizationId);
        return role.equals(CUSTOMER) && isInvolvedInOrder(authorizationId, role, orderId);
    }

    /**
     * @param authorizationId The id of the user for whom the permission is checked.
     * @return {@code true} if the user is allowed to change the delviery zone; otherwise, {@code false}.
     * @throws MicroserviceCommunicationException If communication with the user microservice fails
     *            or if the user type could not be found.
     */
    public Boolean cannotUpdateVendorDeliveryZone(Long authorizationId) throws MicroserviceCommunicationException {
        String role = getUserRole(authorizationId);
        return !role.equals(ADMIN) && !role.equals(VENDOR);
    }

}
