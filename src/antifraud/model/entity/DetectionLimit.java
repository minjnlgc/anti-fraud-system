package antifraud.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "detection_limit_for_card")
public class DetectionLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String cardNumber;
    private long allowedLimit;
    private long manualLimit;

    public DetectionLimit(String cardNumber, long allowedLimit, long manualLimit) {
        this.cardNumber = cardNumber;
        this.allowedLimit = allowedLimit;
        this.manualLimit = manualLimit;
    }
}
