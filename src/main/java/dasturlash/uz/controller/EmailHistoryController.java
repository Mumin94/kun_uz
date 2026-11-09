package dasturlash.uz.controller;

import dasturlash.uz.dto.response.EmailHistoryResponseDTO;
import dasturlash.uz.service.EmailHistoryService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email-history")
public class EmailHistoryController {

    private final EmailHistoryService emailHistoryService;

    @GetMapping
    public ResponseEntity<Page<EmailHistoryResponseDTO>> getAll(@RequestParam(defaultValue = "1") @Min(1) int page, @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(emailHistoryService.getAll(page - 1, size));
    }

    @GetMapping("/by-email")
    public ResponseEntity<Page<EmailHistoryResponseDTO>> getByEmail(@RequestParam("email") @NotBlank @Email String email, @RequestParam(defaultValue = "1") @Min(1) int page, @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(emailHistoryService.getByEmail(email, page - 1, size));
    }

    @GetMapping("/by-date")
    public ResponseEntity<Page<EmailHistoryResponseDTO>> getByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam(defaultValue = "1") @Min(1) int page, @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(emailHistoryService.getByDate(date, page - 1, size));
    }
}