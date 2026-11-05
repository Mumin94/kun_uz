package dasturlash.uz.service;

import dasturlash.uz.dto.request.ProfileCreateRequestDTO;
import dasturlash.uz.dto.request.ProfileUpdateRequestDTO;
import dasturlash.uz.dto.response.ProfileResponseDTO;
import dasturlash.uz.entity.Profile;
import dasturlash.uz.exception.DuplicateResourceException;
import dasturlash.uz.exception.ResourceNotFoundException;
import dasturlash.uz.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponseDTO create(ProfileCreateRequestDTO request) {
        if (profileRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("There is already a profile with this email address.");
        }

        if (profileRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("There is already a profile with this phone number.");
        }
        Profile profile = new Profile();
        profile.setName(request.getName());
        profile.setSurname(request.getSurname());
        profile.setEmail(request.getEmail());
        profile.setPhone(request.getPhone());
        profile.setPassword(passwordEncoder.encode(request.getPassword()));
        profile.setStatus(request.getStatus());
        profile.setRole(request.getRole());
        profile.setVisible(true);
        profile.setCreatedDate(LocalDateTime.now());
        profileRepository.save(profile);
        return toDTO(profile);

    }

    public ProfileResponseDTO getById(Integer id) {
        Profile profile = profileRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + id));
        return toDTO(profile);
    }

    public ProfileResponseDTO update(ProfileUpdateRequestDTO request, Integer id) {
        Profile profile = profileRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + id));
        if (profileRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("This email address is already used by another profile.");
        }
        if (profileRepository.existsByPhoneAndIdNot(request.getPhone(), id)) {
            throw new DuplicateResourceException("This phone number is already used by another profile.");
        }
        profile.setName(request.getName());
        profile.setSurname(request.getSurname());
        profile.setEmail(request.getEmail());
        profile.setPhone(request.getPhone());
        profile.setStatus(request.getStatus());
        profile.setRole(request.getRole());
        profileRepository.save(profile);
        return toDTO(profile);
    }

    public void delete(Integer id) {
        Profile profile = profileRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + id));
        profile.setVisible(false);
        profileRepository.save(profile);
    }

    public Page<ProfileResponseDTO> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending().and(Sort.by("id").descending()));
        Page<Profile> result = profileRepository.findAllByVisibleTrue(pageable);
        return result.map(this::toDTO);
    }


    private ProfileResponseDTO toDTO(Profile entity) {
        ProfileResponseDTO response = new ProfileResponseDTO();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setSurname(entity.getSurname());
        response.setEmail(entity.getEmail());
        response.setPhone(entity.getPhone());
        response.setStatus(entity.getStatus());
        response.setRole(entity.getRole());
        response.setVisible(entity.getVisible());
        response.setCreatedDate(entity.getCreatedDate());
        response.setPhotoId(entity.getPhotoId());
        return response;
    }

}
