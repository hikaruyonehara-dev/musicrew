package co.sponto.musicrew.listing;

import java.util.List;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import co.sponto.musicrew.profile.Country;
import co.sponto.musicrew.profile.Profile;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class ListingSpecs {

    private ListingSpecs() {
    }

    /**
     * Listing is visible in the feed when:
     * - listing.active = true (owner hasn't marked it filled)
     * - poster's User is enabled (account not deleted)
     * - poster's Profile is not hidden
     */
    public static Specification<Listing> isActive() {
        return (root, query, cb) -> {
            Predicate listingActive = cb.isTrue(root.get("active"));
            Predicate userEnabled = cb.isTrue(root.get("user").get("enabled"));

            // Subquery: which user_ids have a non-hidden profile?
            Subquery<Long> sq = query.subquery(Long.class);
            Root<Profile> profile = sq.from(Profile.class);
            sq.select(profile.get("user").get("id"))
                    .where(cb.isFalse(profile.get("hidden")));
            Predicate profileNotHidden = root.get("user").get("id").in(sq);

            return cb.and(listingActive, userEnabled, profileNotHidden);
        };
    }

    public static Specification<Listing> hasAnyInstrument(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }

        return (root, query, cb) -> {
            query.distinct(true);
            Join<Object, Object> join = root.join("instrumentsNeeded");
            return join.get("id").in(ids);
        };
    }

    public static Specification<Listing> hasAnyGenre(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }

        return (root, query, cb) -> {
            query.distinct(true);
            Join<Object, Object> join = root.join("genres");
            return join.get("id").in(ids);
        };
    }

    public static Specification<Listing> inCountry(Country country) {
        if (country == null) {
            return null;
        }

        return (root, query, cb) -> cb.equal(root.get("country"), country);
    }

    public static Specification<Listing> notFromUserIds(java.util.Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> cb.not(root.get("user").get("id").in(userIds));
    }

}
