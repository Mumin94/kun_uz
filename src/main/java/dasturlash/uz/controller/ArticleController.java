package dasturlash.uz.controller;

import dasturlash.uz.dto.request.ArticleCreateRequestDTO;
import dasturlash.uz.dto.request.ArticleExcludeRequestDTO;
import dasturlash.uz.dto.request.ArticleStatusUpdateRequestDTO;
import dasturlash.uz.dto.request.ArticleUpdateRequestDTO;
import dasturlash.uz.dto.response.ArticleFullResponseDTO;
import dasturlash.uz.dto.response.ArticleResponseDTO;
import dasturlash.uz.dto.response.ArticleShortResponseDTO;
import dasturlash.uz.enums.Language;
import dasturlash.uz.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    public ResponseEntity<ArticleResponseDTO> create(@RequestHeader("ProfileId") Integer moderatorId, @Valid @RequestBody ArticleCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(articleService.create(request, moderatorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponseDTO> update(@PathVariable("id") String id, @RequestHeader("ProfileId") Integer moderatorId, @Valid @RequestBody ArticleUpdateRequestDTO request) {
        return ResponseEntity.ok(articleService.update(id, request, moderatorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id, @RequestHeader("ProfileId") Integer moderatorId) {
        articleService.delete(id, moderatorId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ArticleResponseDTO> changeStatus(@PathVariable("id") String id, @RequestHeader("ProfileId") Integer publisherId, @Valid @RequestBody ArticleStatusUpdateRequestDTO request) {
        return ResponseEntity.ok(articleService.changeStatus(id, request, publisherId));
    }

    @GetMapping("/types/{typeId}/latest-5")
    public ResponseEntity<List<ArticleShortResponseDTO>> getLatest5ByType(@PathVariable("typeId") Integer typeId, @RequestParam(defaultValue = "UZ") Language language) {
        return ResponseEntity.ok(articleService.getLatest5ByType(typeId, language));
    }

    @GetMapping("/types/{typeId}/latest-3")
    public ResponseEntity<List<ArticleShortResponseDTO>> getLatest3ByType(@PathVariable("typeId") Integer typeId, @RequestParam(defaultValue = "UZ") Language language) {
        return ResponseEntity.ok(articleService.getLatest3ByType(typeId, language));
    }

    @PostMapping("/latest-8/excluding")
    public ResponseEntity<List<ArticleShortResponseDTO>> getLatest8Excluding(@Valid @RequestBody ArticleExcludeRequestDTO request, @RequestParam(defaultValue = "UZ") Language language) {
        return ResponseEntity.ok(articleService.getLatest8Excluding(request, language));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleFullResponseDTO> getByIdAndLanguage(@PathVariable("id") String id, @RequestParam(defaultValue = "UZ") Language language) {
        return ResponseEntity.ok(articleService.getByIdAndLanguage(id, language));
    }

    @GetMapping("/types/{typeId}/latest-4")
    public ResponseEntity<List<ArticleShortResponseDTO>> getLatest4ByTypeExcluding(@PathVariable("typeId") Integer typeId, @RequestParam("excludedId") String excludedId, @RequestParam(defaultValue = "UZ") Language language) {
        return ResponseEntity.ok(articleService.getLatest4ByTypeExcluding(typeId, excludedId, language));
    }

    @GetMapping("/most-read")
    public ResponseEntity<List<ArticleShortResponseDTO>> getMostRead4(@RequestParam(defaultValue = "UZ") Language language) {
        return ResponseEntity.ok(articleService.getMostRead4(language));
    }
}