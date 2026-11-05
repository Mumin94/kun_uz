package dasturlash.uz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "article_type")
public class ArticleType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer orderNumber;

    @Column(length = 50, nullable = false)
    private String nameUz;

    @Column(length = 50, nullable = false)
    private String nameRu;

    @Column(length = 50, nullable = false)
    private String nameEn;

    @Column(nullable = false)
    private Boolean visible = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

}
