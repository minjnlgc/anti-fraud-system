package antifraud.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDeleteResponse {
    private String username;
    private String status;

    public UserDeleteResponse(String username) {
        this.username = username;
        this.status = "Deleted successfully!";
    }
}
