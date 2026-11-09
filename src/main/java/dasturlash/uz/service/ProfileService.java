package dasturlash.uz.service;

import dasturlash.uz.dto.request.*;
import dasturlash.uz.dto.response.ProfileResponseDTO;
import dasturlash.uz.entity.Profile;
import dasturlash.uz.exception.BadRequestException;
import dasturlash.uz.exception.DuplicateResourceException;
import dasturlash.uz.exception.ResourceNotFoundException;
import dasturlash.uz.repository.ProfileRepository;
import dasturlash.uz.specification.ProfileSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    // Reject duplicate contacts and store an encoded password.
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

    // Find a visible record or report that it is missing.
    public ProfileResponseDTO getById(Integer id) {
        Profile profile = profileRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + id));
        return toDTO(profile);
    }

    // Allow unchanged contacts, but reject contacts used by another profile.
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

    // Hide the record without deleting its database row.
    public void delete(Integer id) {
        Profile profile = profileRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + id));
        profile.setVisible(false);
        profileRepository.save(profile);
    }

    // Return newest records first using zero-based service pagination.
    public Page<ProfileResponseDTO> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending().and(Sort.by("id").descending()));
        Page<Profile> result = profileRepository.findAllByVisibleTrue(pageable);
        return result.map(this::toDTO);
    }

    // Update only the profile name and surname.
    public ProfileResponseDTO updateDetail(Integer profileId, ProfileDetailUpdateRequestDTO request) {
        Profile profile = profileRepository.findByIdAndVisibleTrue(profileId).orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId));
        profile.setName(request.getName());
        profile.setSurname(request.getSurname());
        profileRepository.save(profile);
        return toDTO(profile);
    }

    // Verify the current password before encoding its replacement.
    public void updatePassword(Integer profileId, ProfilePasswordUpdateRequestDTO request) {
        Profile profile = profileRepository.findByIdAndVisibleTrue(profileId).orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId));
        if (!passwordEncoder.matches(request.getCurrentPassword(), profile.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }
        profile.setPassword(passwordEncoder.encode(request.getNewPassword()));
        profileRepository.save(profile);
    }

    // Combine optional filters while always excluding hidden profiles.
    @Transactional(readOnly = true)
    public Page<ProfileResponseDTO> filter(ProfileFilterRequestDTO filter, int page, int size) {
        if (filter.getCreatedDateFrom() != null && filter.getCreatedDateTo() != null && filter.getCreatedDateFrom().isAfter(filter.getCreatedDateTo())) {
            throw new BadRequestException("From date must not be after to date.");
        }

        Specification<Profile> specification = ProfileSpecifications.isVisible().and(ProfileSpecifications.createdFrom(filter.getCreatedDateFrom())).and(ProfileSpecifications.hasName(filter.getName())).and(ProfileSpecifications.hasSurname(filter.getSurname())).and(ProfileSpecifications.hasPhone(filter.getPhone())).and(ProfileSpecifications.hasRole(filter.getRole())).and(ProfileSpecifications.createdTo(filter.getCreatedDateTo()));

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate", "id"));
        Page<Profile> result = profileRepository.findAll(specification, pageable);
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
