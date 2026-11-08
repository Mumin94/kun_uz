package dasturlash.uz.service;

import dasturlash.uz.dto.request.SmsSendRequestDTO;
import dasturlash.uz.entity.SmsHistory;
import dasturlash.uz.enums.SmsStatus;
import dasturlash.uz.exception.BadRequestException;
import dasturlash.uz.repository.SmsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SmsHistoryService {
    private final SmsHistoryRepository smsHistoryRepository;
    private final SmsClientService smsClientService;
    private final SecureRandom secureRandom = new SecureRandom();

    public void checkSendingLimit(String phone) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(1);
        long count = smsHistoryRepository.countByPhoneAndCreatedDateAfter(phone, since);
        if (count >= 4) throw new BadRequestException("Too many SMS requests. Please try again later.");
    }

    private String generateCode() {
        int randomNumber = secureRandom.nextInt(1_000_000);
        return String.format("%06d", randomNumber);
    }

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
        smsHistory.setMock(true);
        smsHistoryRepository.save(smsHistory);
    }

    public SmsHistory getValidCode(String phone, String code) {
        SmsHistory smsHistory = smsHistoryRepository.findTopByPhoneAndSentTrueOrderByCreatedDateDescIdDesc(phone).orElseThrow(() -> new BadRequestException("No verification code was sent to this phone."));
        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(smsHistory.getExpiresAt()) || smsHistory.getSmsStatus() == SmsStatus.USED || !smsHistory.getCode().equals(code)) {
            throw new BadRequestException("Verification code is invalid, expired, or already used.");
        }

        return smsHistory;
    }


}
