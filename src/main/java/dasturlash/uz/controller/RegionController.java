package dasturlash.uz.controller;

import dasturlash.uz.dto.request.RegionRequestDTO;
import dasturlash.uz.dto.response.RegionResponseDTO;
import dasturlash.uz.service.RegionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {
    private final RegionService regionService;

    @PostMapping
    public ResponseEntity<RegionResponseDTO> create(@Valid @RequestBody RegionRequestDTO dto) {
        RegionResponseDTO result = regionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegionResponseDTO> getById(@PathVariable Integer id) {
        RegionResponseDTO result = regionService.getById(id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegionResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody RegionRequestDTO dto) {
        RegionResponseDTO result = regionService.update(dto, id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        regionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<RegionResponseDTO>> getAll(@Min(1) @RequestParam(defaultValue = "1") int page, @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
        Page<RegionResponseDTO> Regions = regionService.getAll(page - 1, size);
        return ResponseEntity.ok(Regions);
    }
}
