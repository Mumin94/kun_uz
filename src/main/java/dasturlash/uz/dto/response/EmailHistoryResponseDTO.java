package dasturlash.uz.dto.response;

import dasturlash.uz.enums.EmailType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EmailHistoryResponseDTO {
    private Integer id;
    private String email;
    private String message;
    private EmailType type;
    private boolean sent;
    private LocalDateTime createdDate;
}
