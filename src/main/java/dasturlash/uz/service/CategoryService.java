package dasturlash.uz.service;

import dasturlash.uz.dto.request.CategoryRequestDTO;
import dasturlash.uz.dto.response.CategoryResponseDTO;
import dasturlash.uz.entity.Category;
import dasturlash.uz.exception.ResourceNotFoundException;
import dasturlash.uz.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryResponseDTO create(CategoryRequestDTO request) {
        Category entity = new Category();
        entity.setOrderNumber(request.getOrderNumber());
        entity.setNameEn(request.getNameEn());
        entity.setNameRu(request.getNameRu());
        entity.setNameUz(request.getNameUz());
        entity.setVisible(true);
        entity.setCreatedDate(LocalDateTime.now());
        categoryRepository.save(entity);
        return toDTO(entity);

    }

    public CategoryResponseDTO getById(Integer id) {
        Category entity = categoryRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        return toDTO(entity);
    }

    public CategoryResponseDTO update(CategoryRequestDTO request, Integer id) {
        Category entity = categoryRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        entity.setOrderNumber(request.getOrderNumber());
        entity.setNameEn(request.getNameEn());
        entity.setNameRu(request.getNameRu());
        entity.setNameUz(request.getNameUz());
        categoryRepository.save(entity);
        return toDTO(entity);

    }

    public void delete(Integer id) {
        Category entity = categoryRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        entity.setVisible(false);
        categoryRepository.save(entity);
    }

    public Page<CategoryResponseDTO> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending().and(Sort.by("id").descending()));
        Page<Category> result = categoryRepository.findAllByVisibleTrue(pageable);
        return result.map(this::toDTO);
    }


    private CategoryResponseDTO toDTO(Category entity) {
        CategoryResponseDTO response = new CategoryResponseDTO();
        response.setId(entity.getId());
        response.setOrderNumber(entity.getOrderNumber());
        response.setNameEn(entity.getNameEn());
        response.setNameRu(entity.getNameRu());
        response.setNameUz(entity.getNameUz());
        response.setCreatedDate(entity.getCreatedDate());
        response.setVisible(entity.getVisible());
        return response;
    }


}
