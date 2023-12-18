package nl.tudelft.sem.template.example;

import nl.tudelft.sem.template.example.controller.CourierController;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.example.service.CourierService;
import nl.tudelft.sem.template.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CourierControllerTest {

    private final DeliveryRepository deliveryRepository = Mockito.mock(DeliveryRepository.class);
    private final VendorRepository vendorRepository = Mockito.mock(VendorRepository.class);
    private final CourierService courierService = new CourierService(deliveryRepository, vendorRepository);
    private final CourierController courierController = new CourierController(courierService);

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
    void getAvailableOrdersTest() {

        List<Long> orderIds = courierController.courierDeliveryCourierIdAvailableOrdersGet(1L, 1).getBody();
        List<Long> expectedResult = new ArrayList<>(List.of(5L));
        assertThat(orderIds).isEqualTo(expectedResult);

        orderIds = courierController.courierDeliveryCourierIdAvailableOrdersGet(18L, 1).getBody();
        expectedResult = new ArrayList<>(List.of(9L));
        assertThat(orderIds).isEqualTo(expectedResult);
    }



}
