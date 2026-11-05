package dasturlash.uz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Table(name = "article_type_link", uniqueConstraints = @UniqueConstraint(columnNames = {"article_id", "article_type_id"}))
@NoArgsConstructor
public class ArticleTypeLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "article_type_id", nullable = false)
    private ArticleType articleType;

}
