package nl.tudelft.sem.template.example.controller;

import nl.tudelft.sem.template.example.authorization.AuthorizationService;
import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.MicroserviceCommunicationException;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.example.service.CourierService;
import nl.tudelft.sem.template.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

public class CourierControllerTest {

    private final DeliveryRepository deliveryRepository = Mockito.mock(DeliveryRepository.class);
    private final VendorRepository vendorRepository = Mockito.mock(VendorRepository.class);
    private final CourierService courierService = Mockito.mock(CourierService.class);
    private final AuthorizationService authorizationService = Mockito.mock(AuthorizationService.class);
    private final CourierController courierController = new CourierController(courierService, authorizationService);


    @BeforeEach
    void setup() {

        List<Delivery> deliveryList = new ArrayList<>();
        List<Vendor> vendors = new ArrayList<>();

        Location location = new Location(5.0,1.0);
        Vendor vendor = new Vendor(1L, 9L, location, new ArrayList<>());
        Order order = new Order(5L, 3L, vendor, Order.StatusEnum.ACCEPTED,  location);
        Rating rating = new Rating();
        Time time = new Time();
        Issue issue = new Issue();
        Delivery delivery = new Delivery(2L, order, null, rating, time, issue);
        deliveryList.add(delivery);
        vendors.add(vendor);

        //this delivery should be filtered out because of wrong status
        location = new Location(6.0,1.0);
        vendor = new Vendor(2L, 9L, location, new ArrayList<>());
        order = new Order(6L, 3L, vendor, Order.StatusEnum.PENDING,  location);
        delivery = new Delivery(3L, order, null, rating, time, issue);
        deliveryList.add(delivery);

        //this delivery should be filtered out because it already has a courier
        order = new Order(7L, 3L, vendor, Order.StatusEnum.ACCEPTED,  location);
        delivery = new Delivery(4L, order, 5L, rating, time, issue);
        deliveryList.add(delivery);

        //this vendor has its own couriers
        vendor = new Vendor(8L, 9L, location, List.of(18L));
        order = new Order(9L, 4L, vendor, Order.StatusEnum.ACCEPTED,  location);
        delivery = new Delivery(5L, order, null, rating, time, issue);
        deliveryList.add(delivery);
        vendors.add(vendor);

        Delivery deliveryAssigning = new Delivery(2L, order, 1L, rating, time, issue);


        Mockito.when(deliveryRepository.findById(2L)).thenReturn(Optional.of(deliveryAssigning));
        Mockito.when(deliveryRepository.findAll()).thenReturn(deliveryList);
        Mockito.when(vendorRepository.findAll()).thenReturn(vendors);

    }

    @Test
    void getAvailableOrdersTest() throws MicroserviceCommunicationException {
        Mockito.when(courierService.getAvailableOrderIds(1L)).thenReturn(List.of(5L));
        Mockito.when(courierService.getAvailableOrderIds(18L)).thenReturn(List.of(9L));
        when(authorizationService.canViewCourierAnalytics(1, 1)).thenReturn(true);
        when(authorizationService.canViewCourierAnalytics(1, 18)).thenReturn(true);


        List<Long> orderIds = courierController.courierDeliveryCourierIdAvailableOrdersGet(1L, 1).getBody();
        List<Long> expectedResult = new ArrayList<>(List.of(5L));
        assertThat(orderIds).isEqualTo(expectedResult);

        orderIds = courierController.courierDeliveryCourierIdAvailableOrdersGet(18L, 1).getBody();
        expectedResult = new ArrayList<>(List.of(9L));
        assertThat(orderIds).isEqualTo(expectedResult);
    }

    @Test
    void testAssignCourierToRandomOrderSuccess() throws DeliveryNotFoundException, NoAvailableOrdersException, OrderNotFoundException, CourierNotFoundException {
        Long courierId = 1L;
        Integer authorizationId = 1;
        doNothing().when(courierService).assignCourierToRandomOrder(courierId);

        ResponseEntity<Void> response = courierController.courierDeliveryCourierIdAssignAnyOrderPut(courierId, authorizationId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAssignCourierToRandomOrderNoAvailableOrders() throws DeliveryNotFoundException, NoAvailableOrdersException, OrderNotFoundException, CourierNotFoundException {
        Long courierId = 1L;
        Integer authorizationId = 1;
        doThrow(new NoAvailableOrdersException("No available orders")).when(courierService).assignCourierToRandomOrder(courierId);

        ResponseEntity<Void> response = courierController.courierDeliveryCourierIdAssignAnyOrderPut(courierId, authorizationId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testAssignCourierToRandomOrderDeliveryNotFound() throws DeliveryNotFoundException, NoAvailableOrdersException, OrderNotFoundException, CourierNotFoundException {
        Long courierId = 1L;
        Integer authorizationId = 1;
        doThrow(new DeliveryNotFoundException("Delivery not found")).when(courierService).assignCourierToRandomOrder(courierId);

        ResponseEntity<Void> response = courierController.courierDeliveryCourierIdAssignAnyOrderPut(courierId, authorizationId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    void assignCourierToSpecificOrderSuccessTest() throws OrderNotFoundException, CourierNotFoundException, DeliveryNotFoundException, NoAvailableOrdersException {
        Long courierId = 1L;
        Long orderId = 5L;

        doNothing().when(courierService).assignCourierToSpecificOrder(courierId, orderId);

        ResponseEntity<Void> response = courierController.courierDeliveryCourierIdAssignOrderIdPut(courierId, orderId, 1);
        Mockito.verify(courierService).assignCourierToSpecificOrder(courierId, orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void assignNonExistentCourierToOrderTest() throws OrderNotFoundException, CourierNotFoundException, DeliveryNotFoundException, NoAvailableOrdersException {
        Long nonExistentCourierId = 999L;
        Long orderId = 5L;

        doThrow(new CourierNotFoundException("Courier not found"))
                .when(courierService).assignCourierToSpecificOrder(nonExistentCourierId, orderId);

        ResponseEntity<Void> response = courierController.courierDeliveryCourierIdAssignOrderIdPut(nonExistentCourierId, orderId, 1);

        Mockito.verify(courierService).assignCourierToSpecificOrder(nonExistentCourierId, orderId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void assignCourierToNonExistentOrderTest() throws OrderNotFoundException, CourierNotFoundException, DeliveryNotFoundException, NoAvailableOrdersException {
        Long courierId = 1L;
        Long nonExistentOrderId = 999L;

        doThrow(new OrderNotFoundException("Order not found"))
                .when(courierService).assignCourierToSpecificOrder(courierId, nonExistentOrderId);

        ResponseEntity<Void> response = courierController.courierDeliveryCourierIdAssignOrderIdPut(courierId, nonExistentOrderId, 1);

        Mockito.verify(courierService).assignCourierToSpecificOrder(courierId, nonExistentOrderId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAvailableOrdersMiscommunicationTest() throws MicroserviceCommunicationException {
        when(authorizationService.canViewCourierAnalytics(10, 2)).thenThrow(MicroserviceCommunicationException.class);

        ResponseEntity<List<Long>> response = courierController.courierDeliveryCourierIdAvailableOrdersGet(2L, 10);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void getAvailableOrdersUnauthorizedTest() throws MicroserviceCommunicationException {
        when(authorizationService.getUserRole(1)).thenReturn("customer");

        ResponseEntity<List<Long>> response = courierController.courierDeliveryCourierIdAvailableOrdersGet(2L, 1);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
