package dasturlash.uz.dto.request;

import dasturlash.uz.enums.ArticleStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class ArticleFilterRequestDTO {
    private String id;
    @Size(max = 255)
    private String title;
    @Positive
    private Integer regionId;
    @Positive
    private Integer categoryId;
    private LocalDate createdDateFrom;
    private LocalDate createdDateTo;
    private LocalDate publishedDateFrom;
    private LocalDate publishedDateTo;
    @Positive
    private Integer moderatorId;
    @Positive
    private Integer publisherId;
    private ArticleStatus status;
}
