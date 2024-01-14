package nl.tudelft.sem.template.example.external;

import nl.tudelft.sem.template.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UsersMicroserviceTest {


    private RestTemplate restTemplate;

    private UsersMicroservice usersMicroservice;
    private final String usersBaseUrl = "localhost:8081";

    @BeforeEach
    public void setup() {
        this.restTemplate = Mockito.mock(RestTemplate.class);
        usersMicroservice = new UsersMicroservice(restTemplate);
    }
    @Test
    void testGetUserType() {
        when(restTemplate.getForObject(usersBaseUrl + "/user/" + 1, String.class))
                .thenReturn("admin");
        assertThat(usersMicroservice.getUserType(1L)).isEqualTo(Optional.of("admin"));
    }

    @Test
    void testGetUserTypeFail() {
        when(restTemplate.getForObject(usersBaseUrl + "/user/" + 1, String.class))
                .thenThrow(HttpClientErrorException.class);
        assertThat(usersMicroservice.getUserType(1L)).isEqualTo(Optional.empty());
    }

    @Test
    void testGetVendorLocation() {
        when(restTemplate.getForObject(usersBaseUrl + "/vendor/" + 1 + "/location", Location.class))
                .thenReturn(new Location(1.0, 1.0));
        assertThat(usersMicroservice.getVendorLocation(1L)).isEqualTo(Optional.of(new Location( 1.0, 1.0)));
    }

    @Test
    void testGetVendorLocationFail() {
        when(restTemplate.getForObject(usersBaseUrl + "/vendor/" + 1 + "/location", Location.class))
                .thenThrow(HttpClientErrorException.class);
        assertThat(usersMicroservice.getVendorLocation(1L)).isEqualTo(Optional.empty());
    }
}
