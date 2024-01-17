package nl.tudelft.sem.template.example.controller_mockmvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.example.Application;
import nl.tudelft.sem.template.example.TestDatabaseLoader;
import nl.tudelft.sem.template.example.configuration.ConfigurationProperties;
import nl.tudelft.sem.template.example.external.OrdersMicroservice;
import nl.tudelft.sem.template.example.external.UsersMicroservice;
import nl.tudelft.sem.template.model.DeliveryPostRequest;
import nl.tudelft.sem.template.model.Issue;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Rating;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = Application.class)
public class SystemMockMvcTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDatabaseLoader testDatabaseLoader;

    @MockBean
    private UsersMicroservice usersMicroservice;

    @MockBean
    private OrdersMicroservice ordersMicroservice;

    @Autowired
    private ConfigurationProperties configurationProperties;

    DeliveryPostRequest dummyDeliveryPostRequest;

    DeliveryPostRequest dummyDeliveryPostRequest2;


    @BeforeEach
    public void setUp() {
        testDatabaseLoader.loadTestData();

        Integer authorizationId = 5;
        dummyDeliveryPostRequest = new DeliveryPostRequest();
        dummyDeliveryPostRequest.setVendorId(55);
        dummyDeliveryPostRequest.setOrderId(66);
        dummyDeliveryPostRequest.setCustomerId(77);
        dummyDeliveryPostRequest.setDestination(new Location(13.0, 13.0));

        dummyDeliveryPostRequest2 = new DeliveryPostRequest();
        dummyDeliveryPostRequest2.setVendorId(111);
        dummyDeliveryPostRequest2.setOrderId(2020);
        dummyDeliveryPostRequest2.setCustomerId(712);
        dummyDeliveryPostRequest2.setDestination(new Location(13.0, 13.0));

        when(usersMicroservice.getUserType(55L)).thenReturn(Optional.of("vendor"));
        when(usersMicroservice.getUserType(77L)).thenReturn(Optional.of("customer"));
        when(usersMicroservice.getUserType(1010L)).thenReturn(Optional.of("admin"));

        when(usersMicroservice.getUserType(4444L)).thenReturn(Optional.of("courier"));
        when(usersMicroservice.getUserType(7777L)).thenReturn(Optional.of("courier"));
        when(usersMicroservice.getUserType(1111L)).thenReturn(Optional.of("courier"));

        when(usersMicroservice.getVendorLocation(any())).thenReturn(Optional.of(new Location(2.0, 3.0)));

        when(ordersMicroservice.putOrderStatus(any(), any(), any())).thenReturn(true);
    }

    @AfterEach
    public void tearDown() {
        testDatabaseLoader.clearTestData();}

    /**
     * This test goes through the entire life cycle of a delivery.
     */
    @Test
    public void fullWalkThroughTest() throws Exception {

        // (1) create a delivery in our system
        mvc.perform(post("/delivery/")
                        .header("authorizationId", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dummyDeliveryPostRequest)))
                .andExpect(status().isOk());

        //(2) change the order status to "Accepted"
        String status = "Accepted";

        mvc.perform(put("/delivery/order/66/status")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(status)))
                .andExpect(status().isOk());

        //(3) get the updated order status
        mvc.perform(get("/delivery/order/66/status")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //(4) add a second delivery in our system
        mvc.perform(post("/delivery/")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dummyDeliveryPostRequest2)))
                .andExpect(status().isOk());

        //(5) get all available-orders, which should only be 1, as only 1 has the "Accepted" status
        mvc.perform(get("/courier/delivery/4444/available-orders")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dummyDeliveryPostRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json("[66]"));

        //(6) assign the order to a courier
        mvc.perform(put("/courier/delivery/4444/assign/66")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dummyDeliveryPostRequest)))
                .andExpect(status().isOk());

        //(7) mark status as "Preparing"
        status = "Preparing";
        mvc.perform(put("/delivery/order/66/status")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(status)))
                .andExpect(status().isOk());

        //(8) put time when order was ready for pick-up
        mvc.perform(put("/delivery/order/66/ready-time")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("2023-07-16T19:51:34.000Z")))
                .andExpect(status().isOk());

        //(9) get the time when order was ready for pick-up
        mvc.perform(get("/delivery/order/66/ready-time")
                        .header("authorizationId", 1010L))
                .andExpect(status().isOk());

        //(10) mark status as "Given_To_Courier"
        status = "Given_To_Courier";
        mvc.perform(put("/delivery/order/66/status")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(status)))
                .andExpect(status().isOk());

        //(11) put the pick-up time of the order
        mvc.perform(put("/delivery/order/66/pickup-time")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("2023-07-16T19:51:34.000Z")))
                .andExpect(status().isOk());

        //(12) get the pick-up time of the order
        mvc.perform(get("/delivery/order/66/pickup-time")
                        .header("authorizationId", 1010L))
                .andExpect(status().isOk());

        //(13) set the order status to be on-transit
        status = "On_Transit";
        mvc.perform(put("/delivery/order/66/status")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(status)))
                .andExpect(status().isOk());

        //(14) get the current location of an order
        mvc.perform(get("/delivery/order/66/location")
                        .header("authorizationId", 1111L))
                .andExpect(status().isForbidden());

        //(15) set the order status to be delivered
        status = "Delivered";
        mvc.perform(put("/delivery/order/66/status")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(status)))
                .andExpect(status().isOk());


        //(16) set the delivered time of the order
        mvc.perform(put("/delivery/order/66/tod")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("2023-07-16T19:51:34.000Z")))
                .andExpect(status().isOk());

        //(17) get the delivered time of the order
        mvc.perform(get("/delivery/order/66/tod")
                        .header("authorizationId", 1010L))
                .andExpect(status().isOk());

        //(18) put an issue in the delivery
        mvc.perform(put("/delivery/order/66/issue")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Issue("Faulty delivery","The food was dropped on the floor"))))
                .andExpect(status().isOk());

        //(19) get the issue associated with the delivery
        mvc.perform(get("/delivery/order/66/issue")
                        .header("authorizationId", 1010L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(new Issue("Faulty delivery","The food was dropped on the floor"))));


        //(20) rate an order
        mvc.perform(put("/analytics/order/66/rating")
                        .header("authorizationId", 77L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Rating(5, "The food was great!"))))
                .andExpect(status().isOk());

        //(21) get the number of successful deliveries of a courier
        mvc.perform(get("/analytics/courier/4444/successful-deliveries")
                        .header("authorizationId", 1010L))
                .andExpect(status().isOk())
                .andExpect(content().json("1"));
    }

    /**
     * This test checks if the endpoints that are not only related to a delivery
     */
    @Test
    void miscellaneousEndpointsTest() throws Exception {
        //(1) set the default delivery zone as an admin
        mvc.perform(put("/delivery/default-delivery-zone")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(303L)))
                .andExpect(status().isOk());

        //(2) get the default delivery zone
        mvc.perform(get("/delivery/default-delivery-zone")
                        .header("authorizationId", 1010L))
                .andExpect(status().isOk())
                .andExpect(content().json("303"));

        //(2) post 2 deliveries and mark their statuses as "Accepted"
        mvc.perform(post("/delivery/")
                        .header("authorizationId", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dummyDeliveryPostRequest)))
                .andExpect(status().isOk());

        String status = "Accepted";

        mvc.perform(put("/delivery/order/66/status")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(status)))
                .andExpect(status().isOk());

        //(3) assign a courier to any order
        mvc.perform(put("/courier/delivery/4444/assign-any-order")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //(4) get the courier assigned to an order
        mvc.perform(get("/delivery/order/66/courier")
                        .header("authorizationId", 1010L))
                .andExpect(status().isOk())
                .andExpect(content().json("4444"));

        //(4) assign couriers to a vendor
        mvc.perform(put("/vendor/delivery/55/assign/7777")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //(5) update the vendor delivery zone
        mvc.perform(put("/vendor/delivery/55/delivery-zone")
                        .header("authorizationId", 1010L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(404L)))
                .andExpect(status().isOk());

        //(6) get the vendor delivery zone
        mvc.perform(get("/vendor/delivery/55/delivery-zone")
                        .header("authorizationId", 1010L))
                .andExpect(status().isOk())
                .andExpect(content().json("404"));

        //(7) get the vendor address
        mvc.perform(get("/vendor/delivery/55/vendor-address")
                        .header("authorizationId", 1010L))
                .andExpect(status().isOk());

        //(8) get the efficiency of a courier
        mvc.perform(get("/analytics/courier/7777/efficiency")
                        .header("authorizationId", 1010L))
                .andExpect(status().isOk())
                .andExpect(content().json("0"));

    }
}
