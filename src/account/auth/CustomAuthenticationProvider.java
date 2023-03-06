package account.auth;

import account.entity.ChangeAccessRequest;
import account.entity.Role;
import account.entity.SecurityEvent;
import account.entity.User;
import account.exception.UserLockedException;
import account.repository.SecurityEventRepository;
import account.repository.UserRepository;
import account.service.AdminService;
import account.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

@Component
@Data
@NoArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private AdminService adminService;
    @Autowired
    private LoginAttemptService loginAttemptService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private SecurityEventRepository securityEventRepository;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        String path = request.getServletPath();

        Optional<User> user = userRepository.findByEmail(username);
        try {

            if (user.isPresent() && !user.get().isAccountNonLocked() ||
                    loginAttemptService.isBlocked(username)) {
                throw new LockedException("User account is blocked");
            }
            Authentication authToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authenticated = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authenticated);
            loginAttemptService.loginSucceed(username);
            return authenticated;
        } catch (AuthenticationException ex) {
            if (user.isPresent() && !user.get().isAccountNonLocked()) throw ex;
            if (user.isPresent() && !user.get().getRoles().contains(Role.ADMINISTRATOR))
                loginAttemptService.loginFailed(username);

            if (loginAttemptService.getNumberOfAttempts(username) <= 5) {
                securityEventRepository.save(
                        SecurityEvent.builder()
                                .date(LocalDate.now())
                                .action(SecurityEvent.EVENT.LOGIN_FAILED.toString())
                                .subject(username)
                                .object(path)
                                .path(path)
                                .build()
                );
            }
            if (loginAttemptService.getNumberOfAttempts(username) == 5)
                blockAndRegisterUserInSecurityEvents(username, path);

            throw ex;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private void blockAndRegisterUserInSecurityEvents(String username, String path) {
        securityEventRepository.save(SecurityEvent.builder()
                .date(LocalDate.now())
                .action(SecurityEvent.EVENT.BRUTE_FORCE.toString())
                .subject(username)
                .object(path)
                .path(path)
                .build());

        try {
            adminService.updateUserAccess(ChangeAccessRequest.builder()
                    .user(username)
                    .operation("LOCK")
                    .build());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}
