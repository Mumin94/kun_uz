package dasturlash.uz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsResendRequestDTO {
    @NotBlank
    @Pattern(regexp = "^\\+998\\d{9}$")
    private String phone;
}
