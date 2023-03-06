package account.validation;

import account.entity.Role;
import account.entity.User;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component("roleValidator")
public class RoleValidator {
    public boolean isValid(User user, Role role) {
        return belongsToAdministration(user) && isRoleAdmin(role) || belongsToBusiness(user) && !isRoleAdmin(role);
    }
    private boolean belongsToAdministration(User user) {
        return user.getRoles().contains(Role.ADMINISTRATOR);
    }
    private boolean belongsToBusiness(User user) {
        return !user.getRoles().contains(Role.ADMINISTRATOR);
    }
    public boolean isRoleAdmin(Role role) {
        return role.equals(Role.ADMINISTRATOR);
    }
}
