package nl.tudelft.sem.template.example.controller_mockmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.example.Application;
import nl.tudelft.sem.template.example.TestDatabaseLoader;
import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.service.AnalyticsService;

import nl.tudelft.sem.template.model.Rating;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = Application.class)
public class AnalyticsControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDatabaseLoader testDatabaseLoader;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private AuthorizationService authorizationService;

    @BeforeEach
    public void setup() {
        testDatabaseLoader.loadTestData();
    }

    @AfterEach
    public void tearDown() {
        testDatabaseLoader.clearTestData();
    }

    @Test
    public void testCreateRating() throws Exception {
        Rating testRating = new Rating(5, "Excellent service");
        Integer orderId = 1;
        Integer authorizationId = 2;

        Mockito.when(authorizationService.canChangeOrderRating(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
        Mockito.when(analyticsService.saveRating(Mockito.any(Rating.class), Mockito.anyLong())).thenReturn(testRating);

        mvc.perform(put("/analytics/order/" + orderId + "/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRating))
                        .header("authorizationId", authorizationId.toString()))
                .andExpect(status().isOk());

    }


    @Test
    public void testGetRatingByOrderId() throws Exception {
        Integer orderId = 1;
        Integer authorizationId = 2;
        Rating testRating = new Rating(5, "Excellent service");

        Mockito.when(analyticsService.getRatingByOrderId(Mockito.anyLong())).thenReturn(testRating);

        mvc.perform(get("/analytics/order/" + orderId + "/rating")
                        .header("authorizationId", authorizationId.toString()))
                .andExpect(status().isOk());

    }

    @Test
    public void testGetDeliveriesPerDay() throws Exception {
        Integer courierId = 1;
        Integer authorizationId = 2;
        int deliveriesPerDay = 5;

        Mockito.when(authorizationService.canViewCourierAnalytics(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
        Mockito.when(analyticsService.getDeliveriesPerDay(Mockito.anyLong())).thenReturn(deliveriesPerDay);

        mvc.perform(get("/analytics/courier/" + courierId + "/deliveries-per-day")
                        .header("authorizationId", authorizationId.toString()))
                .andExpect(status().isOk());

    }

    @Test
    public void testGetSuccessfulDeliveries() throws Exception {
        Integer courierId = 1;
        Integer authorizationId = 2;
        int successfulDeliveries = 10;

        Mockito.when(authorizationService.canViewCourierAnalytics(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
        Mockito.when(analyticsService.getSuccessfulDeliveries(Mockito.anyLong())).thenReturn(successfulDeliveries);

        mvc.perform(get("/analytics/courier/" + courierId + "/successful-deliveries")
                        .header("authorizationId", authorizationId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(successfulDeliveries)));
    }


    @Test
    public void testGetCourierIssues() throws Exception {
        Integer courierId = 1;
        Integer authorizationId = 2;
        List<String> issuesList = Arrays.asList("Issue1", "Issue2");

        Mockito.when(authorizationService.canViewCourierAnalytics(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
        Mockito.when(analyticsService.getCourierIssues(Mockito.anyLong())).thenReturn(issuesList);

        mvc.perform(get("/analytics/courier/" + courierId + "/courier-issues")
                        .header("authorizationId", authorizationId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(issuesList.size())))
                .andExpect(jsonPath("$[0]", is(issuesList.get(0))))
                .andExpect(jsonPath("$[1]", is(issuesList.get(1))));
    }


    @Test
    public void testCreateRating_InvalidInput() throws Exception {
        Rating invalidRating = new Rating(6, "Invalid rating");
        Integer orderId = 1;
        Integer authorizationId = 2;

        mvc.perform(put("/analytics/order/" + orderId + "/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRating))
                        .header("Authorization", authorizationId))
                .andExpect(status().isBadRequest());
    }

}
