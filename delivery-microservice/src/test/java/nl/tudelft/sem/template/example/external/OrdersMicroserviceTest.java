package nl.tudelft.sem.template.example.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

public class OrdersMicroserviceTest {
    private RestTemplate restTemplate;

    private OrdersMicroservice ordersMicroservice;
    private final String usersBaseUrl = "localhost:8081";

    @BeforeEach
    public void setup() {
        this.restTemplate = Mockito.mock(RestTemplate.class);
        ordersMicroservice = new OrdersMicroservice(restTemplate);
    }

    @Test
    public void testPutOrderStatusWorks() {
        Mockito.doNothing().when(restTemplate).put(Mockito.anyString(), Mockito.any());
        assertThat(ordersMicroservice.putOrderStatus(1L, 1L, "Delivered")).isTrue();
        Mockito.verify(restTemplate).put("localhost:8082/order/1/status/1", "Delivered");
    }

    @Test
    public void testPutOrderStatusDoesNotWork() {
        Mockito.doThrow(HttpClientErrorException.class).when(restTemplate)
                .put(Mockito.anyString(), Mockito.any());

        assertThat(ordersMicroservice.putOrderStatus(1L, 1L, "Delivered")).isFalse();
        Mockito.verify(restTemplate).put("localhost:8082/order/1/status/1", "Delivered");
    }

}
