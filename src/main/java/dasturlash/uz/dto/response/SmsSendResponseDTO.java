package dasturlash.uz.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SmsSendResponseDTO {
    private String id;
    private String clientId;
    private String phone;
    private String text;
    private BigDecimal balanceAfter;
    private BigDecimal price;
    private String status;
    private LocalDateTime createdDate;
}
