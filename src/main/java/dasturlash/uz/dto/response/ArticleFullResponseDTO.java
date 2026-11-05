package dasturlash.uz.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ArticleFullResponseDTO {

    private String id;
    private String title;
    private String description;
    private String content;

    private ArticleImageResponseDTO image;
    private ArticleLocationResponseDTO region;
    private ArticleLocationResponseDTO category;

    private LocalDateTime publishedDate;
    private Long sharedCount;
    private Long viewCount;
    private Long likeCount;

    private List<String> tagList;
}