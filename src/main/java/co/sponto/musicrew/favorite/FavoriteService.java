package co.sponto.musicrew.favorite;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserRepository;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void favorite(User favoriter, Long favoritedUserId) {
        if (favoriter.getId().equals(favoritedUserId)) {
            throw new IllegalArgumentException("Cannot favorite yourself");
        }

        User favorited = userRepository.findById(favoritedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + favoritedUserId));

        if (favoriteRepository.existsByFavoriterAndFavorited(favoriter, favorited)) {
            return;
        }

        favoriteRepository.save(new Favorite(favoriter, favorited));
    }

    @Transactional
    public void unfavorite(User favoriter, Long favoritedId) {
        if (favoriter.getId().equals(favoritedId)) {
            throw new IllegalArgumentException("Cannot unfavorite yourself");
        }

        User favorited = userRepository.findById(favoritedId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + favoritedId));

        favoriteRepository.findByFavoriterAndFavorited(favoriter, favorited)
                .ifPresent(favoriteRepository::delete);
    }

    public boolean isFavorited(User favoriter, Long favoritedUserId) {
        return userRepository.findById(favoriter.getId())
                .map(favorited -> favoriteRepository.existsByFavoriterAndFavorited(favoriter, favorited))
                .orElse(false);
    }

    public List<Favorite> myFavorites(User favoriter) {
        return favoriteRepository.findByFavoriterOrderByCreatedAtDesc(favoriter);
    }

    public Set<Long> favoritedUserIds(User favoriter) {
        if (favoriter == null || favoriter.getId() == null) {
            return Set.of();
        }

        return favoriteRepository.findFavoritedUserIdsByFavoriter(favoriter.getId());
    }
}
