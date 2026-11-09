package dasturlash.uz.service;

import dasturlash.uz.dto.request.SmsSendRequestDTO;
import dasturlash.uz.dto.response.EmailHistoryResponseDTO;
import dasturlash.uz.dto.response.SmsHistoryResponseDTO;
import dasturlash.uz.entity.EmailHistory;
import dasturlash.uz.entity.SmsHistory;
import dasturlash.uz.enums.SmsStatus;
import dasturlash.uz.exception.BadRequestException;
import dasturlash.uz.repository.SmsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SmsHistoryService {
    private final SmsHistoryRepository smsHistoryRepository;
    private final SmsClientService smsClientService;
    private final SecureRandom secureRandom = new SecureRandom();

    // Count SMS attempts during the previous minute.
    public void checkSendingLimit(String phone) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(1);
        long count = smsHistoryRepository.countByPhoneAndCreatedDateAfter(phone, since);
        if (count >= 4) throw new BadRequestException("Too many SMS requests. Please try again later.");
    }

    // Preserve leading zeros so every code contains six digits.
    private String generateCode() {
        int randomNumber = secureRandom.nextInt(1_000_000);
        return String.format("%06d", randomNumber);
    }

    // Save the attempt before the provider call, then mark it accepted.
    public void sendVerificationCode(String phone) {
        checkSendingLimit(phone);
        String code = generateCode();
        LocalDateTime now = LocalDateTime.now();
        SmsHistory smsHistory = new SmsHistory();
        smsHistory.setSmsStatus(SmsStatus.NEW);
        smsHistory.setPhone(phone);
        smsHistory.setCreatedDate(now);
        smsHistory.setExpiresAt(now.plusMinutes(2));
        smsHistory.setCode(code);
        smsHistory.setMessage("Your KUN.UZ verification code is: " + code);
        smsHistoryRepository.save(smsHistory);

        SmsSendRequestDTO request = new SmsSendRequestDTO();
        request.setPhone(smsHistory.getPhone());
        request.setText(smsHistory.getMessage());
        smsClientService.send(request);

        smsHistory.setSent(true);
        smsHistoryRepository.save(smsHistory);
    }

    // Validate only the latest accepted code; activation consumes it later.
    public SmsHistory getValidCode(String phone, String code) {
        SmsHistory smsHistory = smsHistoryRepository.findTopByPhoneAndSentTrueOrderByCreatedDateDescIdDesc(phone).orElseThrow(() -> new BadRequestException("No verification code was sent to this phone."));
        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(smsHistory.getExpiresAt()) || smsHistory.getSmsStatus() == SmsStatus.USED || !smsHistory.getCode().equals(code)) {
            throw new BadRequestException("Verification code is invalid, expired, or already used.");
        }

        return smsHistory;
    }

    // Return one phone number's history, newest first.
    @Transactional(readOnly = true)
    public Page<SmsHistoryResponseDTO> getByPhone(String phone, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate", "id"));
        Page<SmsHistory> result = smsHistoryRepository.findAllByPhone(phone, pageable);
        return result.map(this::toDTO);

    }

    // Search from midnight inclusive to the following midnight exclusive.
    @Transactional(readOnly = true)
    public Page<SmsHistoryResponseDTO> getByDate(LocalDate date, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate", "id"));
        LocalDateTime fromDate = date.atStartOfDay();
        LocalDateTime toDate = date.plusDays(1).atStartOfDay();
        Page<SmsHistory> result = smsHistoryRepository.findAllByCreatedDateGreaterThanEqualAndCreatedDateLessThan(fromDate, toDate, pageable);
        return result.map(this::toDTO);
    }

    // Return newest records first using zero-based service pagination.
    @Transactional(readOnly = true)
    public Page<SmsHistoryResponseDTO> getAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate", "id"));
        Page<SmsHistory> result = smsHistoryRepository.findAll(pageable);
        return result.map(this::toDTO);
    }

    private SmsHistoryResponseDTO toDTO(SmsHistory history) {
        SmsHistoryResponseDTO response = new SmsHistoryResponseDTO();
        response.setId(history.getId());
        response.setMessage(history.getMessage());
        response.setPhone(history.getPhone());
        response.setSent(history.isSent());
        response.setSmsStatus(history.getSmsStatus());
        response.setCreatedDate(history.getCreatedDate());
        response.setUsedDate(history.getUsedDate());

        return response;
    }

}
