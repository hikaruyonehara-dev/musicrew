package co.sponto.musicrew.listing;

import co.sponto.musicrew.block.BlockRepository;
import co.sponto.musicrew.profile.Country;
import co.sponto.musicrew.profile.Genre;
import co.sponto.musicrew.profile.GenreRepository;
import co.sponto.musicrew.profile.Instrument;
import co.sponto.musicrew.profile.InstrumentRepository;
import co.sponto.musicrew.user.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final InstrumentRepository instrumentRepository;
    private final GenreRepository genreRepository;
    private final BlockRepository blockRepository;

    public ListingService(ListingRepository listingRepository, InstrumentRepository instrumentRepository,
            GenreRepository genreRepository, BlockRepository blockRepository) {
        this.listingRepository = listingRepository;
        this.instrumentRepository = instrumentRepository;
        this.genreRepository = genreRepository;
        this.blockRepository = blockRepository;
    }

    public Listing getById(Long id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Listing> feed(List<Long> instrumentIds, List<Long> genreIds,
            Country country, Long viewerUserId) {
        Set<Long> blockedIds = viewerUserId == null
                ? Set.of()
                : blockRepository.findUserIdsBlockedBetween(viewerUserId);

        Specification<Listing> spec = Specification.where(ListingSpecs.isActive())
                .and(ListingSpecs.hasAnyInstrument(instrumentIds))
                .and(ListingSpecs.hasAnyGenre(genreIds))
                .and(ListingSpecs.inCountry(country))
                .and(ListingSpecs.notFromUserIds(blockedIds));
        return listingRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<Listing> findUserListings(Long userId) {
        return listingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Listing create(User user,
            String title,
            String description,
            List<Long> instrumentIds,
            List<Long> genreIds,
            Country country,
            String city) {
        Listing listing = new Listing(user, title.trim(), description.trim());
        listing.setCountry(country);
        listing.setCity(city == null ? null : city.trim());
        listing.setInstrumentsNeeded(resolveInstruments(instrumentIds));
        listing.setGenres(resolveGenres(genreIds));
        return listingRepository.save(listing);
    }

    @Transactional
    public Listing update(Long listingId,
            Long ownerId,
            String title,
            String description,
            List<Long> instrumentIds,
            List<Long> genreIds,
            Country country,
            String city) {
        Listing listing = getOwnedById(listingId, ownerId);
        listing.setTitle(title.trim());
        listing.setDescription(description.trim());
        listing.setCountry(country);
        listing.setCity(city == null ? null : city.trim());
        listing.setInstrumentsNeeded(resolveInstruments(instrumentIds));
        listing.setGenres(resolveGenres(genreIds));
        listing.touch();
        return listing;
    }

    @Transactional
    public void close(Long listingId, Long ownerId) {
        Listing listing = getOwnedById(listingId, ownerId);
        listing.setActive(false);
        listing.touch();
    }

    @Transactional
    public void delete(Long listingId, Long ownerId) {
        Listing listing = getOwnedById(listingId, ownerId);
        listingRepository.delete(listing);
    }

    /** Throws SecurityException if the caller doesn't own this listing. */
    private Listing getOwnedById(Long listingId, Long ownerId) {
        Listing listing = getById(listingId);
        if (!listing.getUser().getId().equals(ownerId)) {
            throw new SecurityException("You are not the owner of this listing");
        }
        return listing;
    }

    private Set<Instrument> resolveInstruments(List<Long> ids) {
        return ids == null ? new HashSet<>()
                : new HashSet<>(instrumentRepository.findAllById(ids));
    }

    private Set<Genre> resolveGenres(List<Long> ids) {
        return ids == null ? new HashSet<>()
                : new HashSet<>(genreRepository.findAllById(ids));
    }

}
