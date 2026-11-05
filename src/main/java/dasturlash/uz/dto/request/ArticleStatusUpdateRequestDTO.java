package dasturlash.uz.dto.request;

import dasturlash.uz.enums.ArticleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleStatusUpdateRequestDTO {
    @NotNull
    private ArticleStatus status;
}
