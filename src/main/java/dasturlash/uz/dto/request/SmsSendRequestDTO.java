package dasturlash.uz.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsSendRequestDTO {
    @NotBlank
    private String message;

    @NotBlank
    private String phone;
}
