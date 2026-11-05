package dasturlash.uz.dto.response;

import dasturlash.uz.enums.ArticleStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ArticleResponseDTO {
    private String id;
    private String imageId;
    private Integer regionId;
    private Integer categoryId;
    private Integer moderatorId;
    private Integer publisherId;
    private ArticleStatus status;
    private Long sharedCount;
    private Long viewCount;
    private Boolean visible;
    private LocalDateTime createdDate;
    private LocalDateTime publishedDate;
    private Set<Integer> articleTypeIds;
    private List<ArticleTranslationResponseDTO> translations;

}
