package account.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class ChangePasswordRequest {
    @JsonProperty(value = "new_password")
    @NotBlank
    @Size(min = 12, message = "The password length must be at least 12 chars")
    private String newPassword;
}
