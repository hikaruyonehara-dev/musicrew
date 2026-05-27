package co.sponto.musicrew.favorite;

import org.springframework.data.jpa.repository.JpaRepository;

import co.sponto.musicrew.user.User;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByFavoriterAndFavorited(User favoriter, User favorited);

    boolean existsByFavoriterAndFavorited(User favoriter, User favorited);

    List<Favorite> findByFavoriterOrderByCreatedAtDesc(User favoriter);

    @org.springframework.data.jpa.repository.Query("""
            SELECT f.favorited.id FROM Favorite f
            WHERE f.favoriter.id = :userId
            """)
    java.util.Set<Long> findFavoritedUserIdsByFavoriter(Long userId);
}
