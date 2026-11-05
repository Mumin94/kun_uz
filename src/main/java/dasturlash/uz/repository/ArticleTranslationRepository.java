package dasturlash.uz.repository;

import dasturlash.uz.entity.ArticleTranslation;
import dasturlash.uz.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleTranslationRepository extends JpaRepository<ArticleTranslation, Integer> {
    List<ArticleTranslation> findAllByArticle_Id(String articleId);

    Optional<ArticleTranslation> findByArticle_IdAndLanguage(String articleId, Language language);
}
