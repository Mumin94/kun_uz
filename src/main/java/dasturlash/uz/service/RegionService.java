package dasturlash.uz.service;

import dasturlash.uz.dto.request.RegionRequestDTO;
import dasturlash.uz.dto.response.RegionResponseDTO;
import dasturlash.uz.entity.Region;
import dasturlash.uz.exception.ResourceNotFoundException;
import dasturlash.uz.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegionService {
    private final RegionRepository regionRepository;

    public RegionResponseDTO create(RegionRequestDTO request) {
        Region entity = new Region();
        entity.setOrderNumber(request.getOrderNumber());
        entity.setNameEn(request.getNameEn());
        entity.setNameRu(request.getNameRu());
        entity.setNameUz(request.getNameUz());
        entity.setVisible(true);
        entity.setCreatedDate(LocalDateTime.now());
        regionRepository.save(entity);
        return toDTO(entity);

    }

    public RegionResponseDTO getById(Integer id) {
        Region entity = regionRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Region not found: " + id));
        return toDTO(entity);
    }

    public RegionResponseDTO update(RegionRequestDTO request, Integer id) {
        Region entity = regionRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Region not found: " + id));
        entity.setOrderNumber(request.getOrderNumber());
        entity.setNameEn(request.getNameEn());
        entity.setNameRu(request.getNameRu());
        entity.setNameUz(request.getNameUz());
        regionRepository.save(entity);
        return toDTO(entity);

    }

    public void delete(Integer id) {
        Region entity = regionRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Region not found: " + id));
        entity.setVisible(false);
        regionRepository.save(entity);
    }

    public Page<RegionResponseDTO> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending().and(Sort.by("id").descending()));
        Page<Region> result = regionRepository.findAllByVisibleTrue(pageable);
        return result.map(this::toDTO);
    }


    private RegionResponseDTO toDTO(Region entity) {
        RegionResponseDTO response = new RegionResponseDTO();
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
