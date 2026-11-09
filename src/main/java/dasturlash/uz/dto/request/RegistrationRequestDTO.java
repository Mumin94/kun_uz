package dasturlash.uz.dto.request;

import dasturlash.uz.enums.VerificationChannel;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequestDTO {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @NotBlank
    @Size(min = 3, max = 50)
    private String surname;
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\+998\\d{9}$")
    private String phone;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotNull
    private VerificationChannel verificationChannel = VerificationChannel.SMS;

}
