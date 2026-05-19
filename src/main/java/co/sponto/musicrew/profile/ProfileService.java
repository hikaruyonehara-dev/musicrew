package co.sponto.musicrew.profile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import co.sponto.musicrew.listing.Listing;
import co.sponto.musicrew.listing.ListingRepository;
import co.sponto.musicrew.upload.FileStorageService;
import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final InstrumentRepository instrumentRepository;
    private final GenreRepository genreRepository;
    private final FileStorageService fileStorage;
    private final ListingRepository listingRepository;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository,
            InstrumentRepository instrumentRepository, GenreRepository genreRepository,
            FileStorageService fileStorage, ListingRepository listingRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.instrumentRepository = instrumentRepository;
        this.genreRepository = genreRepository;
        this.fileStorage = fileStorage;
        this.listingRepository = listingRepository;
    }

    public Profile getByUserEmail(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Profile missing for user " + user.getId()));
    }

    public Profile getByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Profile missing for user " + userId));
    }

    public Profile getById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + id));
    }

    @Transactional
    public Profile update(Long profileId, String displayName, String bio, Country country, String city,
            SkillBadge badge, List<Long> instrumentIds, List<Long> genreIds) {
        Profile profile = getById(profileId);
        profile.setDisplayName(displayName);
        profile.setBio(bio);
        profile.setCountry(country);
        profile.setCity(city == null ? null : city.trim());
        profile.setSkillBadge(badge);

        Set<Instrument> instruments = instrumentIds == null ? Set.of()
                : new HashSet<>(instrumentRepository.findAllById(instrumentIds));

        Set<Genre> genres = genreIds == null ? Set.of()
                : new HashSet<>(genreRepository.findAllById(genreIds));

        profile.setInstruments(instruments);
        profile.setGenres(genres);

        profile.touch();
        return profile;
    }

    @Transactional
    public String uploadPicture(Long profileId, MultipartFile file) {
        Profile profile = getById(profileId);
        String url = fileStorage.storeProfilePicture(file);
        profile.setProfilePicPath(url);
        profile.touch();
        return url;
    }

    @Transactional
    public VideoLink addVideoLink(Long profileId, String url) {
        VideoPlatform platform = VideoPlatform.detect(url)
                .orElseThrow(() -> new IllegalArgumentException("URL must be a YouTube, Vimeo, or SoundCloud link"));

        Profile profile = getById(profileId);
        VideoLink link = new VideoLink(profile, url.trim(), platform);
        profile.getVideoLinks().add(link);
        profile.touch();
        return link;
    }

    @Transactional
    public void removeVideoLink(Long profileId, Long videoLinkId) {
        Profile profile = getById(profileId);
        profile.getVideoLinks().removeIf(v -> v.getId().equals(videoLinkId));
        profile.touch();
    }

    @Transactional
    public MusicLink addMusicLink(Long profileId, String url) {
        MusicPlatform platform = MusicPlatform.detect(url)
                .orElseThrow(
                        () -> new IllegalArgumentException("URL must be from a supported music distribution platform"));

        Profile profile = getById(profileId);
        MusicLink link = new MusicLink(profile, url.trim(), platform);
        profile.getMusicLinks().add(link);
        profile.touch();
        return link;
    }

    @Transactional
    public void removeMusicLink(Long profileId, Long muiscLinkId) {
        Profile profile = getById(profileId);
        profile.getMusicLinks().removeIf(v -> v.getId().equals(muiscLinkId));
        profile.touch();
    }

    @Transactional
    public void toggleHidden(Long profileId) {
        Profile profile = getById(profileId);
        profile.setHidden(!profile.isHidden());
        profile.touch();
    }

    @Transactional
    public void deleteAccount(Long userId) {
        List<Listing> listings = listingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        listingRepository.deleteAll(listings);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        user.setEnabled(false);
    }

    public List<Instrument> allInstruments() {
        return instrumentRepository.findAll();
    }

    public List<Genre> allGenres() {
        return genreRepository.findAll();
    }

}
