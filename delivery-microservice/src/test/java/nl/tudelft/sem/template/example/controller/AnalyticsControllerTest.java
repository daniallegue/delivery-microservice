package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.IllegalOrderStatusException;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.exception.RatingNotFoundException;
import nl.tudelft.sem.template.example.service.AnalyticsService;
import nl.tudelft.sem.template.model.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AnalyticsControllerTest {

    private AnalyticsService analyticsService;
    private AnalyticsController analyticsController;
    private AuthorizationService authorizationService;
    @BeforeEach
    void setUp() {
        analyticsService = Mockito.mock(AnalyticsService.class);
        authorizationService = Mockito.mock(AuthorizationService.class);
        analyticsController = new AnalyticsController(analyticsService, authorizationService);
    }

    @Test
    void testSetRatingSuccess() throws Exception {
        Rating rating = new Rating();
        rating.setGrade(5);
        rating.setComment("Excellent");

        when(authorizationService.canChangeOrderRating(1L, (long) 10)).thenReturn(true);
        when(analyticsService.saveRating(rating, (long) 10)).thenReturn(rating);

        ResponseEntity<Void> response = analyticsController.analyticsOrderOrderIdRatingPut(10, 1, rating);

        verify(authorizationService).canChangeOrderRating(1L, (long) 10);
        verify(analyticsService).saveRating(rating, (long) 10);
        assertEquals(200, response.getStatusCodeValue());
    }


    @Test
    void testSetRatingOrderNotFound() throws Exception {
        Rating rating = new Rating();

        when(authorizationService.canChangeOrderRating(1L, (long) 10)).thenReturn(true);
        doThrow(new OrderNotFoundException("Order not found")).when(analyticsService).saveRating(rating, (long) 10);

        ResponseEntity<Void> response = analyticsController.analyticsOrderOrderIdRatingPut(10, 1, rating);

        verify(authorizationService).canChangeOrderRating(1L, (long) 10);
        verify(analyticsService).saveRating(rating, (long) 10);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetRatingSuccess() throws Exception {
        Rating rating = new Rating();
        rating.setGrade(4);
        rating.setComment("Good");

        when(analyticsService.getRatingByOrderId(1L)).thenReturn(rating);

        ResponseEntity<Rating> response = analyticsController.analyticsOrderOrderIdRatingGet(1, 1);

        verify(analyticsService).getRatingByOrderId(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(rating, response.getBody());
    }

    @Test
    void testGetRatingOrderNotFound() throws Exception {
        when(analyticsService.getRatingByOrderId((long) 1)).thenThrow(new OrderNotFoundException("Order not found"));

        ResponseEntity<Rating> response = analyticsController.analyticsOrderOrderIdRatingGet(1, 1);

        verify(analyticsService).getRatingByOrderId((long) 1);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetRatingRatingNotFound() throws Exception {
        when(analyticsService.getRatingByOrderId((long) 1)).thenThrow(new RatingNotFoundException("Rating not found"));

        ResponseEntity<Rating> response = analyticsController.analyticsOrderOrderIdRatingGet(1, 1);

        verify(analyticsService).getRatingByOrderId((long) 1);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testSetRatingUnauthorized() throws Exception {
        Rating rating = new Rating();

        when(authorizationService.canChangeOrderRating((long) 1, (long) 10)).thenReturn(false);

        ResponseEntity<Void> response = analyticsController.analyticsOrderOrderIdRatingPut(10, 1, rating);

        verify(authorizationService).canChangeOrderRating((long) 1, (long) 10);
        verifyNoInteractions(analyticsService);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testSetRatingInternalServerError() throws Exception {
        Rating rating = new Rating();
        when(authorizationService.canChangeOrderRating((long) 10, (long) 1)).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<Void> response = analyticsController.analyticsOrderOrderIdRatingPut(1, 10, rating);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
