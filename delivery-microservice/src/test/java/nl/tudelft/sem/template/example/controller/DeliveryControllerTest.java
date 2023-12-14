package nl.tudelft.sem.template.example.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.example.Application;
import nl.tudelft.sem.template.example.exception.OrderAlreadyExistsException;
import nl.tudelft.sem.template.example.service.DeliveryService;
import nl.tudelft.sem.template.model.DeliveryPostRequest;
import nl.tudelft.sem.template.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import javax.transaction.Transactional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = Application.class)
@Transactional
public class DeliveryControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private DeliveryService deliveryService;

    private DeliveryPostRequest dummyDeliveryPostRequest;
    String jsonDeliveryPostRequest;


    @BeforeEach
    void setup() throws JsonProcessingException {
        dummyDeliveryPostRequest =  new DeliveryPostRequest();
        dummyDeliveryPostRequest.setVendorId(1);
        dummyDeliveryPostRequest.setOrderId(123);
        dummyDeliveryPostRequest.setCustomerId(456);
        dummyDeliveryPostRequest.setDestination(new Location(4.0, 5.0));
        jsonDeliveryPostRequest = new ObjectMapper().writeValueAsString(dummyDeliveryPostRequest);
    }

    @Test
    public void testDeliveryPostSuccess() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/delivery")
                        .header("authorizationId", String.valueOf(1))
                        .content(jsonDeliveryPostRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    public void testDeliveryPostBadRequest() throws Exception {
        when(deliveryService.createDelivery(dummyDeliveryPostRequest)).thenThrow(OrderAlreadyExistsException.class);
        mvc.perform(MockMvcRequestBuilders.post("/delivery")
                        .header("authorizationId", String.valueOf(1))
                        .content(jsonDeliveryPostRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}
