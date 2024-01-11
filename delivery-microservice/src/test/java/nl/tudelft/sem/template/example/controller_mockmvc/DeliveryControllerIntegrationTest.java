package nl.tudelft.sem.template.example.controller_mockmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.example.Application;
import nl.tudelft.sem.template.example.TestDatabaseLoader;
import nl.tudelft.sem.template.example.external.UsersMicroservice;
import nl.tudelft.sem.template.model.DeliveryPostRequest;
import nl.tudelft.sem.template.model.Issue;
import nl.tudelft.sem.template.model.Location;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = Application.class)
public class DeliveryControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDatabaseLoader testDatabaseLoader;

    @MockBean
    private UsersMicroservice usersMicroservice;

    @BeforeEach
    public void setUp() {
        testDatabaseLoader.loadTestData();
    }

    @AfterEach
    public void tearDown() {
        testDatabaseLoader.clearTestData();}

    @Test
    void addDeliveryGoodMicroserviceCommunication() throws Exception {
        Integer authorizationId = 5;
        DeliveryPostRequest dummyDeliveryPostRequest = new DeliveryPostRequest();
        dummyDeliveryPostRequest.setVendorId(1);
        dummyDeliveryPostRequest.setOrderId(123);
        dummyDeliveryPostRequest.setCustomerId(456);
        dummyDeliveryPostRequest.setDestination(new Location(4.0, 5.0));

        when(usersMicroservice.getVendorLocation(any())).thenReturn(Optional.of(new Location(2.0, 3.0)));

        mvc.perform(post("/delivery/")
                        .header("authorizationId", authorizationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dummyDeliveryPostRequest)))
                .andExpect(status().isOk());

        verify(usersMicroservice, times(1)).getVendorLocation(any());
    }
    @Test
    void addDeliveryBadMicroserviceCommunication() throws Exception {
        DeliveryPostRequest dummyDeliveryPostRequest = new DeliveryPostRequest();
        dummyDeliveryPostRequest.setVendorId(1);
        dummyDeliveryPostRequest.setOrderId(123);
        dummyDeliveryPostRequest.setCustomerId(456);
        dummyDeliveryPostRequest.setDestination(new Location(4.0, 5.0));

        when(usersMicroservice.getVendorLocation(any())).thenReturn(Optional.empty());

        mvc.perform(post("/delivery/")
                        .header("authorizationId", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dummyDeliveryPostRequest)))
                .andExpect(status().isBadRequest());

        verify(usersMicroservice, times(1)).getVendorLocation(any());
    }


    @Test
    void addIssueToDeliveryWorks() throws Exception{
        Issue issue = new Issue("traffic", "There was an accident on the way, so the order will be delivered later");
        when(usersMicroservice.getUserType(any())).thenReturn(Optional.of("admin"));

        mvc.perform(put("/delivery/order/1234/issue")
                        .header("authorizationId", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(issue)))
                .andExpect(status().isOk());
        verify(usersMicroservice, times(1)).getUserType(any());
    }

    @Test
    void addIssueToDeliveryForbidden() throws Exception{
        Issue issue = new Issue("traffic", "There was an accident on the way, so the order will be delivered later");
        when(usersMicroservice.getUserType(any())).thenReturn(Optional.of("admin"));

        mvc.perform(put("/delivery/order/1234/issue")
                        .header("authorizationId", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(issue)))
                .andExpect(status().isOk());
        verify(usersMicroservice, times(1)).getUserType(any());
    }

    @Test
    void addIssueToDeliveryOrderNotFound() throws Exception{
        Issue issue = new Issue("traffic", "There was an accident on the way, so the order will be delivered later");
        when(usersMicroservice.getUserType(any())).thenReturn(Optional.of("admin"));

        mvc.perform(put("/delivery/order/12/issue")
                        .header("authorizationId", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(issue)))
                .andExpect(status().isBadRequest());
        verify(usersMicroservice, times(1)).getUserType(any());
    }

    @Test
    void addIssueToDeliveryAccessForbidden() throws Exception{
        Issue issue = new Issue("traffic", "There was an accident on the way, so the order will be delivered later");
        when(usersMicroservice.getUserType(any())).thenReturn(Optional.of("customer"));

        mvc.perform(put("/delivery/order/1234/issue")
                        .header("authorizationId", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(issue)))
                .andExpect(status().isForbidden());
        verify(usersMicroservice, times(1)).getUserType(any());
    }

    @Test
    void getIssueOfDeliveryWorks() throws Exception{
        Issue issue = new Issue("traffic", "There was an accident on the way, so the order will be delivered later");
        when(usersMicroservice.getUserType(any())).thenReturn(Optional.of("admin"));

        mvc.perform(get("/delivery/order/1/issue")
                        .header("authorizationId", 4L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(issue)));
        verify(usersMicroservice, times(1)).getUserType(any());
    }

    @Test
    void getIssueOfDeliveryAccessForbidden() throws Exception{
        when(usersMicroservice.getUserType(any())).thenReturn(Optional.of("customer"));

        mvc.perform(get("/delivery/order/1/issue")
                        .header("authorizationId", 77L))
                .andExpect(status().isForbidden());
        verify(usersMicroservice, times(1)).getUserType(any());
    }

    @Test
    void getIssueOfDeliveryIssueDoesNotExist() throws Exception{
        when(usersMicroservice.getUserType(any())).thenReturn(Optional.of("customer"));

        mvc.perform(get("/delivery/order/1234/issue")
                        .header("authorizationId", 4567L))
                .andExpect(status().isNotFound());
        verify(usersMicroservice, times(1)).getUserType(any());
    }

}
