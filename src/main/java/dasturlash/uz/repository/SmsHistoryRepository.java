package dasturlash.uz.repository;

import dasturlash.uz.entity.SmsHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SmsHistoryRepository extends JpaRepository<SmsHistory, Integer> {
    Optional<SmsHistory> findTopByPhoneAndSentTrueOrderByCreatedDateDescIdDesc(String phone);

    long countByPhoneAndCreatedDateAfter(String phone, LocalDateTime since);

    Page<SmsHistory> findAllByPhone(String phone, Pageable pageable);

    Page<SmsHistory> findAllByCreatedDateGreaterThanEqualAndCreatedDateLessThan(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
