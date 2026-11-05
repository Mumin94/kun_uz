package dasturlash.uz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ArticleTypeRequestDTO {
    @NotNull
    @Positive
    private Integer orderNumber;

    @NotBlank
    @Size(max = 50)
    private String nameUz;

    @NotBlank
    @Size(max = 50)
    private String nameRu;

    @NotBlank
    @Size(max = 50)
    private String nameEn;
}
