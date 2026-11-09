package dasturlash.uz.dto.response;

import dasturlash.uz.enums.SmsStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SmsHistoryResponseDTO {
    private Integer id;
    private String phone;
    private String message;
    private SmsStatus smsStatus;
    private boolean sent;
    private LocalDateTime createdDate;
    private LocalDateTime usedDate;
}
