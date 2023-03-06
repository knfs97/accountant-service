package account.entity;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequest {
    @NotBlank
    @Email
    private String user;
    @NotBlank
    private String role;
    @NotBlank
    private String operation;

    public String getRole() {
        return role.toUpperCase();
    }

    public void setRole(String role) {
        this.role = role.toUpperCase();
    }

    public void setOperation(String operation) {
        this.operation = operation.toUpperCase();
    }

    public String getOperation() {
        return operation.toUpperCase();
    }
}
