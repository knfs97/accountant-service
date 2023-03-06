package account.controller;

import account.entity.ChangeAccessRequest;
import account.entity.ChangeRoleRequest;
import account.response.SuccessResponse;
import account.service.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequestMapping("/api/admin/user/")
@RequiredArgsConstructor
@RestController
public class AdminRestController {
    private final AdminService adminService;

    @DeleteMapping("{email}")
    public ResponseEntity<?> deleteUser(
            @Valid
            @NotBlank
            @PathVariable String email
    ) {
        return ResponseEntity.ok(adminService.deleteUserByEmail(email));
    }

    @PutMapping("role")
    public ResponseEntity<SuccessResponse> updateUserRole(
            @RequestBody
            @Valid
            ChangeRoleRequest request
            ) {
        return ResponseEntity.ok(adminService.updateUserRole(request));
    }
    @GetMapping
    public ResponseEntity<List<?>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("access")
    public ResponseEntity<SuccessResponse> updateAccess(
            @RequestBody
            @Valid
            ChangeAccessRequest request
    ){
        return ResponseEntity.ok(adminService.updateUserAccess(request));
    }
}
