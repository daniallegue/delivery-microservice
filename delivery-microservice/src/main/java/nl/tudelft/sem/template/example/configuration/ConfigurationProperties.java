package nl.tudelft.sem.template.example.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Setter
@Getter
@Configuration
public class ConfigurationProperties {
    private long defaultDeliveryZone = 30L;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
