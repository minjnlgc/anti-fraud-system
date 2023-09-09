package antifraud.request;

import antifraud.model.Enum.TransactionAction;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFeedbackRequest {

    @NotNull
    private long transactionId;

    @NotNull
    private TransactionAction feedback; // it said string in the question, to see if this work
}
