package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.exception.RatingNotFoundException;
import nl.tudelft.sem.template.example.service.AnalyticsService;
import nl.tudelft.sem.template.model.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AnalyticsControllerTest {

    private AnalyticsService analyticsService;
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        analyticsService = Mockito.mock(AnalyticsService.class);
        analyticsController = new AnalyticsController(analyticsService);
    }

    @Test
    void testSetRatingSuccess() throws Exception {
        Rating rating = new Rating();
        rating.setGrade(5);
        rating.setComment("Excellent");

        when(analyticsService.saveRating(rating, (long) 10)).thenReturn(rating);

        ResponseEntity<Void> response = analyticsController.analyticsOrderOrderIdRatingPut(10, 1, rating);

        verify(analyticsService).saveRating(rating, (long) 10);
        assertEquals(200, response.getStatusCodeValue());
    }


    @Test
    void testSetRatingOrderNotFound() throws Exception {
        Rating rating = new Rating();
        doThrow(new OrderNotFoundException("Order not found")).when(analyticsService).saveRating(rating, (long) 10);

        ResponseEntity<Void> response = analyticsController.analyticsOrderOrderIdRatingPut(10, 1, rating);

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
        when(analyticsService.getRatingByOrderId(1L)).thenThrow(new OrderNotFoundException("Order not found"));

        ResponseEntity<Rating> response = analyticsController.analyticsOrderOrderIdRatingGet(1, 1);

        verify(analyticsService).getRatingByOrderId(1L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetRatingRatingNotFound() throws Exception {
        when(analyticsService.getRatingByOrderId(1L)).thenThrow(new RatingNotFoundException("Rating not found"));

        ResponseEntity<Rating> response = analyticsController.analyticsOrderOrderIdRatingGet(1, 1);

        verify(analyticsService).getRatingByOrderId(1L);
        assertEquals(404, response.getStatusCodeValue());
    }
}
