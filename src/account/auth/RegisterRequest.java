package account.auth;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String lastname;
    @NotBlank
    @Email
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@acme\\.com$", message = "Email must end with <@acme.com>")
    private String email;
    @NotBlank
    @Size(min = 12, message = "The password length must be at least 12 chars")
    private String password;
}

