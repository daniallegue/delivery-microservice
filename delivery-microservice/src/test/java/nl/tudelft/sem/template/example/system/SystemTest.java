package nl.tudelft.sem.template.example.system;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import nl.tudelft.sem.template.example.WireMockConfig;
import nl.tudelft.sem.template.model.DeliveryPostRequest;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Rating;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
//        mockOrdersMicroservice();
    }

    @AfterEach
    public void tearDown() {
//        WireMockConfig.stopOrdersServer();
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
     * Series of requests to create an admin in the Users Microservice
     */
    public ResponseEntity<String> createAdmin(String requestBody){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        String url = "http://localhost:8081/admin";
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
    }

    /**
     * Series of requests to create a vendor in the Users Microservice database.
     */
    public ResponseEntity<String> createVendor(String requestBody, Integer adminId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        String url = "http://localhost:8081/vendor/application/";
        restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        System.out.println(adminId);
        headers.set("adminId", adminId.toString());
        HttpEntity<String> requestEntity2 = new HttpEntity<>(requestBody, headers);

        url = "http://localhost:8081/admin/create-vendor";
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity2, String.class);
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
     * Series of requests to create a courier in the Users Microservice database.
     */
    public ResponseEntity<String> createCourier(String requestBody){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        String url = "http://localhost:8081/courier";
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
     * <b> User story 1 - Create a delivery</b>
     * <ol>
     * <li>create an admin in the Users Microservice database</li>
     * <li>create a vendor in the Users Microservice database</li>
     * <li>create a delivery in our database</li>
     * </ol>
     */
//    @Test
    public void createDelivery() throws JsonProcessingException {
        //(1) create an admin in the Users Microservice database
        ResponseEntity<String> creatingAdminEntity = createAdmin(ADMIN_JSON);
        Assertions.assertTrue(creatingAdminEntity.getStatusCode().is2xxSuccessful());

        //(2) create a vendor in the Users Microservice database
        ResponseEntity<String> creatingVendorEntity = createVendor(VENDOR_JSON_1,  extractIdFromResponse(creatingAdminEntity));
        Assertions.assertTrue(creatingVendorEntity.getStatusCode().is2xxSuccessful());

        //(3) create a delivery in our database, which makes a call to the Users Microservice
        DeliveryPostRequest dummyDeliveryPostRequest = new DeliveryPostRequest(409, 123,
                new Location(4.0, 5.0), extractIdFromResponse(creatingVendorEntity));
        String requestBody =  objectMapper.writeValueAsString(dummyDeliveryPostRequest);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, createHeaders(extractIdFromResponse(creatingVendorEntity).toString()));

        ResponseEntity<String> responseEntity = restTemplate.exchange(BASE_URL + "/delivery", HttpMethod.POST, requestEntity, String.class);
        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    }

    /**
     * <b> User story 2 - Try to rate an order as an authorized user</b>
     * <ol>
     * <li>create an admin in the Users Microservice database</li>
     * <li>create a vendor in the Users Microservice database</li>
     * <li>create a delivery in our database</li>
     * <li>rate the order from the delivery</li>
     * </ol>
     */
//    @Test
    public void ratingIsForbidden() throws JsonProcessingException {
        //(1) create an admin in the Users Microservice database
        ResponseEntity<String> creatingAdminEntity = createAdmin(ADMIN_JSON_2);
        Assertions.assertTrue(creatingAdminEntity.getStatusCode().is2xxSuccessful());

        //(1) create a vendor in the Users Microservice database
        ResponseEntity<String> creatingVendorEntity = createVendor(VENDOR_JSON_2,extractIdFromResponse(creatingAdminEntity));
        Assertions.assertTrue(creatingVendorEntity.getStatusCode().is2xxSuccessful());

        //(2) create a customer in the Users Microservice database
        ResponseEntity<String> creatingCustomerEntity = createCustomer(CUSTOMER_JSON_1);
        Assertions.assertTrue(creatingCustomerEntity.getStatusCode().is2xxSuccessful());


        //(3) create a delivery in our database
        DeliveryPostRequest dummyDeliveryPostRequest = new DeliveryPostRequest(444, extractIdFromResponse(creatingCustomerEntity),
                new Location(4.0, 5.0), extractIdFromResponse(creatingVendorEntity));
        String requestBody =  objectMapper.writeValueAsString(dummyDeliveryPostRequest);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, createHeaders(extractIdFromResponse(creatingAdminEntity).toString()));
        ResponseEntity<String> responseEntity = restTemplate.exchange(BASE_URL + "/delivery", HttpMethod.POST, requestEntity, String.class);
        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

        //(4) try to add a rating to the order, it will fail, because the admin is not authorized to do so
        Rating rating = new Rating(5, "yummy");
        String requestBody2 =  objectMapper.writeValueAsString(rating);
        HttpEntity<String> requestEntity2 = new HttpEntity<>(requestBody2, createHeaders(extractIdFromResponse(creatingAdminEntity).toString()));
        ResponseEntity<String> responseEntity2;
        try{
            responseEntity2 = restTemplate.exchange(BASE_URL + "/analytics/order/" + 444 + "/rating", HttpMethod.PUT, requestEntity, String.class);}
        catch (Exception e){
            assertThat(e.getMessage()).contains("401");
        }
    }

    /**
     * Series of requests to change the order status, involving both microservices.
     */
