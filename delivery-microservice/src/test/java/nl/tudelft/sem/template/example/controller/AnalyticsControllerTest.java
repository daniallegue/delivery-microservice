package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.*;
import nl.tudelft.sem.template.example.service.AnalyticsService;
import nl.tudelft.sem.template.model.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AnalyticsControllerTest {

    private AnalyticsService analyticsService;
    private AnalyticsController analyticsController;
    private AuthorizationService authorizationService;
    private Integer courierId;
    private Integer authorizationId;
    private Rating rating;
    private Integer orderId;

    @BeforeEach
    void setUp() {
        analyticsService = Mockito.mock(AnalyticsService.class);
        authorizationService = Mockito.mock(AuthorizationService.class);
        analyticsController = new AnalyticsController(analyticsService, authorizationService);
        courierId = 1;
        authorizationId = 1;
        orderId = 10;
        rating = new Rating();
    }

    @Test
    void testSetRatingSuccess() throws Exception {
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
        when(authorizationService.canChangeOrderRating(1L, (long) 10)).thenReturn(true);
        doThrow(new OrderNotFoundException("Order not found")).when(analyticsService).saveRating(rating, (long) 10);

        ResponseEntity<Void> response = analyticsController.analyticsOrderOrderIdRatingPut(10, 1, rating);

        verify(authorizationService).canChangeOrderRating(1L, (long) 10);
        verify(analyticsService).saveRating(rating, (long) 10);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void testRatingPutIllegalOrderStatusException() throws MicroserviceCommunicationException, OrderNotFoundException, IllegalOrderStatusException {
        when(authorizationService.canChangeOrderRating((long) authorizationId, (long) orderId)).thenReturn(true);
        doThrow(new IllegalOrderStatusException("Illegal order status")).when(analyticsService).saveRating(rating, (long) orderId);

        ResponseEntity<Void> response = analyticsController.analyticsOrderOrderIdRatingPut(orderId, authorizationId, rating);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testSetRatingUnauthorized() throws Exception {
        when(authorizationService.canChangeOrderRating((long) 1, (long) 10)).thenReturn(false);
        ResponseEntity<Void> response = analyticsController.analyticsOrderOrderIdRatingPut(10, 1, rating);

        verify(authorizationService).canChangeOrderRating((long) 1, (long) 10);
        verifyNoInteractions(analyticsService);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testSetRatingInternalServerError() throws Exception {
        when(authorizationService.canChangeOrderRating((long) 10, (long) 1)).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<Void> response = analyticsController.analyticsOrderOrderIdRatingPut(1, 10, rating);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
    @Test
    void testGetRatingSuccess() throws Exception {
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

/////BURAYA TEST
    @Test
    void testGetDeliveriesPerDaySuccess() throws Exception {
        when(authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)).thenReturn(true);
        when(analyticsService.getDeliveriesPerDay((long) courierId)).thenReturn(5);

        ResponseEntity<Integer> response = analyticsController.analyticsCourierCourierIdDeliveriesPerDayGet(courierId, authorizationId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody());
    }

    @Test
    void testGetDeliveriesPerDayUnauthorized() throws Exception {
        when(authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)).thenReturn(false);

        ResponseEntity<Integer> response = analyticsController.analyticsCourierCourierIdDeliveriesPerDayGet(courierId, authorizationId);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testGetDeliveriesPerDayCourierNotFound() throws Exception {
        when(authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)).thenReturn(true);
        when(analyticsService.getDeliveriesPerDay((long) courierId)).thenThrow(new CourierNotFoundException("Courier not found"));

        ResponseEntity<Integer> response = analyticsController.analyticsCourierCourierIdDeliveriesPerDayGet(courierId, authorizationId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeliveriesPerDayNoDeliveriesException() throws MicroserviceCommunicationException, NoDeliveriesException, CourierNotFoundException {
        when(authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)).thenReturn(true);
        when(analyticsService.getDeliveriesPerDay((long) courierId)).thenThrow(new NoDeliveriesException("No deliveries for this courier."));
        Exception exception = assertThrows(RuntimeException.class, () -> {
            analyticsController.analyticsCourierCourierIdDeliveriesPerDayGet(courierId, authorizationId);
        });
        assertTrue(exception.getCause() instanceof NoDeliveriesException);
    }

    //@Test
//public void testAnalyticsCourierCourierIdDeliveriesPerDayGetMicroserviceCommunicationException() throws MicroserviceCommunicationException, NoDeliveriesException, CourierNotFoundException {
//    Integer courierId = 1;
//    Integer authorizationId = 1;
//    when(authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)).thenReturn(true);
//    when(analyticsService.getDeliveriesPerDay((long) courierId)).thenThrow(new MicroserviceCommunicationException(""));
//
//    ResponseEntity<Integer> response = analyticsController.analyticsCourierCourierIdDeliveriesPerDayGet(courierId, authorizationId);
//    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
//}

    @Test
    void testGetSuccessfulDeliveriesSuccess() throws Exception {
        when(authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)).thenReturn(true);
        when(analyticsService.getSuccessfulDeliveries((long) courierId)).thenReturn(10);

        ResponseEntity<Integer> response = analyticsController.analyticsCourierCourierIdSuccessfulDeliveriesGet(courierId, authorizationId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10, response.getBody());
    }

    @Test
    public void testSuccessfulDeliveriesCourierNotFoundException() throws MicroserviceCommunicationException {
        when(authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)).thenReturn(false);
        ResponseEntity<Integer> response = analyticsController.analyticsCourierCourierIdSuccessfulDeliveriesGet(courierId, authorizationId);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testAnalyticsCourierCourierIdSuccessfulDeliveriesGetNoDeliveries() throws MicroserviceCommunicationException, NoDeliveriesException, CourierNotFoundException {
        when(authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)).thenReturn(true);
        when(analyticsService.getSuccessfulDeliveries((long) courierId)).thenThrow(new NoDeliveriesException("Courier has no deliveries"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            analyticsController.analyticsCourierCourierIdSuccessfulDeliveriesGet(courierId, authorizationId);
        });
        assertTrue(exception.getCause() instanceof NoDeliveriesException);
    }

    @Test
    public void testAnalyticsCourierCourierIdSuccessfulDeliveriesGetCourierNotFound() throws MicroserviceCommunicationException, NoDeliveriesException, CourierNotFoundException {
        when(authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)).thenReturn(true);
        when(analyticsService.getSuccessfulDeliveries((long) courierId)).thenThrow(new CourierNotFoundException("Courier not found"));

        ResponseEntity<Integer> response = analyticsController.analyticsCourierCourierIdSuccessfulDeliveriesGet(courierId, authorizationId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    void testGetCourierIssuesSuccess() throws Exception {
        List<String> mockIssues = Arrays.asList("Issue 1", "Issue 2");
        when(authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)).thenReturn(true);
        when(analyticsService.getCourierIssues((long) courierId)).thenReturn(mockIssues);

        ResponseEntity<List<String>> response = analyticsController.analyticsCourierCourierIdCourierIssuesGet(courierId, authorizationId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockIssues, response.getBody());
    }

    @Test
    public void testAnalyticsCourierCourierIdCourierIssuesGetCourierNotFound() throws MicroserviceCommunicationException, NoDeliveriesException, CourierNotFoundException {
        when(authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)).thenReturn(true);
        when(analyticsService.getCourierIssues((long) courierId)).thenThrow(new CourierNotFoundException("Courier not found"));

        ResponseEntity<List<String>> response = analyticsController.analyticsCourierCourierIdCourierIssuesGet(courierId, authorizationId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testAnalyticsCourierCourierIdCourierIssuesGetNoDeliveries() throws MicroserviceCommunicationException, NoDeliveriesException, CourierNotFoundException {
        when(authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)).thenReturn(true);
        when(analyticsService.getCourierIssues((long) courierId)).thenThrow(new NoDeliveriesException("Courier has no deliveries"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            analyticsController.analyticsCourierCourierIdCourierIssuesGet(courierId, authorizationId);
        });

        assertTrue(exception.getCause() instanceof NoDeliveriesException);
    }

    @Test
    public void testAnalyticsCourierCourierIdCourierIssuesGetForbidden() throws MicroserviceCommunicationException {
        when(authorizationService.canViewCourierAnalytics((long) authorizationId, (long) courierId)).thenReturn(false);

        ResponseEntity<List<String>> response = analyticsController.analyticsCourierCourierIdCourierIssuesGet(courierId, authorizationId);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }


}
