package dasturlash.uz.controller;

import dasturlash.uz.dto.request.ArticleTypeRequestDTO;
import dasturlash.uz.dto.response.ArticleTypeResponseDTO;
import dasturlash.uz.dto.response.LocalizedNameResponseDTO;
import dasturlash.uz.enums.Language;
import dasturlash.uz.service.ArticleTypeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/article-types")
@RequiredArgsConstructor
public class ArticleTypeController {
    private final ArticleTypeService articleTypeService;

    @PostMapping
    public ResponseEntity<ArticleTypeResponseDTO> create(@Valid @RequestBody ArticleTypeRequestDTO dto) {
        ArticleTypeResponseDTO result = articleTypeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleTypeResponseDTO> getById(@PathVariable Integer id) {
        ArticleTypeResponseDTO result = articleTypeService.getById(id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleTypeResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody ArticleTypeRequestDTO dto) {
        ArticleTypeResponseDTO result = articleTypeService.update(dto, id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        articleTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ArticleTypeResponseDTO>> getAll(@Min(1) @RequestParam(defaultValue = "1") int page, @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
        Page<ArticleTypeResponseDTO> articleTypes = articleTypeService.getAll(page - 1, size);
        return ResponseEntity.ok(articleTypes);
    }

    @GetMapping("/lang")
    public ResponseEntity<List<LocalizedNameResponseDTO>> getByLanguage(@RequestParam(defaultValue = "UZ") Language language) {
        return ResponseEntity.ok(articleTypeService.getByLanguage(language));
    }
}
