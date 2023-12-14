package nl.tudelft.sem.template.example.repository;

import nl.tudelft.sem.template.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
