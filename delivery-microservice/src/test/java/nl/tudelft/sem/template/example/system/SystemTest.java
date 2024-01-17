package nl.tudelft.sem.template.example.system;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import nl.tudelft.sem.template.model.DeliveryPostRequest;
import nl.tudelft.sem.template.model.Issue;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Rating;
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
 * To check that our system works with the actual microservices, we comment out the mocking.
 * In this case, for the tests to pass, Application.java in the Delivery Microservice,
 * Orders Microservice and UsersMicroservice must be running.
 */
public class SystemTest {

    private RestTemplate restTemplate;

    ObjectMapper objectMapper = new ObjectMapper();
    WireMockServer ordersMicroservice;

    String BASE_URL = "http://localhost:8080";

    @BeforeEach
    public void setup() throws JsonProcessingException {
        restTemplate = new RestTemplate();
    }

    /**
     * Mocking the Orders Microservice's endpoint:
     * localhost:8082/order/{orderId}/status/{userId}?status={status}
     */
    public void mockPutStatus(Integer orderId, Integer userId, String status){
        System.out.println("/order/"+orderId + "/status/ + " + userId);
        ordersMicroservice.stubFor(put(urlPathEqualTo("/order/" + orderId + "/status/" + userId))
                .withQueryParam("status", equalTo(status))
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
    public ResponseEntity<String> createCourier(String requestBody, Integer adminId) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        String url = "http://localhost:8081/courier";
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

//        headers.set("adminId", adminId.toString());
//        HttpEntity<String> requestEntity2 = new HttpEntity<>("I am TU Delft student that wants to earn money.", headers);
//        String url2 = "http://localhost:8081/admin/verify-courier/" + extractIdFromResponse(creatingCourierEntity);
//        return restTemplate.exchange(url2, HttpMethod.POST, requestEntity2, String.class);
    }

    /**
     * Series of requests to create an order in the Orders Microservice database.
     */
    public ResponseEntity<String> createOrder(String customerId, String vendorId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        String url = "http://localhost:8082/order/new/" + customerId + "/" + vendorId;
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
    @Test
    public void createDelivery() throws JsonProcessingException {
        //(1) create an admin in the Users Microservice database
        ResponseEntity<String> creatingAdminEntity = createAdmin(ADMIN_JSON);

        //(2) create a vendor in the Users Microservice database
        ResponseEntity<String> creatingVendorEntity = createVendor(VENDOR_JSON_1,  extractIdFromResponse(creatingAdminEntity));

        //(3) create a delivery in our database, which makes a call to the Users Microservice
        DeliveryPostRequest dummyDeliveryPostRequest = new DeliveryPostRequest(409, 123,
                new Location(4.0, 5.0), extractIdFromResponse(creatingVendorEntity));
        String requestBody =  objectMapper.writeValueAsString(dummyDeliveryPostRequest);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, createHeaders(extractIdFromResponse(creatingVendorEntity).toString()));

        ResponseEntity<String> responseEntity = restTemplate.exchange(BASE_URL + "/delivery", HttpMethod.POST, requestEntity, String.class);
    }

    /**
     * <b> User story 2 - Try to rate an order as an authorized user</b>
     * <ol>
     * <li>create an admin in the Users Microservice database </li>
     * <li>create a vendor in the Users Microservice database </li>
     * <li>create a customer in the Users Microservice database </li>
     * <li>create a delivery in our database</li>
     * <li>rate the order from the delivery</li>
     * </ol>
     */
    @Test
    public void ratingIsForbidden() throws JsonProcessingException {
        //(1) create an admin in the Users Microservice database
        ResponseEntity<String> creatingAdminEntity = createAdmin(ADMIN_JSON_2);

        //(2) create a vendor in the Users Microservice database
        ResponseEntity<String> creatingVendorEntity = createVendor(VENDOR_JSON_2,extractIdFromResponse(creatingAdminEntity));

        //(3) create a customer in the Users Microservice database
        ResponseEntity<String> creatingCustomerEntity = createCustomer(CUSTOMER_JSON_1);

        //(4) create a delivery in our database
        DeliveryPostRequest dummyDeliveryPostRequest = new DeliveryPostRequest(444, extractIdFromResponse(creatingCustomerEntity),
                new Location(4.0, 5.0), extractIdFromResponse(creatingVendorEntity));
        String requestBody =  objectMapper.writeValueAsString(dummyDeliveryPostRequest);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, createHeaders(extractIdFromResponse(creatingAdminEntity).toString()));
        ResponseEntity<String> responseEntity = restTemplate.exchange(BASE_URL + "/delivery", HttpMethod.POST, requestEntity, String.class);

        //(5) try to add a rating to the order, it will fail, because the admin is not authorized to do so
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
     * <b>User story 3 - Change the status of an order</b>
     * <ol>
     * <li>create an admin in the Users Microservice database</li>
     * <li>create a vendor in the Users Microservice database</li>
     * <li>create a customer in the Users Microservice database</li>
     * <li>create a delivery in our database</li>
     * <li>change the status of the order</li>
     * </ol>
     */
    @Test
    public void changeOrderStatus() throws JsonProcessingException {
        Integer orderId = 202020;
        //(1) create an admin in the Users Microservice database
        ResponseEntity<String> creatingAdminEntity = createAdmin(ADMIN_JSON_3);


        //(1) create a vendor in the Users Microservice database
        ResponseEntity<String> creatingVendorEntity = createVendor(VENDOR_JSON_3,extractIdFromResponse(creatingAdminEntity));


        //(2) create a customer in the Users Microservice database
        ResponseEntity<String> creatingCustomerEntity = createCustomer(CUSTOMER_JSON_2);

        //(3) create an order in the Orders Microservice, which, in theory, should post a delivery in our database
        ResponseEntity<String> creatingOrderEntity = createOrder(extractIdFromResponse(creatingCustomerEntity).toString(), extractIdFromResponse(creatingVendorEntity).toString());
        orderId = extractOrderIdFromResponse(creatingOrderEntity);

        //(3) create a delivery in our database, if the Orders microservice did not do it
        DeliveryPostRequest dummyDeliveryPostRequest = new DeliveryPostRequest(orderId, extractIdFromResponse(creatingCustomerEntity),
                new Location(4.0, 5.0), extractIdFromResponse(creatingVendorEntity));
        String requestBody =  objectMapper.writeValueAsString(dummyDeliveryPostRequest);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, createHeaders(extractIdFromResponse(creatingAdminEntity).toString()));
        ResponseEntity<String> responseEntity = restTemplate.exchange(BASE_URL + "/delivery", HttpMethod.POST, requestEntity, String.class);


        //(4) change the order status (both in our microservice and in the Orders microservice
        String url2 = BASE_URL + "delivery/order/" + orderId + "/status";
        HttpEntity<String> requestEntity2 = new HttpEntity<>("accepted",createHeaders(extractIdFromResponse(creatingVendorEntity).toString()));
        ResponseEntity<String> responseEntity2 = restTemplate.exchange(url2, HttpMethod.PUT, requestEntity2, String.class);
    }

