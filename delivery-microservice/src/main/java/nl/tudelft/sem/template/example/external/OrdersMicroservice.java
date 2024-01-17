package nl.tudelft.sem.template.example.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class OrdersMicroservice {
    private final RestTemplate restTemplate;

    private final String ordersBaseUrl = "http://localhost:8082";

    @Autowired
    public OrdersMicroservice(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Puts the order status in the OrdersMicroservice.
     *
     * @param orderId - The id of the order.
     * @param authorizationId - The id of the user who is making the request.
     * @param status - The status we will update the order with.
     * @return True if the order status was updated successfully, false otherwise.
     */
    public Boolean putOrderStatus(Long orderId, Long authorizationId, String status) {
        String path = ordersBaseUrl + "/order/" + orderId + "/status/" + authorizationId + "?status=" + status;
        try {
            restTemplate.put(path, null);
            return true;
        } catch (HttpClientErrorException ex) {
            return false;
        }
    }
}