package nl.tudelft.sem.template.example.external;

import java.util.List;
import java.util.Optional;

import nl.tudelft.sem.template.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class UsersMicroservice {
    private final RestTemplate restTemplate;

    private final String usersBaseUrl = "http://localhost:8081";

    @Autowired
    public UsersMicroservice(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get the User Type from the Users Microservice.
     *
     * @param userId The id of the user we want to check the type of (admin, vendor, courier, customer).
     * @return The type of user if it can be found or an empty optional otherwise.
     */
    public Optional<String> getUserType(Long userId) {
        String path = usersBaseUrl + "/user/" + userId;
        try {
            String userType = restTemplate.getForObject(path, String.class);
            return Optional.ofNullable(userType);
        } catch (HttpClientErrorException ex) {
            return Optional.empty();
        }
    }

    /**
     * Get the Vendor Address from the Users Microservice.
     *
     * @param vendorId The id of the vendor.
     * @return The location if it can reach it or an empty optional otherwise.
     */
    public Optional<Location> getVendorLocation(Long vendorId) {
        String path = usersBaseUrl + "/vendor/" + vendorId + "/location";
        try {
            Location vendorLocation = restTemplate.getForObject(path, Location.class);
            return Optional.ofNullable(vendorLocation);
        } catch (HttpClientErrorException ex) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves all the courier ids from the Users microservice.
     *
     * @return List of couriers
     */
    public Optional<List<Long>> getCourierIds() {
        String path = usersBaseUrl + "/courier";
        try {
            List<Long> couriers = restTemplate.getForObject(path, List.class);
            return Optional.of(couriers);
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }
}