    /**
     * <b>User story 3 - Change the delivery zone and vendor couriers</b>
     *
     * <ol>
     *     <li>create an admin in the Users Microservice database</li>
     *     <li>create a vendor in the Users Microservice database</li>
     *     <li>create 3 couriers in the Users Microservice database</li>
     *     <li>create a delivery in our database, which also creates the vendor if he does not yet exist</li>
     *     <li>try changing the delivery zone, which will not succeed because the vendor does not have any couriers</li>
     *     <li>assign couriers to the vendor</li>
     *     <li>change the delivery zone, which will now succeed because the vendor has couriers</li>
     * </ol>
     */
    @Test
    public void courierAndVendorActions() throws JsonProcessingException {
        //(1) create an admin in the Users Microservice database
        ResponseEntity<String> creatingAdminEntity = createAdmin(ADMIN_JSON_4);

        //(2) create a vendor in the Users Microservice database
        ResponseEntity<String> creatingVendorEntity = createVendor(VENDOR_JSON_4, extractIdFromResponse(creatingAdminEntity));

        //(3) create 3 couriers in the Users Microservice database
        ResponseEntity<String> creatingCourierEntity1 = createCourier(COURIER_JSON_1, extractIdFromResponse(creatingAdminEntity));

        ResponseEntity<String> creatingCourierEntity2 = createCourier(COURIER_JSON_2, extractIdFromResponse(creatingAdminEntity));

        ResponseEntity<String> creatingCourierEntity3 = createCourier(COURIER_JSON_3, extractIdFromResponse(creatingAdminEntity));

        //(4) create a delivery in our database, which also creates the vendor if he does not yet exist
        DeliveryPostRequest dummyDeliveryPostRequest = new DeliveryPostRequest(7777, 555,
                new Location(4.0, 5.0), extractIdFromResponse(creatingVendorEntity));
        String requestBody =  objectMapper.writeValueAsString(dummyDeliveryPostRequest);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, createHeaders(extractIdFromResponse(creatingVendorEntity).toString()));
        restTemplate.exchange(BASE_URL + "/delivery", HttpMethod.POST, requestEntity, String.class);

