package dasturlash.uz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ArticleExcludeRequestDTO {

    @NotNull
    private Set<@NotBlank String> excludedIds;
}