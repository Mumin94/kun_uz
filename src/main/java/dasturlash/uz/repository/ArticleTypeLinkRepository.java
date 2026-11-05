package dasturlash.uz.repository;

import dasturlash.uz.entity.ArticleTypeLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleTypeLinkRepository extends JpaRepository<ArticleTypeLink, Integer> {
    List<ArticleTypeLink> findAllByArticle_Id(String articleId);
    void deleteAllByArticle_Id(String articleId);
}
