package antifraud.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AntifraudDeleteResponse {
    private String status;

    public AntifraudDeleteResponse(String status) {
        this.status = status;
    }


}
