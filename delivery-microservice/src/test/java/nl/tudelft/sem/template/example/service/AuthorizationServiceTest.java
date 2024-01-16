package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.Application;
import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.external.UsersMicroservice;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.model.Delivery;
import nl.tudelft.sem.template.model.Location;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Vendor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = Application.class)
public class AuthorizationServiceTest {
    private final UsersMicroservice usersMicroservice;

    private final DeliveryRepository deliveryRepository;

    private final AuthorizationService authorizationService;



    @Autowired
    public AuthorizationServiceTest(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
        this.usersMicroservice = Mockito.mock(UsersMicroservice.class);
        this.authorizationService = new AuthorizationService(usersMicroservice, deliveryRepository);
    }

    Delivery firstDelivery;

    @BeforeEach
    void setup(){
        Vendor normalVendor = new Vendor(4L, 30L, new Location(3.0, 4.0), new ArrayList<>());
        Order firstOrder = new Order(1L, 7L, normalVendor, Order.StatusEnum.PENDING, new Location(5.0, 6.0));
        firstDelivery = new Delivery();
        firstDelivery.setOrder(firstOrder);
        deliveryRepository.save(firstDelivery);
    }

    @Test
    void testIsInvolvedInOrderUserIsAdmin() {
        int authorizationId = 1;
        String role = "admin";
        int orderId = 1;

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isTrue();
    }

    @Test
    void testIsInvolvedInOrderUserIsCustomerOfOrder() {
        int authorizationId = 7;
        String role = "customer";
        int orderId = 1;

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isTrue();
    }

    @Test
    void testIsInvolvedInOrderUserIsCustomerButNotOfOrder() {
        int authorizationId = 10;
        String role = "customer";
        int orderId = 1;

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isFalse();
    }

    @Test
    void testIsInvolvedInOrderUserIsVendorOfOrder() {
        int authorizationId = 4;
        String role = "vendor";
        int orderId = 1;

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isTrue();
    }

    @Test
    void testIsInvolvedInOrderUserIsVendorButNotOfOrder() {
        int authorizationId = 9;
        String role = "vendor";
        int orderId = 1;

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isFalse();
    }



    @Test
    void testIsInvolvedInOrderUserIsCourierButOrderHasNoCourier() {
        int authorizationId = 5;
        String role = "courier";
        int orderId = 1;

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isFalse();
    }

    @Test
    void testIsInvolvedInOrderUserIsCourierButNotOfOrder() {
        int authorizationId = 5;
        String role = "courier";
        int orderId = 1;

        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId((long) orderId);
        delivery.setCourierId(8L);
        deliveryRepository.save(delivery);

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isFalse();
    }

    @Test
    void testIsInvolvedInOrderUserIsCourierOfOrder() {
        int authorizationId = 5;
        String role = "courier";
        int orderId = 1;

        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId((long) orderId);
        delivery.setCourierId(5L);
        deliveryRepository.save(delivery);

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isTrue();
    }

    @Test
    void testGetUserRoleSuccessfulResponse() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));

        String userRole = authorizationService.getUserRole(123);

        assertThat(userRole).isEqualTo("customer");
    }

    @Test
    void testGetUserRoleEmptyResponse() {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> authorizationService.getUserRole(456))
                .isInstanceOf(MicroserviceCommunicationException.class);
    }

    @Test
    void testCanViewDeliveryDetails() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("vendor"));
        boolean result = authorizationService.canViewDeliveryDetails(4,1);
        assertThat(result).isTrue();
    }

    @Test
    void testCanNotViewDeliveryDetails() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));
        boolean result = authorizationService.canViewDeliveryDetails(1000,1);
        assertThat(result).isFalse();
    }

    @Test
    void testCanViewDeliveryDetailsFaultyMicroserviceCommunication()  {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> authorizationService.canViewDeliveryDetails(456, 1))
                .isInstanceOf(MicroserviceCommunicationException.class);
    }

    @Test
    void testCanUpdateDeliveryDetails() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("vendor"));
        boolean result = authorizationService.canUpdateDeliveryDetails(4,1);
        assertThat(result).isTrue();
    }

    @Test
    void testCanNotUpdateDeliveryDetails() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));
        boolean result = authorizationService.canUpdateDeliveryDetails(7,1);
        assertThat(result).isFalse();
    }

    @Test
    void testCanUpdateDeliveryDetailsFaultyMicroserviceCommunication()  {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> authorizationService.canUpdateDeliveryDetails(1000, 1))
                .isInstanceOf(MicroserviceCommunicationException.class);
    }

    @Test
    void testCanViewCourierAnalyticsAsAdmin() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("admin"));
        boolean result = authorizationService.canViewCourierAnalytics(4,1);
        assertThat(result).isTrue();
    }

    @Test
    void testCanViewCourierAnalyticsAsACourier() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("courier"));
        boolean result = authorizationService.canViewCourierAnalytics(55,55);
        assertThat(result).isTrue();
    }

    @Test
    void testCanViewCourierAnalyticsAsADifferentCourier() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("courier"));
        boolean result = authorizationService.canViewCourierAnalytics(55,77);
        assertThat(result).isFalse();
    }

    @Test
    void testCanViewCourierAnalyticsAsOtherUser() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));
        boolean result = authorizationService.canViewCourierAnalytics(1,77);
        assertThat(result).isFalse();
    }

    @Test
    void testCanViewCourierAnalyticsFaultyMicroserviceCommunication()  {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> authorizationService.canViewCourierAnalytics(1000, 1))
                .isInstanceOf(MicroserviceCommunicationException.class);
    }

    @Test
    void testCanChangeOrderRating() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));
        boolean result = authorizationService.canChangeOrderRating(7,1);
        assertThat(result).isTrue();
    }

    @Test
    void testCanChangeOrderRatingNotCustomerOfOrder() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));
        boolean result = authorizationService.canChangeOrderRating(9,1);
        assertThat(result).isFalse();
    }

    @Test
    void testCanChangeOrderRatingFaultyMicroserviceCommunication()  {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> authorizationService.canChangeOrderRating(1000, 1))
                .isInstanceOf(MicroserviceCommunicationException.class);
    }

    @Test
    void testCanChangeVendorDeliveryZoneTrue() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("vendor"));
        boolean result = authorizationService.cannotUpdateVendorDeliveryZone(1);
        assertThat(result).isFalse();

        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("admin"));
        boolean result2 = authorizationService.cannotUpdateVendorDeliveryZone(1);
        assertThat(result).isFalse();
    }

    @Test
    void testCanChangeVendorDeliveryZoneFalse() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));
        boolean result = authorizationService.cannotUpdateVendorDeliveryZone(1);
        assertThat(result).isTrue();
    }

    @Test
    void testIsInvolvedInOrderFalse() {
        Integer authorizationId = 7;
        String role = "random";
        Integer orderId = 1;

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isFalse();
    }
}
