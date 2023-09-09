package antifraud.response;

import antifraud.model.Enum.UserAccess;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessResponse {
    @JsonIgnore
    private String username;
    @JsonIgnore
    private UserAccess operation;
    private String status;

    public UserAccessResponse(String username, UserAccess operation) {
        this.username = username;
        this.operation = operation;
        this.status = String.format("User %s %s!", username, operation.name().toLowerCase()+"ed");
    }
}
