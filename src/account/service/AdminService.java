package account.service;

import account.entity.*;
import account.repository.SecurityEventRepository;
import account.repository.UserRepository;
import account.response.SuccessResponse;
import account.validation.RoleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@RequiredArgsConstructor
@Service
public class AdminService {
    private final UserRepository userRepository;
    private final RoleValidator roleValidator;
    @Autowired
    private LoginAttemptService loginAttemptService;
    @Autowired
    private SecurityEventRepository securityEventRepository;
    public Map<String, String> deleteUserByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase());

        if (userOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");

        User user = userOpt.get();
        if (user.getRoles().contains(Role.ADMINISTRATOR))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");

        userRepository.delete(user);

        securityEventRepository.save(
                SecurityEvent.builder()
                        .date(LocalDate.now())
                        .action(SecurityEvent.EVENT.DELETE_USER.toString())
                        .subject(getAuthenticatedAdminEmail())
                        .object(user.getEmail())
                        .path("/api/admin/user")
                        .build()
        );

        return Map.of(
                "user", user.getEmail(),
                "status", "Deleted successfully!"
        );
    }
    public List<?> getAllUsers() {
        final List<User> users = userRepository.getAllUsers();
        final List<SuccessResponse> usersDetails = new ArrayList<>();
        if (users.size() == 0) return new ArrayList<>();
        users.forEach(user ->
                usersDetails.add(SuccessResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .lastname(user.getLastname())
                        .email(user.getEmail())
                        .roles(user.getRolesWithPrefix())
                        .build()));
        return usersDetails;
    }
    public SuccessResponse updateUserRole(ChangeRoleRequest request) {
        User user = getUserByEmail(request.getUser().toLowerCase());
        Role role = getRoleFromString(request.getRole());
        Operation operation = getOperationFromString(request.getOperation());
        if (operation.equals(Operation.GRANT)) grantRole(user, role);
        else if (operation.equals(Operation.REMOVE)) removeRole(user, role);
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong operation, try with GRANT or REMOVE");
        userRepository.save(user);
        return SuccessResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .roles(user.getRolesWithPrefix())
                .build();
    }
    public SuccessResponse updateUserAccess(ChangeAccessRequest request) {
        User user = getUserByEmail(request.getUser().toLowerCase());
        if (user.getRoles().contains(Role.ADMINISTRATOR))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");

        Operation operation = getOperationFromString(request.getOperation());
        if (operation.equals(Operation.LOCK)) user.setNonLocked(false);
        else if (operation.equals(Operation.UNLOCK)) {
            user.setNonLocked(true);
            loginAttemptService.evictUserFromLoginAttemptCache(user.getEmail());
        }
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong operation, try with LOCK or UNLOCK");
        userRepository.save(user);

        boolean isNonLocked = user.isNonLocked();
        String object = (isNonLocked ? "Unlock " : "Lock ") + "user " + user.getEmail();
        String authenticatedAdminEmail = getAuthenticatedAdminEmail();
        securityEventRepository.save(
                SecurityEvent.builder()
                        .date(LocalDate.now())
                        .action(isNonLocked ? SecurityEvent.EVENT.UNLOCK_USER.toString() : SecurityEvent.EVENT.LOCK_USER.toString())
                        .subject(authenticatedAdminEmail != null ? authenticatedAdminEmail : user.getEmail())
                        .object(object)
                        .path("/api/admin/user/role")
                        .build()
        );
        return SuccessResponse.builder()
                .status("User " + user.getEmail() + (isNonLocked ? " unlocked!" : " locked!"))
                .build();
    }
    private String getAuthenticatedAdminEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception ex) {
            return null;
        }
    }
    private User getUserByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        return userOpt.get();
    }
    private void grantRole(User user, Role role) {
        if (roleValidator.isValid(user, role)) {
            user.getRoles().add(role);
            securityEventRepository.save(
                    SecurityEvent.builder()
                            .date(LocalDate.now())
                            .action(SecurityEvent.EVENT.GRANT_ROLE.toString())
                            .subject(getAuthenticatedAdminEmail())
                            .object("Grant role " + role + " to " + user.getEmail())
                            .path("/api/admin/user/role")
                            .build()
            );
        } else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user cannot combine administrative and business roles!");
    }
    private void removeRole(User user, Role role) {
        if (roleValidator.isRoleAdmin(role)) // trying to remove admin role
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        if (!user.getRoles().contains(role)) // trying to remove role does not exist
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
        if (user.getRoles().size() == 1) // trying to remove last user's role
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
        user.getRoles().remove(role); // remove user
        securityEventRepository.save(
                SecurityEvent.builder()
                        .date(LocalDate.now())
                        .action(SecurityEvent.EVENT.REMOVE_ROLE.toString())
                        .subject(getAuthenticatedAdminEmail())
                        .object("Remove role " + role + " from " + user.getEmail())
                        .path("/api/admin/user/role")
                        .build()
        );
    }
    public Role getRoleFromString(String roleStr) {
        try {
            return Role.valueOf(roleStr);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
        }
    }
    public Operation getOperationFromString(String operationStr) {
        try {
            return Operation.valueOf(operationStr);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong operation");
        }
    }
}