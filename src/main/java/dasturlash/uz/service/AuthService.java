package dasturlash.uz.service;

import dasturlash.uz.dto.request.*;
import dasturlash.uz.dto.response.ProfileResponseDTO;
import dasturlash.uz.entity.EmailHistory;
import dasturlash.uz.entity.Profile;
import dasturlash.uz.entity.SmsHistory;
import dasturlash.uz.enums.ProfileRole;
import dasturlash.uz.enums.ProfileStatus;
import dasturlash.uz.enums.SmsStatus;
import dasturlash.uz.enums.VerificationChannel;
import dasturlash.uz.event.ProfileActivatedEvent;
import dasturlash.uz.exception.BadRequestException;
import dasturlash.uz.exception.DuplicateResourceException;
import dasturlash.uz.exception.ResourceNotFoundException;
import dasturlash.uz.repository.EmailHistoryRepository;
import dasturlash.uz.repository.ProfileRepository;
import dasturlash.uz.repository.SmsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final EmailHistoryService emailHistoryService;
    private final EmailHistoryRepository emailHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Create an inactive USER, then send verification through the selected channel.
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
        if (request.getVerificationChannel() == VerificationChannel.EMAIL) {
            emailHistoryService.sendVerificationLink(profile.getEmail());
        } else {
            smsHistoryService.sendVerificationCode(request.getPhone());
        }

    }

    // Send a new SMS code only while the profile is inactive.
    public void resend(SmsResendRequestDTO request) {
        Profile profile = profileRepository.findFirstByPhoneAndVisibleTrue(request.getPhone()).orElseThrow(() -> new ResourceNotFoundException("Profile not found."));

        if (profile.getStatus() != ProfileStatus.NOT_ACTIVE) {
            throw new BadRequestException("Verification codes can only be resent to inactive profiles.");
        }

        smsHistoryService.sendVerificationCode(profile.getPhone());
    }

    // Activate the locked profile and consume its SMS code in one transaction.
    @Transactional
    public void verifySms(SmsVerificationRequestDTO request) {
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

    // Check email or phone credentials and require an active profile.
    @Transactional(readOnly = true)
    public ProfileResponseDTO login(LoginRequestDTO request) {
        String login = request.getLogin().strip();
        Profile profile;
        if (login.contains("@")) {
            profile = profileRepository.findFirstByEmailAndVisibleTrue(login).orElseThrow(() -> new BadRequestException("Login or password is incorrect."));
        } else {
            profile = profileRepository.findFirstByPhoneAndVisibleTrue(login).orElseThrow(() -> new BadRequestException("Login or password is incorrect."));
        }

        if (!passwordEncoder.matches(request.getPassword(), profile.getPassword())) {
            throw new BadRequestException("Login or password is incorrect.");
        }

        if (profile.getStatus() != ProfileStatus.ACTIVE) {
            throw new BadRequestException("Profile is not active. Complete verification first.");
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

    // Activate the locked profile and consume the email token together.
    @Transactional
    public void verifyEmail(String token) {
        EmailHistory initialHistory = emailHistoryService.getValidToken(token);

        Profile profile = profileRepository.findByEmailAndVisibleTrue(initialHistory.getEmail()).orElseThrow(() -> new ResourceNotFoundException("Profile not found."));
        if (profile.getStatus() != ProfileStatus.NOT_ACTIVE) {
            throw new BadRequestException("Profile is already active or cannot be verified.");
        }

        // Check expiry again after any wait for the profile lock.
        EmailHistory emailHistory = emailHistoryService.getValidToken(token);
        profile.setStatus(ProfileStatus.ACTIVE);
        emailHistory.setUsedDate(LocalDateTime.now());
        profileRepository.save(profile);
        emailHistoryRepository.save(emailHistory);
        // The listener runs only after this transaction commits.
        eventPublisher.publishEvent(new ProfileActivatedEvent(profile.getEmail(), profile.getName() + " " + profile.getSurname()));

    }

    // Send a fresh verification link only to an inactive profile.
    public void resendEmail(EmailResendRequestDTO request) {
        Profile profile = profileRepository.findFirstByEmailAndVisibleTrue(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("Profile not found."));
        if (profile.getStatus() != ProfileStatus.NOT_ACTIVE) {
            throw new BadRequestException("Verification links can only be resent to inactive profiles.");
        }
        emailHistoryService.sendVerificationLink(profile.getEmail());
    }

}
