package dasturlash.uz.specification;

import dasturlash.uz.entity.Profile;
import dasturlash.uz.enums.ProfileRole;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

public class ProfileSpecifications {
    // Always exclude soft-deleted profiles.
    public static Specification<Profile> isVisible() {
        return (root, query, cb) -> cb.isTrue(root.get("visible"));
    }

    // Ignore an omitted role; otherwise require an exact match.
    public static Specification<Profile> hasRole(ProfileRole role) {
        return (root, query, cb) -> {
            if (role == null) return cb.conjunction();
            return cb.equal(root.get("role"), role);
        };
    }

    // Ignore blank input; otherwise match the trimmed phone exactly.
    public static Specification<Profile> hasPhone(String phone) {
        return (root, query, cb) -> {
            if (phone == null || phone.isBlank()) return cb.conjunction();
            return cb.equal(root.get("phone"), phone.strip());
        };
    }

    // Match a name fragment without case sensitivity.
    public static Specification<Profile> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("name")), "%" + name.strip().toLowerCase(Locale.ROOT) + "%");
        };
    }

    // Match a surname fragment without case sensitivity.
    public static Specification<Profile> hasSurname(String surname) {
        return (root, query, cb) -> {
            if (surname == null || surname.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("surname")), "%" + surname.strip().toLowerCase(Locale.ROOT) + "%");
        };
    }

    // Include records from the start of the supplied date.
    public static Specification<Profile> createdFrom(LocalDate fromDate) {
        return (root, query, cb) -> {
            if (fromDate == null) {
                return cb.conjunction();
            } else {
                return cb.greaterThanOrEqualTo(root.get("createdDate"), fromDate.atStartOfDay());
            }
        };
    }

    // Include the whole final date by excluding the next midnight.
    public static Specification<Profile> createdTo(LocalDate toDate) {
        return (root, query, cb) -> {
            if (toDate == null) {
                return cb.conjunction();
            } else {
                return cb.lessThan(root.get("createdDate"), toDate.plusDays(1).atStartOfDay());
            }
        };
    }
}
