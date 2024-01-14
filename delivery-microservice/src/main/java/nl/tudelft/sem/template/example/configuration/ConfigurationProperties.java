package nl.tudelft.sem.template.example.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConfigurationProperties {
    private long defaultDeliveryZone = 30L;

    public long getDefaultDeliveryZone() {
        return defaultDeliveryZone;
    }

    public void setDefaultDeliveryZone(long defaultDeliveryZone) {
        this.defaultDeliveryZone = defaultDeliveryZone;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
