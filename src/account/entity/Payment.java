package account.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
@Valid
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Payment {
    @Id
    @GeneratedValue
    private Long id;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User user;
    @JsonProperty("employee")
    @Transient
    @NotBlank
    @Email
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@acme\\.com$")
    private String email;
    @Pattern(regexp = "^(0[1-9]|1[0-2])-(\\d{4})$", message = "Wrong date!")
    private String period;
    @Min(value = 0, message = "Salary must be non negative!")
    private Long salary;

}
