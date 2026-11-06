package dasturlash.uz.service;

import dasturlash.uz.dto.request.LoginRequestDTO;
import dasturlash.uz.dto.request.RegistrationRequestDTO;
import dasturlash.uz.dto.request.SmsResendRequestDTO;
import dasturlash.uz.dto.request.SmsVerificationRequestDTO;
import dasturlash.uz.dto.response.ProfileResponseDTO;
import dasturlash.uz.entity.Profile;
import dasturlash.uz.entity.SmsHistory;
import dasturlash.uz.enums.ProfileRole;
import dasturlash.uz.enums.ProfileStatus;
import dasturlash.uz.enums.SmsStatus;
import dasturlash.uz.exception.BadRequestException;
import dasturlash.uz.exception.DuplicateResourceException;
import dasturlash.uz.exception.ResourceNotFoundException;
import dasturlash.uz.repository.ProfileRepository;
import dasturlash.uz.repository.SmsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsHistoryService smsHistoryService;
    private final SmsHistoryRepository smsHistoryRepository;

    public void register(RegistrationRequestDTO request) {
        boolean phone = profileRepository.existsByPhone(request.getPhone());
        boolean email = profileRepository.existsByEmail(request.getEmail());
        if (phone || email) {
            throw new DuplicateResourceException("Phone or Email address already exists.");
        }
        Profile profile = new Profile();
        profile.setName(request.getName());
        profile.setSurname(request.getSurname());
        profile.setPhone(request.getPhone());
        profile.setEmail(request.getEmail());
        profile.setPassword(passwordEncoder.encode(request.getPassword()));
        profile.setRole(ProfileRole.USER);
        profile.setStatus(ProfileStatus.NOT_ACTIVE);
        profile.setVisible(true);
        profile.setCreatedDate(LocalDateTime.now());
        profileRepository.save(profile);

        smsHistoryService.sendVerificationCode(request.getPhone());
    }

    public void resend(SmsResendRequestDTO request) {
        Profile profile = profileRepository.findFirstByPhoneAndVisibleTrue(request.getPhone()).orElseThrow(() -> new ResourceNotFoundException("Profile not found."));

        if (profile.getStatus() != ProfileStatus.NOT_ACTIVE) {
            throw new BadRequestException("Verification codes can only be resent to inactive profiles.");
        }

        smsHistoryService.sendVerificationCode(profile.getPhone());
    }

    @Transactional
    public void verify(SmsVerificationRequestDTO request) {
        Profile profile = profileRepository.findByPhoneAndVisibleTrue(request.getPhone()).orElseThrow(() -> new ResourceNotFoundException("Profile not found."));
        if (profile.getStatus() != ProfileStatus.NOT_ACTIVE) {
            throw new BadRequestException("Profile is already active or cannot be verified.");
        }
        SmsHistory smsHistory = smsHistoryService.getValidCode(request.getPhone(), request.getCode());
        profile.setStatus(ProfileStatus.ACTIVE);
        smsHistory.setSmsStatus(SmsStatus.USED);
        smsHistory.setUsedDate(LocalDateTime.now());

        smsHistoryRepository.save(smsHistory);
        profileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponseDTO login(LoginRequestDTO request) {
        Profile profile = profileRepository.findFirstByPhoneAndVisibleTrue(request.getPhone()).orElseThrow(() -> new BadRequestException("Phone or password is incorrect."));

        if (!passwordEncoder.matches(request.getPassword(), profile.getPassword())) {
            throw new BadRequestException("Phone or password is incorrect.");
        }

        if (profile.getStatus() != ProfileStatus.ACTIVE) {
            throw new BadRequestException("Profile is not active. Verify your phone first.");
        }

        ProfileResponseDTO response = new ProfileResponseDTO();
        response.setId(profile.getId());
        response.setName(profile.getName());
        response.setSurname(profile.getSurname());
        response.setEmail(profile.getEmail());
        response.setPhone(profile.getPhone());
        response.setStatus(profile.getStatus());
        response.setRole(profile.getRole());
        response.setVisible(profile.getVisible());
        response.setCreatedDate(profile.getCreatedDate());
        response.setPhotoId(profile.getPhotoId());

        return response;
    }


}
