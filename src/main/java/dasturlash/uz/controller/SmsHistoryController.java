package dasturlash.uz.controller;

import dasturlash.uz.dto.response.SmsHistoryResponseDTO;
import dasturlash.uz.service.SmsHistoryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sms-history")
public class SmsHistoryController {

    private final SmsHistoryService smsHistoryService;

    @GetMapping
    public ResponseEntity<Page<SmsHistoryResponseDTO>> getAll(@RequestParam(defaultValue = "1") @Min(1) int page, @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(smsHistoryService.getAll(page - 1, size));
    }

    @GetMapping("/by-phone")
    public ResponseEntity<Page<SmsHistoryResponseDTO>> getByPhone(@RequestParam("phone") @NotBlank @Pattern(regexp = "^\\+998\\d{9}$") String phone, @RequestParam(defaultValue = "1") @Min(1) int page, @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(smsHistoryService.getByPhone(phone, page - 1, size));
    }

    @GetMapping("/by-date")
    public ResponseEntity<Page<SmsHistoryResponseDTO>> getByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam(defaultValue = "1") @Min(1) int page, @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(smsHistoryService.getByDate(date, page - 1, size));
    }
}