package antifraud.request;

import antifraud.model.Enum.UserAccess;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetAccessRequest {
    @NotNull
    private String username;
    @NotNull
    private UserAccess operation;
}
