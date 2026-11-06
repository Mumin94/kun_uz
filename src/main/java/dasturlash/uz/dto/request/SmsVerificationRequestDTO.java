package dasturlash.uz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsVerificationRequestDTO {
    @NotBlank
    @Pattern(regexp = "^\\+998\\d{9}$")
    private String phone;

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$")
    private String code;
}
