package antifraud.repository;

import antifraud.model.entity.StolenCard;
import antifraud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StolenCardRepository extends JpaRepository<StolenCard, Long> {
    Optional<StolenCard> findByNumber(String number);
    void deleteByNumber(String number);
}
