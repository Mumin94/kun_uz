package dasturlash.uz.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ArticleShortResponseDTO {
    private String id;
    private String title;
    private String description;
    private ArticleImageResponseDTO image;
    private LocalDateTime publishedDate;
}