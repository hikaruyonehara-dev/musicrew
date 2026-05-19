package co.sponto.musicrew.search;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import co.sponto.musicrew.profile.Country;
import co.sponto.musicrew.profile.Profile;
import jakarta.persistence.criteria.Join;

public final class ProfileSpecs {

    private ProfileSpecs() {
    }

    /**
     * Only profiles with a pic AND at least one video link AND not hidden AND user
     * enabled.
     */
    public static Specification<Profile> isVisible() {
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("profilePicPath")),
                cb.greaterThan(cb.size(root.get("videoLinks")), 0),
                cb.isFalse(root.get("hidden")),
                cb.isTrue(root.get("user").get("enabled")));
    }

    public static Specification<Profile> hasAnyInstrument(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }

        return (root, query, cb) -> {
            query.distinct(true);
            Join<Object, Object> join = root.join("instruments");
            return join.get("id").in(ids);
        };
    }

    public static Specification<Profile> hasAnyGenre(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }

        return (root, query, cb) -> {
            query.distinct(true);
            Join<Object, Object> join = root.join("genres");
            return join.get("id").in(ids);
        };
    }

    public static Specification<Profile> inCountry(Country country) {
        if (country == null) {
            return null;
        }

        return (root, query, cb) -> cb.equal(root.get("country"), country);
    }

    public static Specification<Profile> cityContains(String city) {
        if (city == null || city.isBlank()) {
            return null;
        }
        String pattern = "%" + city.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("city")), pattern);
    }

    public static Specification<Profile> notOwnedByUser(Long userId) {
        if (userId == null)
            return null;
        return (root, query, cb) -> cb.notEqual(root.get("user").get("id"), userId);
    }

    public static Specification<Profile> notInUserIds(java.util.Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> cb.not(root.get("user").get("id").in(userIds));
    }

}
