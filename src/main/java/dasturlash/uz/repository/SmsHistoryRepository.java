package dasturlash.uz.repository;

import dasturlash.uz.entity.SmsHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SmsHistoryRepository extends JpaRepository<SmsHistory, Integer> {
    Optional<SmsHistory> findTopByPhoneAndSentTrueOrderByCreatedDateDescIdDesc(String phone);

    long countByPhoneAndCreatedDateAfter(String phone, LocalDateTime since);
}
