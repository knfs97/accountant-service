package account.controller;

import account.entity.Payment;
import account.entity.SecurityEvent;
import account.entity.User;
import account.response.SuccessResponse;
import account.service.BusinessService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
@Validated
//@PreAuthorize("hasAnyRole('USER', 'ACCOUNTANT')")
public class BusinessRestController {
    private final BusinessService businessService;

    @GetMapping("empl/payment")
    public ResponseEntity<?> getEmployeePayment(
            @Valid
            @Pattern(regexp = "^(0[1-9]|1[0-2])-(\\d{4})$", message = "Invalid date")
            @RequestParam(required = false) String period,
            @AuthenticationPrincipal User user) {
        List<SuccessResponse> payrolls = businessService.getPayroll(user, period);
        return ResponseEntity.ok(payrolls.size() != 1 ? payrolls : payrolls.get(0));
    }

    @PostMapping("acct/payments")
    public ResponseEntity<?> uploadPayrolls(
            @NotEmpty(message = "Input payment list cannot be empty.")
            @Valid @RequestBody List<Payment> payrolls
    ) {
        return ResponseEntity.ok(businessService.uploadsPayrolls(payrolls));
    }

    @PutMapping("acct/payments")
    public ResponseEntity<SuccessResponse> changeSalary(
            @Valid @RequestBody Payment payment) {
        return ResponseEntity.ok(businessService.changeSalary(payment));
    }
    @GetMapping("security/events/")
    public ResponseEntity<List<SecurityEvent>> getSecurityEvents() {
        return ResponseEntity.ok(businessService.getSecurityEvents());
    }
}
