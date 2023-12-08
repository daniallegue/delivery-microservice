package nl.tudelft.sem.template.example.repository;

import nl.tudelft.sem.template.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    Order findOrderByOrderId(Long id);
}
