package co.sponto.musicrew.block;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.sponto.musicrew.user.User;

public interface BlockRepository extends JpaRepository<Block, Long> {

    Optional<Block> findByBlockerAndBlocked(User blocker, User blocked);

    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    List<Block> findByBlocker(User blocker);

    @org.springframework.data.jpa.repository.Query("""
            SELECT COUNT (b) > 0 FROM Block b
            WHERE (b.blocker.id = :a AND b.blocked.id = :b) OR (b.blocker.id = :b AND b.blocked.id = :a)
            """)
    boolean existsBetween(Long a, Long b);

    @org.springframework.data.jpa.repository.Query("""
            SELECT CASE WHEN b.blocker.id = :me THEN b.blocked.id ELSE b.blocker.id END
            FROM Block b WHERE b.blocker.id = :me OR b.blocked.id = :me
                """)
    java.util.Set<Long> findUserIdsBlockedBetween(Long me);

}
