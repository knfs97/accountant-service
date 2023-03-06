package account.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeAccessRequest {
    @NotBlank
    @Email
    private String user;
    @NotBlank
    private String operation;

    public String getOperation() {
        return operation.toUpperCase();
    }
}
