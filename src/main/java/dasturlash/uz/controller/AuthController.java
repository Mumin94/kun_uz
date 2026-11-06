package dasturlash.uz.controller;

import dasturlash.uz.dto.request.LoginRequestDTO;
import dasturlash.uz.dto.request.RegistrationRequestDTO;
import dasturlash.uz.dto.request.SmsResendRequestDTO;
import dasturlash.uz.dto.request.SmsVerificationRequestDTO;
import dasturlash.uz.dto.response.ProfileResponseDTO;
import dasturlash.uz.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;


    @PostMapping("/registration")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegistrationRequestDTO request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Registration created. Verification code accepted by SMS service.");
    }

    @PostMapping("/verification")
    public ResponseEntity<String> verification(@Valid @RequestBody SmsVerificationRequestDTO request) {
        authService.verify(request);
        return ResponseEntity.ok("Phone verified successfully.");
    }

    @PostMapping("/resend")
    public ResponseEntity<String> resend(@Valid @RequestBody SmsResendRequestDTO request) {
        authService.resend(request);
        return ResponseEntity.ok("Verification code accepted by SMS service.");
    }

    @PostMapping("/login")
    public ResponseEntity<ProfileResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