        //(5) try changing the delivery zone, which will not succeed because the vendor does not have any couriers
        String url = BASE_URL + "vendor/delivery/" + extractIdFromResponse(creatingVendorEntity) + "/delivery-zone";
        try{
            HttpEntity<String> requestEntity2 = new HttpEntity<>("7", createHeaders(extractIdFromResponse(creatingVendorEntity).toString()));
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity2, String.class);
        }
        catch (Exception e){
            //The vendor does not have any couriers, so it cannot change its delivery zone
            assertThat(e.getMessage()).contains("400");
        }

        //(6) assign couriers to the vendor
        String url2 = BASE_URL + "vendor/delivery/" + extractIdFromResponse(creatingVendorEntity) + "/assign/"; ;
        HttpEntity<String> requestEntity2 = new HttpEntity<>(createHeaders(extractIdFromResponse(creatingAdminEntity).toString()));
        restTemplate.exchange(url2 + extractIdFromResponse(creatingCourierEntity1), HttpMethod.PUT, requestEntity2, String.class);
        restTemplate.exchange(url2 + extractIdFromResponse(creatingCourierEntity2), HttpMethod.PUT, requestEntity2, String.class);

        //(7) change the delivery zone, which will now succeed because the vendor has couriers
        HttpEntity<String> requestEntity3 = new HttpEntity<>("7", createHeaders(extractIdFromResponse(creatingVendorEntity).toString()));
        restTemplate.exchange(url, HttpMethod.PUT, requestEntity3, String.class);
    }

    /**
     * <b>User story 4 - Walkthrough Delivery</b>
     * <ol>
     * <li>create an admin in the Users Microservice database</li>
     * <li>create an vendor in the Users Microservice database</li>
     * <li>create a courier in the Users Microservice database</li>
     * <li>create a delivery in our database/li>
     * <li>assign a courier to the order</li>
     * <li>get the estimated time of arrival of the order</li>
     * <li>mark an issue to the order</>
     * </ol>
     */
    @Test
    public void exampleWalkThroughDeliveryProcess() throws JsonProcessingException {
        //(1) create an admin in the Users Microservice database
        ResponseEntity<String> creatingAdminEntity = createAdmin(ADMIN_JSON_5);

        //(2) create a vendor in the Users Microservice database
        ResponseEntity<String> creatingVendorEntity = createVendor(VENDOR_JSON_5,extractIdFromResponse(creatingAdminEntity));

        //(3) create a customer in the Users Microservice database
        ResponseEntity<String> creatingCustomerEntity = createCustomer(CUSTOMER_JSON_3);

        //(4) create a courier in the Users Microservice database
        ResponseEntity<String> creatingCourierEntity = createCourier(COURIER_JSON_4, extractIdFromResponse(creatingAdminEntity));

        //(5) create a delivery in our database, if the other microservice did not do it
        DeliveryPostRequest dummyDeliveryPostRequest = new DeliveryPostRequest(88, extractIdFromResponse(creatingCustomerEntity),
                new Location(4.0, 5.0), extractIdFromResponse(creatingVendorEntity));
        String requestBody =  objectMapper.writeValueAsString(dummyDeliveryPostRequest);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, createHeaders(extractIdFromResponse(creatingVendorEntity).toString()));
        ResponseEntity<String> responseEntity = restTemplate.exchange(BASE_URL + "/delivery", HttpMethod.POST, requestEntity, String.class);

        //(6) assign a courier to the order
        String url3 = BASE_URL + "courier/delivery/" + extractIdFromResponse(creatingCourierEntity) + "/assign/88";
        HttpEntity<String> requestEntity3 = new HttpEntity<>(createHeaders(extractIdFromResponse(creatingVendorEntity).toString()));
        ResponseEntity<String> responseEntity3 = restTemplate.exchange(url3, HttpMethod.PUT, requestEntity3, String.class);

        //(7) get the estimated time of arrival of the delivery
        String url4 = BASE_URL + "delivery/order/" + 88 + "/eta";
        HttpEntity<String> requestEntity4 = new HttpEntity<>(createHeaders(extractIdFromResponse(creatingCustomerEntity).toString()));
        ResponseEntity<String> responseEntity4 = restTemplate.exchange(url4, HttpMethod.GET, requestEntity4, String.class);

        //(8) add an issue to the delivery
        String url5 = BASE_URL + "delivery/order/" + 88 + "/issue";
        Issue issue = new Issue("accident","I got into an accident while delivering");
        HttpEntity<String> requestEntity5 = new HttpEntity<>(objectMapper.writeValueAsString(issue),createHeaders(extractIdFromResponse(creatingCourierEntity).toString()));
        ResponseEntity<String> responseEntity5 = restTemplate.exchange(url5, HttpMethod.PUT, requestEntity5, String.class);
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

    /**
     * Extracts the order id from the response of a request to the Orders Microservice.
     * @param responseEntity The response of the request.
     * @return The id of the order.
     */
    public Integer extractOrderIdFromResponse(ResponseEntity<String> responseEntity)throws JsonProcessingException {
        String responseJson = responseEntity.getBody();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(responseJson);
        return jsonNode.get("order_id").asInt();
    }

    //JSON examples taken from their respective microservices' api specifications
    String VENDOR_JSON_1 = "{\n" +
            "  \"name\": \"Una\",\n" +
            "  \"surname\": \"Jacimovic\",\n" +
            "  \"email\": \"una_jacimovic@gmail.com\",\n" +
            "  \"deliveryZone\": 50,\n" +
            "  \"location\": {\n" +
            "    \"latitude\": 52.0116,\n" +
            "    \"longitude\": 4.3571\n" +
            "  },\n" +
            "  \"proof\": \"These are the documents from the KvK confirming my ownership\"\n" +
            "}";
    String VENDOR_JSON_2 = "{\n" +
            "  \"name\": \"bla\",\n" +
            "  \"surname\": \"bla\",\n" +
            "  \"email\": \"bla_bla@gmail.com\",\n" +
            "  \"deliveryZone\": 50,\n" +
            "  \"location\": {\n" +
            "    \"latitude\": 52.0116,\n" +
            "    \"longitude\": 4.3571\n" +
            "  },\n"  +
            "  \"proof\": \"These are twefefhe documents from the KvK confirming my ownership\"\n" +
            "}";
    String VENDOR_JSON_3 = "{\n" +
            "  \"name\": \"bla\",\n" +
            "  \"surname\": \"bla\",\n" +
            "  \"email\": \"bla_sdvfbla@gmadssil.com\",\n" +
            "  \"deliveryZone\": 50,\n" +
            "  \"location\": {\n" +
            "    \"latitude\": 52.0116,\n" +
            "    \"longitude\": 4.3571\n" +
            "  },\n"  +
            "  \"proof\": \"These are twefefhe documents from the KvK confirming my ownership\"\n" +
            "}";

    String VENDOR_JSON_4 = "{\n" +
            "  \"name\": \"bla\",\n" +
            "  \"surname\": \"bla\",\n" +
            "  \"email\": \"ayo@ayo.com\",\n" +
            "  \"deliveryZone\": 50,\n" +
            "  \"location\": {\n" +
            "    \"latitude\": 52.0116,\n" +
            "    \"longitude\": 4.3571\n" +
            "  },\n"  +
            "  \"proof\": \"Blablad\"\n" +
            "}";
    String VENDOR_JSON_5 = "{\n" +
            "  \"name\": \"pisi\",\n" +
            "  \"surname\": \"pisi\",\n" +
            "  \"email\": \"pisi@pisi.com\",\n" +
            "  \"location\": {\n" +
            "    \"latitude\": 52.0116,\n" +
            "    \"longitude\": 4.3571\n" +
            "  },\n"  +
            "  \"proof\": \"cleo\"\n" +
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
    String CUSTOMER_JSON_2 = "{\n" +
            "  \"name\": \"cleo\",\n" +
            "  \"surname\": \"cleo\",\n" +
            "  \"email\": \"cleo.cleo@cleo.com\",\n" +
            "  \"homeAddress\": {\n" +
            "    \"latitude\": 52.0116,\n" +
            "    \"longitude\": 4.3571\n" +
            "  }\n" +
            "}";
    String CUSTOMER_JSON_3 = "{\n" +
            "  \"name\": \"oscar\",\n" +
            "  \"surname\": \"oscar\",\n" +
            "  \"email\": \"oscar.oscar@oscar.com\",\n" +
            "  \"homeAddress\": {\n" +
            "    \"latitude\": 52.0116,\n" +
            "    \"longitude\": 4.3571\n" +
            "  }\n" +
            "}";

    String COURIER_JSON_1 = "{\n" +
            "  \"name\": \"Peter\",\n" +
            "  \"surname\": \"Peterity\",\n" +
            "  \"email\": \"peter@gmail.com\",\n" +
            "  \"paymentMethod\": \"cash\"\n" +
            "}";

    String COURIER_JSON_2 = "{\n" +
            "  \"name\": \"Bruh\",\n" +
            "  \"surname\": \"Bruh\",\n" +
            "  \"email\": \"burh@bruh.com\",\n" +
            "  \"paymentMethod\": \"cash\"\n" +
            "}";

    String COURIER_JSON_3 = "{\n" +
            "  \"name\": \"jajaja\",\n" +
            "  \"surname\": \"jajaja\",\n" +
            "  \"email\": \"jajaja@jajaja.com\",\n" +
            "  \"paymentMethod\": \"cash\"\n" +
            "}";

    String COURIER_JSON_4 = "{\n" +
            "  \"name\": \"haha\",\n" +
            "  \"surname\": \"haha\",\n" +
            "  \"email\": \"haha@haha.com\",\n" +
            "  \"paymentMethod\": \"cash\"\n" +
            "}";

    String ADMIN_JSON ="{\n"  +
            "  \"name\": \"Peter\",\n" +
            "  \"surname\": \"Peterity\",\n" +
            "  \"email\": \"peter@gmail.com\"\n" +
            "}";
    String ADMIN_JSON_2 ="{\n"  +
            "  \"name\": \"Bruh\",\n" +
            "  \"surname\": \"Bruh\",\n" +
            "  \"email\": \"bruh@gmail.com\"\n" +
            "}";
    String ADMIN_JSON_3 ="{\n" +
            "  \"name\": \"lol\",\n" +
            "  \"surname\": \"lol\",\n" +
            "  \"email\": \"lol@lol.com\"\n" +
            "}";
    String ADMIN_JSON_4 ="{\n"  +
            "  \"name\": \"man\",\n" +
            "  \"surname\": \"man\",\n" +
            "  \"email\": \"man@man.com\"\n" +
            "}";

    String ADMIN_JSON_5 ="{\n"  +
            "  \"name\": \"w\",\n" +
            "  \"surname\": \"w\",\n" +
            "  \"email\": \"w@w.com\"\n" +
            "}";
}
