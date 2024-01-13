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
        Long authorizationId = 1L;
        String role = "admin";
        Long orderId = 1L;

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isTrue();
    }

    @Test
    void testIsInvolvedInOrderUserIsCustomerOfOrder() {
        Long authorizationId = 7L;
        String role = "customer";
        Long orderId = 1L;

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isTrue();
    }

    @Test
    void testIsInvolvedInOrderUserIsCustomerButNotOfOrder() {
        Long authorizationId = 10L;
        String role = "customer";
        Long orderId = 1L;

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isFalse();
    }

    @Test
    void testIsInvolvedInOrderUserIsVendorOfOrder() {
        Long authorizationId = 4L;
        String role = "vendor";
        Long orderId = 1L;

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isTrue();
    }

    @Test
    void testIsInvolvedInOrderUserIsVendorButNotOfOrder() {
        Long authorizationId = 9L;
        String role = "vendor";
        Long orderId = 1L;

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isFalse();
    }



    @Test
    void testIsInvolvedInOrderUserIsCourierButOrderHasNoCourier() {
        Long authorizationId = 5L;
        String role = "courier";
        Long orderId = 1L;

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isFalse();
    }

    @Test
    void testIsInvolvedInOrderUserIsCourierButNotOfOrder() {
        Long authorizationId = 5L;
        String role = "courier";
        Long orderId = 1L;

        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        delivery.setCourierId(8L);
        deliveryRepository.save(delivery);

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isFalse();
    }

    @Test
    void testIsInvolvedInOrderUserIsCourierOfOrder() {
        Long authorizationId = 5L;
        String role = "courier";
        Long orderId = 1L;

        Delivery delivery = deliveryRepository.findDeliveryByOrder_OrderId(orderId);
        delivery.setCourierId(5L);
        deliveryRepository.save(delivery);

        boolean result = authorizationService.isInvolvedInOrder(authorizationId, role, orderId);

        assertThat(result).isTrue();
    }

    @Test
    void testGetUserRoleSuccessfulResponse() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));

        String userRole = authorizationService.getUserRole(123L);

        assertThat(userRole).isEqualTo("customer");
    }

    @Test
    void testGetUserRoleEmptyResponse() {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> authorizationService.getUserRole(456L))
                .isInstanceOf(MicroserviceCommunicationException.class);
    }

    @Test
    void testCanViewDeliveryDetails() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("vendor"));
        boolean result = authorizationService.canViewDeliveryDetails(4L,1L);
        assertThat(result).isTrue();
    }

    @Test
    void testCanNotViewDeliveryDetails() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));
        boolean result = authorizationService.canViewDeliveryDetails(1000L,1L);
        assertThat(result).isFalse();
    }

    @Test
    void testCanViewDeliveryDetailsFaultyMicroserviceCommunication()  {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> authorizationService.canViewDeliveryDetails(456L, 1L))
                .isInstanceOf(MicroserviceCommunicationException.class);
    }

    @Test
    void testCanUpdateDeliveryDetails() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("vendor"));
        boolean result = authorizationService.canUpdateDeliveryDetails(4L,1L);
        assertThat(result).isTrue();
    }

    @Test
    void testCanNotUpdateDeliveryDetails() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));
        boolean result = authorizationService.canUpdateDeliveryDetails(7L,1L);
        assertThat(result).isFalse();
    }

    @Test
    void testCanUpdateDeliveryDetailsFaultyMicroserviceCommunication()  {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> authorizationService.canUpdateDeliveryDetails(1000L, 1L))
                .isInstanceOf(MicroserviceCommunicationException.class);
    }

    @Test
    void testCanViewCourierAnalyticsAsAdmin() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("admin"));
        boolean result = authorizationService.canViewCourierAnalytics(4L,1L);
        assertThat(result).isTrue();
    }

    @Test
    void testCanViewCourierAnalyticsAsACourier() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("courier"));
        boolean result = authorizationService.canViewCourierAnalytics(55L,55L);
        assertThat(result).isTrue();
    }

    @Test
    void testCanViewCourierAnalyticsAsADifferentCourier() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("courier"));
        boolean result = authorizationService.canViewCourierAnalytics(55L,77L);
        assertThat(result).isFalse();
    }

    @Test
    void testCanViewCourierAnalyticsAsOtherUser() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));
        boolean result = authorizationService.canViewCourierAnalytics(1L,77L);
        assertThat(result).isFalse();
    }

    @Test
    void testCanViewCourierAnalyticsFaultyMicroserviceCommunication()  {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> authorizationService.canViewCourierAnalytics(1000L, 1L))
                .isInstanceOf(MicroserviceCommunicationException.class);
    }

    @Test
    void testCanChangeOrderRating() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));
        boolean result = authorizationService.canChangeOrderRating(7L,1L);
        assertThat(result).isTrue();
    }

    @Test
    void testCanChangeOrderRatingNotCustomerOfOrder() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));
        boolean result = authorizationService.canChangeOrderRating(9L,1L);
        assertThat(result).isFalse();
    }

    @Test
    void testCanChangeOrderRatingFaultyMicroserviceCommunication()  {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> authorizationService.canChangeOrderRating(1000L, 1L))
                .isInstanceOf(MicroserviceCommunicationException.class);
    }

    @Test
    void testCanChangeVendorDeliveryZoneTrue() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("vendor"));
        boolean result = authorizationService.cannotUpdateVendorDeliveryZone(1L);
        assertThat(result).isFalse();

        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("admin"));
        boolean result2 = authorizationService.cannotUpdateVendorDeliveryZone(1L);
        assertThat(result).isFalse();
    }

    @Test
    void testCanChangeVendorDeliveryZoneFalse() throws MicroserviceCommunicationException {
        when(usersMicroservice.getUserType(anyLong())).thenReturn(Optional.of("customer"));
        boolean result = authorizationService.cannotUpdateVendorDeliveryZone(1L);
        assertThat(result).isTrue();
    }



}
