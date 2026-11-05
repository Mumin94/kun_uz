package dasturlash.uz.service;

import dasturlash.uz.dto.request.ArticleTypeRequestDTO;
import dasturlash.uz.dto.response.ArticleTypeResponseDTO;
import dasturlash.uz.entity.ArticleType;
import dasturlash.uz.exception.ResourceNotFoundException;
import dasturlash.uz.repository.ArticleTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ArticleTypeService {
    private final ArticleTypeRepository articleTypeRepository;

    public ArticleTypeResponseDTO create(ArticleTypeRequestDTO request) {
        ArticleType entity = new ArticleType();
        entity.setOrderNumber(request.getOrderNumber());
        entity.setNameEn(request.getNameEn());
        entity.setNameRu(request.getNameRu());
        entity.setNameUz(request.getNameUz());
        entity.setVisible(true);
        entity.setCreatedDate(LocalDateTime.now());
        articleTypeRepository.save(entity);
        return toDTO(entity);

    }

    public ArticleTypeResponseDTO getById(Integer id) {
        ArticleType entity = articleTypeRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Article type not found: " + id));
        return toDTO(entity);
    }

    public ArticleTypeResponseDTO update(ArticleTypeRequestDTO request, Integer id) {
        ArticleType entity = articleTypeRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Article type not found: " + id));
        entity.setOrderNumber(request.getOrderNumber());
        entity.setNameEn(request.getNameEn());
        entity.setNameRu(request.getNameRu());
        entity.setNameUz(request.getNameUz());
        articleTypeRepository.save(entity);
        return toDTO(entity);

    }

    public void delete(Integer id) {
        ArticleType entity = articleTypeRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Article type not found: " + id));
        entity.setVisible(false);
        articleTypeRepository.save(entity);
    }

    public Page<ArticleTypeResponseDTO> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending().and(Sort.by("id").descending()));
        Page<ArticleType> result = articleTypeRepository.findAllByVisibleTrue(pageable);
        return result.map(this::toDTO);
    }


    private ArticleTypeResponseDTO toDTO(ArticleType entity) {
        ArticleTypeResponseDTO response = new ArticleTypeResponseDTO();
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
