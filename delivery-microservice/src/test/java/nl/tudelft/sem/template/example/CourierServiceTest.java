package nl.tudelft.sem.template.example;

import com.sun.nio.sctp.PeerAddressChangeNotification;
import nl.tudelft.sem.template.example.exception.DeliveryNotFoundException;
import nl.tudelft.sem.template.example.repository.DeliveryRepository;
import nl.tudelft.sem.template.example.repository.VendorRepository;
import nl.tudelft.sem.template.example.service.CourierService;
import nl.tudelft.sem.template.model.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class CourierServiceTest {

    private final DeliveryRepository deliveryRepository = Mockito.mock(DeliveryRepository.class);
    private final VendorRepository vendorRepository = Mockito.mock(VendorRepository.class);

    private final Random random = new Random(0);
    private final CourierService courierService = new CourierService(deliveryRepository, vendorRepository);


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

        Mockito.when(deliveryRepository.findById(2L)).thenReturn(Optional.of(deliveryAssigning));
        Mockito.when(deliveryRepository.findAll()).thenReturn(deliveryList);
        Mockito.when(vendorRepository.findAll()).thenReturn(vendors);

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
    void checkIfCourierIsAssignedTest() {

        Long vendorId = courierService.checkIfCourierIsAssignedToVendor(8L);
        assertThat(vendorId).isEqualTo(3L);

        vendorId = courierService.checkIfCourierIsAssignedToVendor(5L);
        assertThat(vendorId).isEqualTo(-1L);

    }

    @Test
    void getVendorsWithCouriers() {

        List<Long> couriers = courierService.getVendorsThatHaveTheirOwnCouriers();
        assertThat(couriers).isEqualTo(List.of(2L, 3L));

    }


    @Test
    void assignCourierToRandomOrderTest() throws DeliveryNotFoundException {
        courierService.assignCourierToRandomOrder(1L);

        Long actual = deliveryRepository.findById(2L).get().getCourierId();
        Assertions.assertThat(actual).isEqualTo(1L);
    }

}
