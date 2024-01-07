package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.api.AnalyticsApi;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.exception.RatingNotFoundException;
import nl.tudelft.sem.template.example.service.AnalyticsService;
import nl.tudelft.sem.template.model.Rating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyticsController implements AnalyticsApi {
    AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Creates a rating for an order
     *
     * @path PUT: PUT /analytics/order/{order_id}/rating
     * @param orderId Unique identifier of the order (required)
     * @param authorizationId Identification of the user who is making the request (required)
     */
    @Override
    public ResponseEntity<Void> analyticsOrderOrderIdRatingPut(Integer orderId, Integer authorizationId, Rating rating) {
        try {
            analyticsService.saveRating(rating, Long.valueOf(orderId));
        } catch (OrderNotFoundException | DeliveryNotFoundException e) {
            return (ResponseEntity<Void>) ResponseEntity.status(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Returns a rating for a specific order
     *
     * @path GET: GET /analytics/order/{order_id}/rating
     * @param orderId Unique identifier of the order (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return Returns the rating of a specific order
     */
    @Override
    public ResponseEntity<Rating> analyticsOrderOrderIdRatingGet(Integer orderId, Integer authorizationId) {
        try {
            Rating rating = analyticsService.getRatingByOrderId(Long.valueOf(orderId));
            if (rating == null) {
                throw new RatingNotFoundException("Rating for order ID " + orderId + " not found.");
            }
            return ResponseEntity.ok(rating);
        } catch (RatingNotFoundException | OrderNotFoundException | DeliveryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
