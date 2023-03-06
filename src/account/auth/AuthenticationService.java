package account.auth;

import account.entity.SecurityEvent;
import account.exception.EmailTakenException;
import account.entity.Role;
import account.entity.User;
import account.repository.SecurityEventRepository;
import account.repository.UserRepository;
import account.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private SecurityEventRepository securityEventRepository;

    public SuccessResponse signup(RegisterRequest request) {
        // email case insensitive
        request.setEmail(request.getEmail().toLowerCase());
        // is email taken
        if (exitsByEmail(request.getEmail())) throw new EmailTakenException();

        // password is breached
        if (BreachedPasswords.PASSWORDS.contains(request.getPassword()))
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The password is in the hacker's database!"
            );

        List<User> users = repository.findAll();

        var savedUser = repository.save(
                User.builder()
                        .name(request.getName())
                        .lastname(request.getLastname())
                        .email(request.getEmail())
                        .nonLocked(true)
                        .password(passwordEncoder.encode(request.getPassword()))
                        .roles(users.size() != 0 ? Set.of(Role.USER) : Set.of(Role.ADMINISTRATOR))
                        .build());

        securityEventRepository.save(
                SecurityEvent.builder()
                        .date(LocalDate.now())
                        .action(SecurityEvent.EVENT.CREATE_USER.toString())
                        .subject("Anonymous")
                        .object(savedUser.getEmail())
                        .path("/api/auth/signup")
                        .build()
        );

        return SuccessResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .lastname(savedUser.getLastname())
                .email(savedUser.getEmail())
                .roles(savedUser.getRolesWithPrefix())
                .build();
    }

    public SuccessResponse changePassword(ChangePasswordRequest request) {
        String newPassword = request.getNewPassword();

        // empty or null password
        if (newPassword == null || newPassword.length() < 12) throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Password length must be 12 chars minimum!"
        );

        // password is breached
        if (BreachedPasswords.PASSWORDS.contains(newPassword))
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The password is in the hacker's database!"
            );

        User authenticatedUser = getAuthenticatedUser();

        if (isSameAsCurrentPassword(newPassword, authenticatedUser.getPassword()))
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The passwords must be different!"
            );

        authenticatedUser.setPassword(passwordEncoder.encode(newPassword));
        repository.save(authenticatedUser);

        securityEventRepository.save(
                SecurityEvent.builder()
                        .date(LocalDate.now())
                        .action(SecurityEvent.EVENT.CHANGE_PASSWORD.toString())
                        .subject(authenticatedUser.getEmail())
                        .object(authenticatedUser.getEmail())
                        .path("/api/auth/changepass")
                        .build()
        );
        return SuccessResponse.builder()
                .email(authenticatedUser.getEmail())
                .status("The password has been updated successfully")
                .build();
    }

    public boolean exitsByEmail(String email) {
        Optional<User> user = repository.findByEmail(email);
        return user.isPresent();
    }

    public User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findByEmail(email).orElseThrow();
    }

    public boolean isSameAsCurrentPassword(String newPassword, String currentPassword) {
        return passwordEncoder.matches(newPassword, currentPassword);
    }
}