package account.auth;

import account.entity.User;
import account.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authService;

    @PostMapping("signup")
    public ResponseEntity<SuccessResponse> signup(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("changepass")
    public ResponseEntity<SuccessResponse> changePassword(@AuthenticationPrincipal User user,
                                                          @RequestBody ChangePasswordRequest request) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(authService.changePassword(request));
    }
}
