package dasturlash.uz.repository;

import dasturlash.uz.entity.ArticleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleTypeRepository extends JpaRepository<ArticleType, Integer> {
    Optional<ArticleType> findByIdAndVisibleTrue(Integer id);
    Page<ArticleType>findAllByVisibleTrue(Pageable pageable);
}
