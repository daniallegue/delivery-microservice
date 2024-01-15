package nl.tudelft.sem.template.example.system;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import nl.tudelft.sem.template.example.WireMockConfig;
import nl.tudelft.sem.template.model.DeliveryPostRequest;
import nl.tudelft.sem.template.model.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * This class tests the system as a whole.
 * <p>
 * To test that the system works with the provided examples in
 * the other microservices' api specifications, we mock them with WireMock.
 * In this case, for the tests to pass, Application.java in the Delivery Microservice must be running.
 * <p>
 * To check that our system works with the actual microservices, we comment out the mocking.
 * In this case, for the tests to pass, Application.java in the Delivery Microservice,
 * Orders Microservice and UsersMicroservice must be running.
 */
public class SystemTest {

    private RestTemplate restTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    String BASE_URL = "http://localhost:8080";

    @BeforeEach
    public void setup() throws JsonProcessingException {
        restTemplate = new RestTemplate();

        //UNCOMMENT THESE LINES TO CHECK THAT WITH MOCKING REQUESTS ARE SUCCESSFUL
//        mockUsersMicroservice();
//        mockOrdersMicroservice();
    }

    @AfterEach
    public void tearDown() {
//        WireMockConfig.stopUserServer();
//        WireMockConfig.stopOrdersServer();
    }

