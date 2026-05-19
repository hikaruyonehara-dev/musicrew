package co.sponto.musicrew.listing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    List<Listing> findByUserIdOrderByCreatedAtDesc(Long userId);
}
