package co.sponto.musicrew.search;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import co.sponto.musicrew.profile.Country;
import co.sponto.musicrew.profile.Profile;
import co.sponto.musicrew.profile.ProfileRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchService {
    private final ProfileRepository profileRepository;

    public SearchService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional
    public List<Profile> search(List<Long> instrumentIds,
            List<Long> genreIds,
            Country country,
            String city,
            Long excludeUserId) {
        Specification<Profile> spec = Specification.where(ProfileSpecs.isVisible())
                .and(ProfileSpecs.hasAnyInstrument(instrumentIds))
                .and(ProfileSpecs.hasAnyGenre(genreIds))
                .and(ProfileSpecs.inCountry(country))
                .and(ProfileSpecs.cityContains(city))
                .and(ProfileSpecs.notOwnedByUser(excludeUserId));
        return profileRepository.findAll(spec);
    }

}
