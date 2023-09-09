package antifraud.repository;

import antifraud.model.entity.DetectionLimit;
import antifraud.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DetectionLimitRepository extends JpaRepository<DetectionLimit, Long> {
    Optional<DetectionLimit> findByCardNumber(String cardNumber);
}
