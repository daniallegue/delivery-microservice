import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import nl.tudelft.sem.template.model.Location;

import javax.validation.Valid;

import io.swagger.v3.oas.annotations.media.Schema;


import javax.annotation.Generated;

/**
 * Order
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-12-06T17:15:08.118139700+01:00[Europe/Berlin]")
@javax.persistence.Entity
public class Blabla {

    @javax.persistence.Id
    private Long id;

    private Integer customerId;

    private Integer vendorId;

    /**
     * Gets or Sets status
     */
    public enum StatusEnum {
        PENDING("Pending"),

        ACCEPTED("Accepted"),

        REJECTED("Rejected"),

        PREPARING("Preparing"),

        GIVEN_TO_COURIER("Given_To_Courier"),

        ON_TRANSIT("On_Transit"),

        DELIVERED("Delivered");

        private String value;

        StatusEnum(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static StatusEnum fromValue(String value) {
            for (StatusEnum b : StatusEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private StatusEnum status;

    private Location destination;

    public Blabla id(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Identifier for the order of the delivery
     * @return id
     */

    @Schema(name = "id", example = "3", description = "Identifier for the order of the delivery", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Blabla customerId(Integer customerId) {
        this.customerId = customerId;
        return this;
    }

    /**
     * Get customerId
     * @return customerId
     */

    @Schema(name = "customer_id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("customer_id")
    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Blabla vendorId(Integer vendorId) {
        this.vendorId = vendorId;
        return this;
    }

    /**
     * Get vendorId
     * @return vendorId
     */

    @Schema(name = "vendor_id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("vendor_id")
    public Integer getVendorId() {
        return vendorId;
    }

    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }

    public Blabla status(StatusEnum status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     * @return status
     */

    @Schema(name = "status", example = "Preparing", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("status")
    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public Blabla destination(Location destination) {
        this.destination = destination;
        return this;
    }

    /**
     * Get destination
     * @return destination
     */
    @Valid
    @Schema(name = "destination", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("destination")
    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Blabla blabla = (Blabla) o;
        return Objects.equals(this.id, blabla.id) &&
                Objects.equals(this.customerId, blabla.customerId) &&
                Objects.equals(this.vendorId, blabla.vendorId) &&
                Objects.equals(this.status, blabla.status) &&
                Objects.equals(this.destination, blabla.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customerId, vendorId, status, destination);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Order {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    customerId: ").append(toIndentedString(customerId)).append("\n");
        sb.append("    vendorId: ").append(toIndentedString(vendorId)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    destination: ").append(toIndentedString(destination)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

