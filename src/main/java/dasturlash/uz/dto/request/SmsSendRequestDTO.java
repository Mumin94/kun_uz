package dasturlash.uz.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsSendRequestDTO {
    @NotBlank
    private String text;

    @NotBlank
    private String phone;

    @NotBlank
    @JsonProperty("client_id")
    private String clientId;
}