//    @Test
    public void changeOrderStatus() throws JsonProcessingException {
        //(1) create an admin in the Users Microservice database
        ResponseEntity<String> creatingAdminEntity = createAdmin(ADMIN_JSON_2);
        Assertions.assertTrue(creatingAdminEntity.getStatusCode().is2xxSuccessful());

        //(1) create a vendor in the Users Microservice database
        ResponseEntity<String> creatingVendorEntity = createVendor(VENDOR_JSON_2,extractIdFromResponse(creatingAdminEntity));
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

//    @Test
    public void courierActions() throws JsonProcessingException {
        //(1) create a vendor in the Users Microservice database
        ResponseEntity<String> creatingVendorEntity = createVendor(VENDOR_JSON_1, 100);
        Assertions.assertTrue(creatingVendorEntity.getStatusCode().is2xxSuccessful());

        //(2) create couriers in the Users Microservice database
        ResponseEntity<String> creatingCourierEntity1 = createCourier(COURIER_JSON_1);
        Assertions.assertTrue(creatingCourierEntity1.getStatusCode().is2xxSuccessful());

        ResponseEntity<String> creatingCourierEntity2 = createCourier(COURIER_JSON_2);
        Assertions.assertTrue(creatingCourierEntity2.getStatusCode().is2xxSuccessful());

        //(3) create a customer in the Users Microservice database
        ResponseEntity<String> creatingCustomerEntity = createCustomer(CUSTOMER_JSON_1);
        Assertions.assertTrue(creatingCustomerEntity.getStatusCode().is2xxSuccessful());

        //(4) create an order in the Orders Microservice, which, in theory, should post a delivery in our database
        ResponseEntity<String> creatingOrderEntity = createOrder(ORDER_JSON_1, "123", "5");
        Assertions.assertTrue(creatingOrderEntity.getStatusCode().is2xxSuccessful());

        //(4.2) create a delivery in our database, if the other microservice did not do it
        DeliveryPostRequest dummyDeliveryPostRequest = new DeliveryPostRequest(4000, 123,
                new Location(4.0, 5.0), 5);
        String requestBody =  objectMapper.writeValueAsString(dummyDeliveryPostRequest);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, createHeaders("5"));
        ResponseEntity<String> responseEntity = restTemplate.exchange(BASE_URL + "/delivery", HttpMethod.POST, requestEntity, String.class);
        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

        //(5) change the order status (both in our microservice and in the Orders microservice
        String url2 = BASE_URL + "delivery/order/4000/status";
        HttpEntity<String> requestEntity2 = new HttpEntity<>("Accepted",createHeaders("5"));
        ResponseEntity<String> responseEntity2 = restTemplate.exchange(url2, HttpMethod.PUT, requestEntity2, String.class);
        Assertions.assertTrue(responseEntity2.getStatusCode().is2xxSuccessful());

    }

    /**
     * Extracts the id from the response of a request to the Users Microservice.
     * @param responseEntity The response of the request.
     * @return The id of the user.
     */
    public Integer extractIdFromResponse(ResponseEntity<String> responseEntity) throws JsonProcessingException {
        String responseJson = responseEntity.getBody();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(responseJson);
        return jsonNode.get("id").asInt();
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
            "  \"id\": 666,\n" +
            "  \"name\": \"bla\",\n" +
            "  \"surname\": \"bla\",\n" +
            "  \"email\": \"bla_bla@gmail.com\",\n" +
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
            "  \"proof\": \"These are twefefhe documents from the KvK confirming my ownership\"\n" +
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

    String COURIER_JSON_1 = "{\n" +
            "  \"id\": 10,\n" +
            "  \"name\": \"Peter\",\n" +
            "  \"surname\": \"Peterity\",\n" +
            "  \"email\": \"peter@gmail.com\",\n" +
            "  \"validated\": true,\n" +
            "  \"completedOrders\": [\n" +
            "    1\n" +
            "  ],\n" +
            "  \"paymentMethod\": \"cash\"\n" +
            "}";

    String COURIER_JSON_2 = "{\n" +
            "  \"id\": 15,\n" +
            "  \"name\": \"Bruh\",\n" +
            "  \"surname\": \"Bruh\",\n" +
            "  \"email\": \"burh@bruh.com\",\n" +
            "  \"validated\": true,\n" +
            "  \"completedOrders\": [\n" +
            "    1\n" +
            "  ],\n" +
            "  \"paymentMethod\": \"cash\"\n" +
            "}";

    String ADMIN_JSON ="{\n" +
            "  \"id\": 100,\n" +
            "  \"name\": \"Peter\",\n" +
            "  \"surname\": \"Peterity\",\n" +
            "  \"email\": \"peter@gmail.com\"\n" +
            "}";
    String ADMIN_JSON_2 ="{\n" +
            "  \"id\": 1001,\n" +
            "  \"name\": \"Bruh\",\n" +
            "  \"surname\": \"Bruh\",\n" +
            "  \"email\": \"bruh@gmail.com\"\n" +
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