    /**
     * Mocking the Users Microservice.
     */
    public void mockUsersMicroservice() throws JsonProcessingException {
        WireMockConfig.startUserServer();
        WireMockServer usersMicroservice = WireMockConfig.usersMicroservice;

        // Mocking the endpoint localhost:8081/vendor/4/location
        Location location = new Location(52.0116, 4.3571);
        usersMicroservice.stubFor(get(urlPathEqualTo("/vendor/4/location"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody(objectMapper.writeValueAsString(location))));

        // Mocking the endpoint localhost:8081/vendor/5/location
        location = new Location(1.0, 3.0);
        usersMicroservice.stubFor(get(urlPathEqualTo("/vendor/5/location"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody(objectMapper.writeValueAsString(location))));

        // Mocking the endpoint localhost:8081/vendor/5/location
        usersMicroservice.stubFor(get(urlPathEqualTo("/user/5"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody("vendor")));
    }

    /**
     * Mocking the Orders Microservice.
     */
    public void mockOrdersMicroservice(){
        WireMockConfig.startOrdersServer();
        WireMockServer ordersMicroservice = WireMockConfig.ordersMicroservice;

        // Mocking the endpoint localhost:8082/order/4/status
        ordersMicroservice.stubFor(put(urlPathEqualTo("/order/4000/status/5"))
                .withQueryParam("status", equalTo("Accepted"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())));
    }

    /**
     * Series of requests to create a vendor in the Users Microservice database.
     */
    public ResponseEntity<String> createVendor(String requestBody){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        String url = "http://localhost:8081/vendor/vendor/application/";
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
    }

    /**
     * Series of requests to create a customer in the Users Microservice database.
     */
    public ResponseEntity<String> createCustomer(String requestBody){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        String url = "http://localhost:8081/customer";
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
    }

    /**
     * Series of requests to create an order in the Orders Microservice database.
     */
    public ResponseEntity<String> createOrder(String requestBody, String customerId, String vendorId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        String url = "http://localhost:8082/order/new/" + customerId + "/" + vendorId;
        System.out.println(url);
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
    }

    /**
     * Creates the headers for the requests within our own server.
     */
    public HttpHeaders createHeaders(String authorizationId){
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorizationId",authorizationId);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }

    /**
     * Series of requests to create a delivery in our own Microservice.
     */
//    @Test
    public void testCreateDelivery() throws JsonProcessingException {
        //UNCOMMENT THIS LINE TO MAKE THE ACTUAL NECESSARY SERIES OF REQUESTS BEFORE THE CALL TO THIS ENDPOINT
        ResponseEntity<String> creatingVendorEntity = createVendor(VENDOR_JSON_1);
        Assertions.assertTrue(creatingVendorEntity.getStatusCode().is2xxSuccessful());

        DeliveryPostRequest dummyDeliveryPostRequest = new DeliveryPostRequest(409, 123,
                new Location(4.0, 5.0), 4);
        String requestBody =  objectMapper.writeValueAsString(dummyDeliveryPostRequest);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, createHeaders("4"));

        ResponseEntity<String> responseEntity = restTemplate.exchange(BASE_URL + "/delivery", HttpMethod.POST, requestEntity, String.class);
        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    }

    /**
     * Series of requests to change the order status.
     */
//    @Test
    public void changeOrderStatus() throws JsonProcessingException {
        //(1) create a vendor in the Users Microservice database
        ResponseEntity<String> creatingVendorEntity = createVendor(VENDOR_JSON_1);
        Assertions.assertTrue(creatingVendorEntity.getStatusCode().is2xxSuccessful());

        //(2) create a customer in the Users Microservice database
       ResponseEntity<String> creatingCustomerEntity = createCustomer(CUSTOMER_JSON_1);
       Assertions.assertTrue(creatingCustomerEntity.getStatusCode().is2xxSuccessful());

        //(3) create an order in the Orders Microservice, which, in theory, should post a delivery in our database
        ResponseEntity<String> creatingOrderEntity = createOrder(ORDER_JSON_1, "123", "5");
        Assertions.assertTrue(creatingOrderEntity.getStatusCode().is2xxSuccessful());

        //(3.2) create a delivery in our database, if the other microservice did not do it
        DeliveryPostRequest dummyDeliveryPostRequest = new DeliveryPostRequest(4000, 123,
                new Location(4.0, 5.0), 5);
        String requestBody =  objectMapper.writeValueAsString(dummyDeliveryPostRequest);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, createHeaders("5"));
        ResponseEntity<String> responseEntity = restTemplate.exchange(BASE_URL + "/delivery", HttpMethod.POST, requestEntity, String.class);
        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

        //(4) change the order status (both in our microservice and in the Orders microservice
        String url2 = BASE_URL + "delivery/order/4000/status";
        HttpEntity<String> requestEntity2 = new HttpEntity<>("Accepted",createHeaders("5"));
        ResponseEntity<String> responseEntity2 = restTemplate.exchange(url2, HttpMethod.PUT, requestEntity2, String.class);
        Assertions.assertTrue(responseEntity2.getStatusCode().is2xxSuccessful());
    }



    //JSON examples taken from their respective microservices' api specifications
    String VENDOR_JSON_1 = "{\n" +
            "  \"id\": 4,\n" +
            "  \"name\": \"Una\",\n" +
            "  \"surname\": \"Jacimovic\",\n" +
            "  \"email\": \"una_jacimovic@gmail.com\",\n" +
            "  \"deliveryZone\": 50,\n" +
            "  \"location\": {\n" +
            "    \"latitude\": 52.0116,\n" +
            "    \"longitude\": 4.3571\n" +
            "  },\n" +
            "  \"openingHours\": {\n" +
            "    \"monday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    },\n" +
            "    \"tuesday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    },\n" +
            "    \"wednesday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    },\n" +
            "    \"thursday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    },\n" +
            "    \"friday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    },\n" +
            "    \"saturday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    },\n" +
            "    \"sunday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"proof\": \"These are the documents from the KvK confirming my ownership\"\n" +
            "}";
    String VENDOR_JSON_2 = "{\n" +
            "  \"id\": 5,\n" +
            "  \"name\": \"Una\",\n" +
            "  \"surname\": \"Jacimovic\",\n" +
            "  \"email\": \"una_jacimovic@gmail.com\",\n" +
            "  \"deliveryZone\": 50,\n" +
            "  \"location\": {\n" +
            "    \"latitude\": 52.0116,\n" +
            "    \"longitude\": 4.3571\n" +
            "  },\n" +
            "  \"openingHours\": {\n" +
            "    \"monday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    },\n" +
            "    \"tuesday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    },\n" +
            "    \"wednesday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    },\n" +
            "    \"thursday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    },\n" +
            "    \"friday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    },\n" +
            "    \"saturday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    },\n" +
            "    \"sunday\": {\n" +
            "      \"open\": \"09:00\",\n" +
            "      \"close\": \"1320\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"proof\": \"These are the documents from the KvK confirming my ownership\"\n" +
            "}";

    String CUSTOMER_JSON_1 = "{\n" +
            "  \"name\": \"John\",\n" +
            "  \"surname\": \"Doe\",\n" +
            "  \"email\": \"john.doe@example.com\",\n" +
            "  \"homeAddress\": {\n" +
            "    \"latitude\": 52.0116,\n" +
            "    \"longitude\": 4.3571\n" +
            "  }\n" +
            "}";
    String ORDER_JSON_1 =  "{\n" +
            "  \"order_id\": 4000,\n" +
            "  \"customer_id\": 123,\n" +
            "  \"vendor_id\": 5,\n" +
            "  \"price\": 20.3,\n" +
            "  \"dishes\": [\n" +
            "    {\n" +
            "      \"id\": 10,\n" +
            "      \"name\": \"pepperoni pizza\",\n" +
            "      \"allergens\": [\n" +
            "        \"string\"\n" +
            "      ],\n" +
            "      \"price\": 0\n" +
            "    }\n" +
            "  ],\n" +
            "  \"time\": \"Today at 19:00\",\n" +
            "  \"location\": {\n" +
            "    \"id\": 34,\n" +
            "    \"latitude\": 0,\n" +
            "    \"longitude\": 0\n" +
            "  },\n" +
            "  \"specialRequirenments\": \"No french fries with my burger\",\n" +
            "  \"rating_id\": 2023,\n" +
            "  \"status\": \"Pending\"\n" +
            "}";

}
