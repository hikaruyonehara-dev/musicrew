package co.sponto.musicrew.block;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserRepository;

@Service
public class BlockService {

    private final BlockRepository blockRepository;
    private final UserRepository userRepository;

    public BlockService(BlockRepository blockRepository, UserRepository userRepository) {
        this.blockRepository = blockRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void block(User blocker, Long blockedId) {
        if (blocker.getId().equals(blockedId)) {
            throw new IllegalArgumentException("You can't block yourself");
        }

        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + blockedId));

        if (blockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            return;
        }
        blockRepository.save(new Block(blocker, blocked));
    }

    @Transactional
    public void unblock(User blocker, Long blockedUserId) {
        User blocked = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + blockedUserId));

        blockRepository.findByBlockerAndBlocked(blocker, blocked)
                .ifPresent(blockRepository::delete);
    }

    public boolean isBlockedBetween(Long userIdA, Long userIdB) {
        if (userIdA == null || userIdB == null) {
            return false;
        }

        return blockRepository.existsBetween(userIdA, userIdB);
    }

    public List<Block> myBlocks(User blocker) {
        return blockRepository.findByBlocker(blocker);
    }

}
