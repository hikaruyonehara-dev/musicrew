package co.sponto.musicrew.upload;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * Local disk implementation. Used in dev and any non-prod profile.
 */
@Service
@Profile("!prod")
public class LocalDiskFileStorageService implements FileStorageService {

    private static final Set<String> ALLOWED_TYPES =
        Set.of("image/jpeg", "image/png", "image/webp");
    private static final int MAX_DIMENSION = 1024;

    private final Path uploadDir;

    public LocalDiskFileStorageService(@Value("${musicrew.upload.dir}") String uploadDir) throws IOException {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath();
        Files.createDirectories(this.uploadDir);
    }

    @Override
    public String storeProfilePicture(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only JPEG, PNG, or WebP images are allowed");
        }

        String ext = switch (file.getContentType()) {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalStateException();
        };
        String filename = UUID.randomUUID() + ext;
        Path target = uploadDir.resolve(filename);

        try {
            Thumbnails.of(file.getInputStream())
                    .size(MAX_DIMENSION, MAX_DIMENSION)
                    .outputFormat(ext.equals(".webp") ? "webp" : ext.substring(1))
                    .toFile(target.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save upload: " + e.getMessage(), e);
        }
        return "/uploads/" + filename;
    }
}
