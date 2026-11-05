package dasturlash.uz.repository;

import dasturlash.uz.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Integer> {
    Optional<Profile> findByIdAndVisibleTrue(Integer id);
    Page<Profile> findAllByVisibleTrue(Pageable pageable);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByEmailAndIdNot(String email, Integer id);
    boolean existsByPhoneAndIdNot(String phone, Integer id);
}
