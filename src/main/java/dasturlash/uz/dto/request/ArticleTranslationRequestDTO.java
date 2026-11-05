package dasturlash.uz.dto.request;

import dasturlash.uz.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleTranslationRequestDTO {
    @NotNull
    private Language language;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String content;
}
