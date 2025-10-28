package product_service.domain.repository;

import product_service.domain.model.Shade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShadeRepository extends JpaRepository<Shade, Long> {
}
