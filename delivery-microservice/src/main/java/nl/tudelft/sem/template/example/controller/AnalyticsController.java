package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.api.AnalyticsApi;
import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.*;
import nl.tudelft.sem.template.example.service.AnalyticsService;
import nl.tudelft.sem.template.model.Rating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /**
     * Retrieve the average number of deliveries a courier has made/day
     *
     * @path GET: GET /analytics/courier/{courier_id}/deliveries-per-day : Deliveries/day of a courier
     * @param courierId The id of the courier (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return successful (status code 200)
     *         or Bad request if the courier ID is invalid (status code 400)
     *         or Forbidden access (status code 403)
     *         or Courier not found (status code 404)
     *         or There was a problem with the server (status code 500)
     */
    @Override
    public  ResponseEntity<Integer> analyticsCourierCourierIdDeliveriesPerDayGet(Integer courierId, Integer authorizationId) {
        try {
            if (!authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            int deliveriesPerDay = analyticsService.getDeliveriesPerDay((long) courierId);
            return ResponseEntity.ok(deliveriesPerDay);
        } catch (CourierNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (NoDeliveriesException e) {
            throw new RuntimeException(e);
        } catch (MicroserviceCommunicationException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Retrieve the number of successful deliveries a courier has made
     *
     * @path GET: GET /analytics/courier/{courier_id}/successful-deliveries : Number of successful deliveries of a courier
     * @param courierId The id of the courier (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return successful (status code 200)
     *         or Bad request if the courier ID is invalid (status code 400)
     *         or Forbidden access (status code 403)
     *         or Courier not found (status code 404)
     *         or There was a problem with the server (status code 500)
     */
    @Override
    public ResponseEntity<Integer> analyticsCourierCourierIdSuccessfulDeliveriesGet(Integer courierId, Integer authorizationId) {
        try {
            if (!authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            int successfulDeliveries = analyticsService.getSuccessfulDeliveries((long) courierId);
            return ResponseEntity.ok(successfulDeliveries);
        } catch (CourierNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (NoDeliveriesException e) {
            throw new RuntimeException(e);
        } catch (MicroserviceCommunicationException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieve the issues a courier has encountered during deliveries
     *
     * @path GET: GET /analytics/courier/{courier_id}/courier-issues : Issues of a courier
     * @param courierId The id of the courier (required)
     * @param authorizationId Identification of the user who is making the request (required)
     * @return successful (status code 200)
     *         or Bad request if the courier ID is invalid (status code 400)
     *         or Forbidden access (status code 403)
     *         or Courier not found (status code 404)
     *         or There was a problem with the server (status code 500)
     */
    @Override
    public ResponseEntity<List<String>> analyticsCourierCourierIdCourierIssuesGet(Integer courierId, Integer authorizationId) {
        try {
            if (!authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            List<String> courierIssues = analyticsService.getCourierIssues((long) courierId);
            return ResponseEntity.ok(courierIssues);
        } catch (CourierNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (NoDeliveriesException e) {
            throw new RuntimeException(e);
        } catch (MicroserviceCommunicationException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
