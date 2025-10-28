package auth_service.domain.repository;

import auth_service.domain.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    UserAccount findByUsername(String username);

    UserAccount findByEmail(String email);

    void deleteByEmail(String email);
}
