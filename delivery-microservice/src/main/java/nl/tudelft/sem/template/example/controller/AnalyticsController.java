package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.api.AnalyticsApi;
import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.IllegalOrderStatusException;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
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
    AuthorizationService authorizationService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService, AuthorizationService authorizationService) {
        this.analyticsService = analyticsService;
        this.authorizationService = authorizationService;
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
            if (!authorizationService.canChangeOrderRating((long) authorizationId, (long) orderId)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            analyticsService.saveRating(rating, (long) orderId);
            return ResponseEntity.ok().build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalOrderStatusException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (MicroserviceCommunicationException e) {
            return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
            Rating rating = analyticsService.getRatingByOrderId((long) orderId);
            if (rating == null) {
                throw new RatingNotFoundException("Rating for order ID " + orderId + " not found.");
            }
            return ResponseEntity.ok(rating);
        } catch (RatingNotFoundException | OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
