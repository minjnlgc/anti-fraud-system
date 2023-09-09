package antifraud.model.entity;

import antifraud.model.Enum.Region;
import antifraud.model.Enum.TransactionAction;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TRANSACTION_HISTORY")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("transactionId")
    private long id;

    @NotNull
    @Min(1)
    private long amount;

    @NotNull
    private String ip;

    @NotNull
    private String number;

    @NotNull
    private Region region;

    @JsonProperty("date")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTime;

    private TransactionAction result;

    private TransactionAction feedback;

    public Transaction(long amount, String ip, String number, Region region, LocalDateTime dateTime) {
        this.amount = amount;
        this.ip = ip;
        this.number = number;
        this.region = region;
        this.dateTime = dateTime;
    }

}
