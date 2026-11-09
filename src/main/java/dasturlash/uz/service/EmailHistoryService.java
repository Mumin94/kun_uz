package dasturlash.uz.service;

import dasturlash.uz.dto.response.EmailHistoryResponseDTO;
import dasturlash.uz.entity.EmailHistory;
import dasturlash.uz.entity.Profile;
import dasturlash.uz.enums.EmailType;
import dasturlash.uz.exception.BadRequestException;
import dasturlash.uz.repository.EmailHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailHistoryService {
    private final EmailHistoryRepository emailHistoryRepository;
    private final EmailService emailService;
    @Value("${app.verification.base-url}")
    private String verificationBaseUrl;
    @Value("${app.verification.email-expiry-minutes}")
    private int emailExpiryMinutes;

    // Count recent attempts, including unsuccessful sends and welcome emails.
    public void checkSendingLimit(String email) {
        LocalDateTime minuteAgo = LocalDateTime.now().minusMinutes(1);
        long count = emailHistoryRepository.countByEmailAndCreatedDateAfter(email, minuteAgo);
        if (count >= 4) {
            throw new BadRequestException("Too many email requests. Please try again later.");
        }
    }

    // Read a classpath template and close its stream automatically.
    private String loadTemplate(String path) {
        ClassPathResource resource = new ClassPathResource("templates/" + path);
        try (InputStream input = resource.getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load email template.", e);
        }
    }

    // Record the attempt before sending; mark accepted only after SMTP succeeds.
    public void sendVerificationLink(String email) {
        checkSendingLimit(email);
        String token = UUID.randomUUID().toString();
        LocalDateTime currentTime = LocalDateTime.now();
        String verificationUrl = verificationBaseUrl + "/api/v1/auth/email/verification?token=" + token;
        String html = loadTemplate("email-verification.html").replace("{{verificationUrl}}", verificationUrl).replace("{{expiryMinutes}}", String.valueOf(emailExpiryMinutes));

        EmailHistory emailHistory = new EmailHistory();
        emailHistory.setEmail(email);
        emailHistory.setMessage(html);
        emailHistory.setToken(token);
        emailHistory.setCreatedDate(currentTime);
        emailHistory.setExpiresAt(currentTime.plusMinutes(emailExpiryMinutes));
        emailHistory.setSent(false);
        emailHistory.setType(EmailType.VERIFICATION);
        emailHistoryRepository.save(emailHistory);

        emailService.send(email, "Verify your KUN.UZ email", html);

        emailHistory.setSent(true);
        emailHistoryRepository.save(emailHistory);

    }

    // Reject unsent, used, expired, or non-verification records.
    public EmailHistory getValidToken(String token) {
        EmailHistory emailHistory = emailHistoryRepository.findByToken(token).orElseThrow(() -> new BadRequestException("Verification link is invalid."));
        if (emailHistory.getType() != EmailType.VERIFICATION || !emailHistory.isSent() || emailHistory.getUsedDate() != null || emailHistory.getExpiresAt() == null || !LocalDateTime.now().isBefore(emailHistory.getExpiresAt())) {
            throw new BadRequestException("Verification link is invalid, expired, or already used.");
        }
        return emailHistory;
    }

    // Personalize the welcome template and record the sending attempt.
    public void sendWelcomeEmail(String email, String name) {
        checkSendingLimit(email);
        String html = loadTemplate("email-welcome.html").replace("{{name}}", HtmlUtils.htmlEscape(name));
        EmailHistory emailHistory = new EmailHistory();
        emailHistory.setEmail(email);
        emailHistory.setMessage(html);
        emailHistory.setCreatedDate(LocalDateTime.now());
        emailHistory.setType(EmailType.WELCOME);
        emailHistory.setSent(false);
        emailHistoryRepository.save(emailHistory);
        emailService.send(email, "Welcome to KUN.UZ", html);
        emailHistory.setSent(true);
        emailHistoryRepository.save(emailHistory);

    }

    // Return one recipient's history, newest first.
    @Transactional(readOnly = true)
    public Page<EmailHistoryResponseDTO> getByEmail(String email, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate", "id"));
        Page<EmailHistory> result = emailHistoryRepository.findAllByEmail(email, pageable);
        return result.map(this::toDTO);
    }

    // Search from midnight inclusive to the following midnight exclusive.
    @Transactional(readOnly = true)
    public Page<EmailHistoryResponseDTO> getByDate(LocalDate date, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate", "id"));
        LocalDateTime fromDate = date.atStartOfDay();
        LocalDateTime toDate = date.plusDays(1).atStartOfDay();
        Page<EmailHistory> result = emailHistoryRepository.findAllByCreatedDateGreaterThanEqualAndCreatedDateLessThan(fromDate, toDate, pageable);
        return result.map(this::toDTO);
    }

    // Return newest records first using zero-based service pagination.
    @Transactional(readOnly = true)
    public Page<EmailHistoryResponseDTO> getAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate", "id"));
        Page<EmailHistory> result = emailHistoryRepository.findAll(pageable);
        return result.map(this::toDTO);
    }

    private EmailHistoryResponseDTO toDTO(EmailHistory history) {
        EmailHistoryResponseDTO response = new EmailHistoryResponseDTO();
        response.setId(history.getId());
        response.setEmail(history.getEmail());
        response.setMessage(history.getMessage());
        response.setType(history.getType());
        response.setSent(history.isSent());
        response.setCreatedDate(history.getCreatedDate());
        return response;
    }

}
