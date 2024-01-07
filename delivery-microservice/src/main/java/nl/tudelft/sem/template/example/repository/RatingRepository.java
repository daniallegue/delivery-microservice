package nl.tudelft.sem.template.example.repository;

import nl.tudelft.sem.template.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Rating findByOrderId(Long orderId);
}
