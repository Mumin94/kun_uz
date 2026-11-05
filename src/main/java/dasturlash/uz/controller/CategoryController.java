package dasturlash.uz.controller;

import dasturlash.uz.dto.request.CategoryRequestDTO;
import dasturlash.uz.dto.response.CategoryResponseDTO;
import dasturlash.uz.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> create(@Valid @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO result = categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getById(@PathVariable Integer id) {
        CategoryResponseDTO result = categoryService.getById(id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO result = categoryService.update(dto, id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponseDTO>> getAll(@Min(1) @RequestParam(defaultValue = "1") int page, @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
        Page<CategoryResponseDTO> Categorys = categoryService.getAll(page - 1, size);
        return ResponseEntity.ok(Categorys);
    }
}
