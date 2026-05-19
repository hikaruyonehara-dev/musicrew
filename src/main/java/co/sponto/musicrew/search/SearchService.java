package co.sponto.musicrew.search;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.sponto.musicrew.block.BlockRepository;
import co.sponto.musicrew.profile.Country;
import co.sponto.musicrew.profile.Profile;
import co.sponto.musicrew.profile.ProfileRepository;

@Service
public class SearchService {
    private final ProfileRepository profileRepository;
    private final BlockRepository blockRepository;

    public SearchService(ProfileRepository profileRepository, BlockRepository blockRepository) {
        this.profileRepository = profileRepository;
        this.blockRepository = blockRepository;
    }

    @Transactional
    public List<Profile> search(List<Long> instrumentIds, List<Long> genreIds,
            Country country, String city, Long excludeUserId) {

        Set<Long> blockedIds = excludeUserId == null
                ? Set.of()
                : blockRepository.findUserIdsBlockedBetween(excludeUserId);

        Specification<Profile> spec = Specification.where(ProfileSpecs.isVisible())
                .and(ProfileSpecs.hasAnyInstrument(instrumentIds))
                .and(ProfileSpecs.hasAnyGenre(genreIds))
                .and(ProfileSpecs.inCountry(country))
                .and(ProfileSpecs.cityContains(city))
                .and(ProfileSpecs.notOwnedByUser(excludeUserId))
                .and(ProfileSpecs.notInUserIds(blockedIds));

        return profileRepository.findAll(spec);
    }
}
