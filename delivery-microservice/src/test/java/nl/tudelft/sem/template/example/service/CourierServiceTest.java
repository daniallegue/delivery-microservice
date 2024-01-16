package nl.tudelft.sem.template.example.service;

import nl.tudelft.sem.template.example.exception.CourierNotFoundException;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.exception.NoAvailableOrdersException;
import nl.tudelft.sem.template.example.exception.OrderNotFoundException;
import nl.tudelft.sem.template.example.external.UsersMicroservice;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.OrderRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.model.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CourierServiceTest {

    private final DeliveryRepository deliveryRepository = Mockito.mock(DeliveryRepository.class);
    private final VendorRepository vendorRepository = Mockito.mock(VendorRepository.class);

    private final UsersMicroservice usersMicroservice = Mockito.mock(UsersMicroservice.class);

    private final CourierService courierService = Mockito.spy(new CourierService(deliveryRepository, vendorRepository, usersMicroservice));

    private final OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
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

        location = new Location(6.0,1.0);
        vendor = new Vendor(2L, 9L, location, List.of(16L));
        order = new Order(6L, 3L, vendor, Order.StatusEnum.PENDING,  location);
        delivery = new Delivery(2L, order, null, rating, time, issue);
        deliveryList.add(delivery);
        vendors.add(vendor);

        order = new Order(7L, 3L, vendor, Order.StatusEnum.ACCEPTED,  location);
        delivery = new Delivery(2L, order, 5L, rating, time, issue);
        deliveryList.add(delivery);

        vendor = new Vendor(3L, 9L, location, List.of(8L));
        order = new Order(9L, 4L, vendor, Order.StatusEnum.ACCEPTED,  location);
        delivery = new Delivery(2L, order, null, rating, time, issue);
        deliveryList.add(delivery);
        vendors.add(vendor);

        Delivery deliveryAssigning = new Delivery(2L, order, 1L, rating, time, issue);


        when(deliveryRepository.findById(2L)).thenReturn(Optional.of(deliveryAssigning));
        when(deliveryRepository.findAll()).thenReturn(deliveryList);
        when(deliveryRepository.findDeliveryByOrder_OrderId(5L)).thenReturn(deliveryAssigning);
        when(vendorRepository.findAll()).thenReturn(vendors);

        when(deliveryRepository.findDeliveryByOrder_OrderId(9L)).thenReturn(delivery);
        when(deliveryRepository.findById(2L)).thenReturn(Optional.of(delivery));
    }

    @Test
    void getAvailableOrdersTest() {

        List<Long> orderIds = courierService.getAvailableOrderIds(1L);
        List<Long> expectedResult = new ArrayList<>(List.of(5L));
        Assertions.assertThat(orderIds).isEqualTo(expectedResult);

        orderIds = courierService.getAvailableOrderIds(8L);
        expectedResult = new ArrayList<>(List.of(9L));
        Assertions.assertThat(orderIds).isEqualTo(expectedResult);

    }

    @Test
    void getAvailableOrdersWithNonNullOrNonAcceptedStatusTest() {
        Delivery deliveryWithCourier = new Delivery(3L, new Order(8L, 3L, new Vendor(1L, 9L, new Location(5.0, 1.0), new ArrayList<>()), Order.StatusEnum.ACCEPTED, new Location(5.0, 1.0)), 1L, new Rating(), new Time(), new Issue());
        Delivery deliveryNotAccepted = new Delivery(4L, new Order(9L, 3L, new Vendor(1L, 9L, new Location(5.0, 1.0), new ArrayList<>()), Order.StatusEnum.PENDING, new Location(5.0, 1.0)), null, new Rating(), new Time(), new Issue());
        List<Delivery> additionalDeliveries = Arrays.asList(deliveryWithCourier, deliveryNotAccepted);
        when(deliveryRepository.findAll()).thenReturn(additionalDeliveries);
        List<Long> orderIds = courierService.getAvailableOrderIds(1L);
        Assertions.assertThat(orderIds).doesNotContain(8L, 9L);
    }


    @Test
    void checkIfCourierIsAssignedTest() throws CourierNotFoundException {
        Long vendorId = courierService.checkIfCourierIsAssignedToVendor(8L);
        assertThat(vendorId).isEqualTo(3L);
    }

    @Test
    void getVendorsWithCouriers() {

        List<Long> couriers = courierService.getVendorsThatHaveTheirOwnCouriers();
        assertThat(couriers).isEqualTo(List.of(2L, 3L));

    }


    @Test
    void assignCourierToRandomOrderTest() throws DeliveryNotFoundException, NoAvailableOrdersException, OrderNotFoundException, CourierNotFoundException {
        when(usersMicroservice.getUserType(1L)).thenReturn(Optional.of("courier"));
        courierService.assignCourierToRandomOrder(1L);

        Long actual = deliveryRepository.findById(2L).get().getCourierId();
        Assertions.assertThat(actual).isEqualTo(1L);
    }

    @Test
    void assignCourierToRandomOrderCourierNotFoundTest() throws CourierNotFoundException {
        when(usersMicroservice.getUserType(999L)).thenReturn(Optional.of("vendor"));
        Long nonExistentCourierId = 999L;
        Throwable exception = assertThrows(CourierNotFoundException.class, () -> {
            courierService.assignCourierToRandomOrder(nonExistentCourierId);
        });

        assertThat(exception.getMessage()).isEqualTo("Courier with id " + nonExistentCourierId + " not found.");

    }

    @Test
    void assignCourierToSpecificOrderTest() throws DeliveryNotFoundException, OrderNotFoundException, CourierNotFoundException, NoAvailableOrdersException {
        when(usersMicroservice.getUserType(1L)).thenReturn(Optional.of("courier"));
        when(usersMicroservice.getUserType(5L)).thenReturn(Optional.of("courier"));
        courierService.assignCourierToSpecificOrder(5L, 9L);

        Long actualCourier = deliveryRepository.findById(2L).get().getCourierId();
        Assertions.assertThat(actualCourier).isEqualTo(5L);
    }

    @Test
    void assigningNonExistentCourierToSpecificOrderTest() {
        Long nonExistentCourierId = 999L;
        Long existingOrderId = 9L;
        when(usersMicroservice.getUserType(999L)).thenReturn(Optional.of("vendor"));
        Throwable exception = assertThrows(CourierNotFoundException.class, () -> {
            courierService.assignCourierToSpecificOrder(nonExistentCourierId, existingOrderId);
        });

        assertThat(exception.getMessage()).isEqualTo("Courier with id " + nonExistentCourierId + " not found.");
    }

    @Test
    void assigningNonExistentOrderToCourierTest() {
        Long existingCourierId = 1L;
        Long nonExistentOrderId = 999L;
        when(usersMicroservice.getUserType(1L)).thenReturn(Optional.of("courier"));

        Throwable exception = assertThrows(DeliveryNotFoundException.class, () -> {
            courierService.assignCourierToSpecificOrder(existingCourierId, nonExistentOrderId);
        });

        assertThat(exception.getMessage()).isEqualTo("Delivery with order id " + nonExistentOrderId + " was not found.");
    }

}
