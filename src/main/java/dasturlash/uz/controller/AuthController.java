package dasturlash.uz.controller;

import dasturlash.uz.dto.request.*;
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
        return ResponseEntity.status(HttpStatus.CREATED).body("Registration created. Verification instructions sent.");
    }

    @PostMapping("/verification")
    public ResponseEntity<String> verification(@Valid @RequestBody SmsVerificationRequestDTO request) {
        authService.verifySms(request);
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

    @GetMapping("/email/verification")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully.");
    }

    @PostMapping("/email/resend")
    public ResponseEntity<String> resendEmail(@Valid @RequestBody EmailResendRequestDTO request){
        authService.resendEmail(request);
        return ResponseEntity.ok("Verification email sent.");
    }
}
