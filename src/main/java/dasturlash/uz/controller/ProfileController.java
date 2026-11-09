package dasturlash.uz.controller;

import dasturlash.uz.dto.request.*;
import dasturlash.uz.dto.response.ProfileResponseDTO;
import dasturlash.uz.service.ProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<ProfileResponseDTO> create(@Valid @RequestBody ProfileCreateRequestDTO request) {
        ProfileResponseDTO profile = profileService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponseDTO> getById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(profileService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfileResponseDTO> update(@PathVariable("id") Integer id, @Valid @RequestBody ProfileUpdateRequestDTO request) {
        return ResponseEntity.ok(profileService.update(request, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        profileService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ProfileResponseDTO>> getAll(@Min(1) @RequestParam(defaultValue = "1") int page, @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
        Page<ProfileResponseDTO> profiles = profileService.getAll(page - 1, size);
        return ResponseEntity.ok(profiles);
    }

    @PutMapping("/detail")
    public ResponseEntity<ProfileResponseDTO> updateDetail(@RequestHeader("ProfileId") Integer profileId, @Valid @RequestBody ProfileDetailUpdateRequestDTO request) {
        return ResponseEntity.ok(profileService.updateDetail(profileId, request));
    }

    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestHeader("ProfileId") Integer profileId, @Valid @RequestBody ProfilePasswordUpdateRequestDTO request) {
        profileService.updatePassword(profileId, request);
        return ResponseEntity.ok("Password changed successfully.");
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ProfileResponseDTO>> filterProfile(@ModelAttribute ProfileFilterRequestDTO filter, @Min(1) @RequestParam(defaultValue = "1") int page, @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(profileService.filter(filter, page -1 , size));
    }
}