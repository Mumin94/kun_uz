package dasturlash.uz.dto.request;

import dasturlash.uz.enums.ArticleStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ArticleUpdateRequestDTO {
    @NotBlank
    private String imageId;

    @NotNull
    @Positive
    private Integer regionId;

    @NotNull
    @Positive
    private Integer categoryId;

    @NotEmpty
    private Set<@NotNull @Positive Integer> articleTypeIds;

    @NotEmpty
    @Valid
    private List<@NotNull ArticleTranslationRequestDTO> translations;

    @NotNull
    private Set<@NotBlank @Size(max = 100) String> tagNames;

}
