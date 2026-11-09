package dasturlash.uz.repository;

import dasturlash.uz.entity.EmailHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailHistoryRepository extends JpaRepository<EmailHistory, Integer> {
    Optional<EmailHistory> findByToken(String token);

    long countByEmailAndCreatedDateAfter(String email, LocalDateTime since);

    Page<EmailHistory> findAllByEmail(String email, Pageable pageable);

    Page<EmailHistory> findAllByCreatedDateGreaterThanEqualAndCreatedDateLessThan(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
