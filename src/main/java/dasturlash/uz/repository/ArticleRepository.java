package dasturlash.uz.repository;

import dasturlash.uz.entity.Article;
import dasturlash.uz.enums.ArticleStatus;
import dasturlash.uz.enums.Language;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ArticleRepository extends JpaRepository<Article, String> {
    Optional<Article> findByIdAndVisibleTrue(String id);

    Optional<Article> findByIdAndVisibleTrueAndStatus(String id, ArticleStatus status);


    @Query("""
            SELECT a
            FROM Article a
            WHERE a.visible = true
              AND a.status = :status
              AND EXISTS (
                  SELECT link.id
                  FROM ArticleTypeLink link
                  WHERE link.article = a
                    AND link.articleType.id = :typeId
                    AND link.articleType.visible = true
              )
              AND EXISTS (
                  SELECT translation.id
                  FROM ArticleTranslation translation
                  WHERE translation.article = a
                    AND translation.language = :language
              )
            ORDER BY a.createdDate DESC, a.id DESC
            """)
    List<Article> findLatestByType(@Param("typeId") Integer typeId, @Param("language") Language language, @Param("status") ArticleStatus status, Pageable pageable);


    @Query("""
            SELECT a
            FROM Article a
            WHERE a.visible = true
              AND a.status = :status
              AND EXISTS (
                  SELECT t.id
                  FROM ArticleTranslation t
                  WHERE t.article = a
                    AND t.language = :language
              )
            ORDER BY a.createdDate DESC, a.id DESC
            """)
    List<Article> findLatestPublished(@Param("language") Language language, @Param("status") ArticleStatus status, Pageable pageable);

    @Query("""
            SELECT a
            FROM Article a
            WHERE a.visible = true
              AND a.status = :status
              AND a.id NOT IN :excludedIds
              AND EXISTS (
                  SELECT t.id
                  FROM ArticleTranslation t
                  WHERE t.article = a
                    AND t.language = :language
              )
            ORDER BY a.createdDate DESC, a.id DESC
            """)
    List<Article> findLatestPublishedExcludingIds(@Param("excludedIds") Set<String> excludedIds, @Param("language") Language language, @Param("status") ArticleStatus status, Pageable pageable);

    @Query("""
            SELECT a
            FROM Article a
            WHERE a.visible = true
              AND a.status = :status
              AND a.id <> :excludedId
              AND EXISTS (
                  SELECT link.id
                  FROM ArticleTypeLink link
                  WHERE link.article = a
                    AND link.articleType.id = :typeId
                    AND link.articleType.visible = true
              )
              AND EXISTS (
                  SELECT t.id
                  FROM ArticleTranslation t
                  WHERE t.article = a
                    AND t.language = :language
              )
            ORDER BY a.createdDate DESC, a.id DESC
            """)
    List<Article> findLatestByTypeExcludingId(@Param("typeId") Integer typeId, @Param("excludedId") String excludedId, @Param("language") Language language, @Param("status") ArticleStatus status, Pageable pageable);

    @Query("""
            SELECT a
            FROM Article a
            WHERE a.visible = true
              AND a.status = :status
              AND EXISTS (
                  SELECT t.id
                  FROM ArticleTranslation t
                  WHERE t.article = a
                    AND t.language = :language
              )
            ORDER BY a.viewCount DESC, a.createdDate DESC, a.id DESC
            """)
    List<Article> findMostRead(@Param("language") Language language, @Param("status") ArticleStatus status, Pageable pageable);


}
