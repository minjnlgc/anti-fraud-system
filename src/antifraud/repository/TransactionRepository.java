package antifraud.repository;

import antifraud.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByNumberAndDateTimeBetween(String cardNumber, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo);
    List<Transaction> findAllByNumber(String cardNumber);
    Optional<Transaction> findById(long id);
}
