package account.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "security_events")
@Builder
@Setter
@Getter
public class SecurityEvent {
    @Id
    @GeneratedValue
    private Long id;
    public enum EVENT{
        CREATE_USER,
        CHANGE_PASSWORD,
        ACCESS_DENIED,
        LOGIN_FAILED,
        GRANT_ROLE,
        REMOVE_ROLE,
        LOCK_USER,
        UNLOCK_USER,
        DELETE_USER,
        BRUTE_FORCE
    }
    private LocalDate date;
    private String action;
    private String subject;
    private String object;
    private String path;
}
