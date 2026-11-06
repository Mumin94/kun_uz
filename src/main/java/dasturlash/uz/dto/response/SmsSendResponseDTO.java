package dasturlash.uz.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsSendResponseDTO {

    private boolean success;
    private boolean mock;
    private String message;
}
