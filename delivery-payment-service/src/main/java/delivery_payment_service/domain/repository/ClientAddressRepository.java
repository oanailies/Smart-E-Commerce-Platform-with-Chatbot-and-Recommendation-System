package delivery_payment_service.domain.repository;

import delivery_payment_service.domain.model.ClientAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientAddressRepository extends JpaRepository<ClientAddress, Long> {
    List<ClientAddress> findByClientId(Long clientId);
}
