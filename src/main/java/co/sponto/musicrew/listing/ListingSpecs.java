package co.sponto.musicrew.listing;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import co.sponto.musicrew.profile.Country;
import jakarta.persistence.criteria.Join;

public final class ListingSpecs {

    private ListingSpecs() {
    }

    public static Specification<Listing> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
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

}
