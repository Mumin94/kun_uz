package dasturlash.uz.repository;

import dasturlash.uz.entity.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Integer> {

    List<ArticleTag> findAllByArticle_Id(String articleId);
}