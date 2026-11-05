package dasturlash.uz.repository;

import dasturlash.uz.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByIdAndVisibleTrue (Integer id);
    Page<Category> findAllByVisibleTrue (Pageable pageable);
}
