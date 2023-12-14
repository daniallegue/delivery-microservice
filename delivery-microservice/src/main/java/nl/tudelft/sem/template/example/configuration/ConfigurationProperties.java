package nl.tudelft.sem.template.example.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationProperties {
    private long defaultDeliveryZone = 30L;

    public long getDefaultDeliveryZone() {
        return defaultDeliveryZone;
    }

    public void setDefaultDeliveryZone(long defaultDeliveryZone) {
        this.defaultDeliveryZone = defaultDeliveryZone;
    }

}
