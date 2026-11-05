package dasturlash.uz.repository;

import dasturlash.uz.entity.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Integer> {
    Optional<Region> findByIdAndVisibleTrue (Integer id);
    Page<Region> findAllByVisibleTrue (Pageable pageable);
    List<Region> findAllByVisibleTrueOrderByOrderNumberAscIdAsc();
}
