package nl.tudelft.sem.template.example.external;

import nl.tudelft.sem.template.model.Location;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class OrdersMicroservice {
    private final RestTemplate restTemplate;

    private final String usersBaseUrl = "localhost:8082";

    public OrdersMicroservice() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Puts the order status in the OrdersMicroservice.
     * @param orderID - The id of the order.
     * @param authorizationId - The id of the user who is making the request.
     * @param status - The status we will update the order with.
     * @return True if the order status was updated successfully, false otherwise.
     */
    public Boolean putOrderStatus(Long orderID, Long authorizationId, String status) {
        String path = usersBaseUrl + "/order/" + orderID + "/status/" + authorizationId;
        try {
            restTemplate.put(path, status);
            return true;
        } catch (HttpClientErrorException ex) {
            return false;
        }
    }
}