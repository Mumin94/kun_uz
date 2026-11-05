package dasturlash.uz.dto.response;

import dasturlash.uz.enums.Language;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleTranslationResponseDTO {
    private Language language;
    private String title;
    private String description;
    private String content;
}
