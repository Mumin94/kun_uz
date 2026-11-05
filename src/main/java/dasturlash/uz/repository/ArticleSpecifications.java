package dasturlash.uz.repository;

import dasturlash.uz.dto.request.ArticleFilterRequestDTO;
import dasturlash.uz.entity.*;
import dasturlash.uz.enums.ArticleStatus;
import dasturlash.uz.enums.Language;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ArticleSpecifications {
    private ArticleSpecifications() {}

    public static Specification<Article> publicArticles(
            Language language, Integer typeId, Integer regionId,
            Integer categoryId, String tagName) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("visible")));
            predicates.add(cb.equal(root.get("status"), ArticleStatus.PUBLISHED));
            predicates.add(hasTranslation(root, query, cb, language, null));
            if (regionId != null) {
                predicates.add(cb.equal(root.get("region").get("id"), regionId));
                predicates.add(cb.isTrue(root.get("region").get("visible")));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
                predicates.add(cb.isTrue(root.get("category").get("visible")));
            }
            if (typeId != null) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                Root<ArticleTypeLink> link = sub.from(ArticleTypeLink.class);
                sub.select(link.get("id")).where(
                        cb.equal(link.get("article"), root),
                        cb.equal(link.get("articleType").get("id"), typeId),
                        cb.isTrue(link.get("articleType").get("visible")));
                predicates.add(cb.exists(sub));
            }
            if (tagName != null) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                Root<ArticleTag> link = sub.from(ArticleTag.class);
                sub.select(link.get("id")).where(
                        cb.equal(link.get("article"), root),
                        cb.equal(link.get("tag").get("name"), tagName));
                predicates.add(cb.exists(sub));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Article> filter(ArticleFilterRequestDTO f, Language language) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.isTrue(root.get("visible")));
            p.add(hasTranslation(root, query, cb, language, f.getTitle()));
            if (f.getId() != null) p.add(cb.equal(root.get("id"), f.getId()));
            if (f.getRegionId() != null) p.add(cb.equal(root.get("region").get("id"), f.getRegionId()));
            if (f.getCategoryId() != null) p.add(cb.equal(root.get("category").get("id"), f.getCategoryId()));
            if (f.getModeratorId() != null) p.add(cb.equal(root.get("moderator").get("id"), f.getModeratorId()));
            if (f.getPublisherId() != null) p.add(cb.equal(root.get("publisher").get("id"), f.getPublisherId()));
            if (f.getStatus() != null) p.add(cb.equal(root.get("status"), f.getStatus()));
            if (f.getCreatedDateFrom() != null) p.add(cb.greaterThanOrEqualTo(root.get("createdDate"), f.getCreatedDateFrom().atStartOfDay()));
            if (f.getCreatedDateTo() != null) p.add(cb.lessThan(root.get("createdDate"), f.getCreatedDateTo().plusDays(1).atStartOfDay()));
            if (f.getPublishedDateFrom() != null) p.add(cb.greaterThanOrEqualTo(root.get("publishedDate"), f.getPublishedDateFrom().atStartOfDay()));
            if (f.getPublishedDateTo() != null) p.add(cb.lessThan(root.get("publishedDate"), f.getPublishedDateTo().plusDays(1).atStartOfDay()));
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    private static Predicate hasTranslation(Root<Article> article, CriteriaQuery<?> query,
            CriteriaBuilder cb, Language language, String title) {
        Subquery<Integer> sub = query.subquery(Integer.class);
        Root<ArticleTranslation> t = sub.from(ArticleTranslation.class);
        List<Predicate> p = new ArrayList<>();
        p.add(cb.equal(t.get("article"), article));
        p.add(cb.equal(t.get("language"), language));
        if (title != null && !title.isBlank()) {
            String escaped = title.strip().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_");
            p.add(cb.like(cb.lower(t.get("title")), "%" + escaped + "%", '!'));
        }
        sub.select(t.get("id")).where(p.toArray(new Predicate[0]));
        return cb.exists(sub);
    }
}
