package antifraud.model.dto;

import antifraud.model.Enum.Region;
import antifraud.validator.CardNumber;
import antifraud.validator.Ipv4;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

    private long transactionId;

    @NotNull
    @Min(1)
    private long amount;

    @NotNull
    @Ipv4
    private String ip;

    @NotNull
    @CardNumber
    private String number;

    @NotNull
    private Region region;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    private String result;

    private String feedback;

    public TransactionDto(long amount, String ip, String number, Region region, LocalDateTime date, String result, String feedback) {
        this.amount = amount;
        this.ip = ip;
        this.number = number;
        this.region = region;
        this.date = date;
        this.result = result;
        this.feedback = feedback;
    }
}
