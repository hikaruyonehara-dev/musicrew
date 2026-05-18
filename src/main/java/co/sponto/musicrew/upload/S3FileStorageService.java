package co.sponto.musicrew.upload;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * S3 implementation. Used in prod.
 * Credentials come from the AWS SDK's default chain (instance role, env vars, etc.).
 */
@Service
@Profile("prod")
public class S3FileStorageService implements FileStorageService {

    private static final Set<String> ALLOWED_TYPES =
        Set.of("image/jpeg", "image/png", "image/webp");
    private static final int MAX_DIMENSION = 1024;

    private final S3Client s3;
    private final String bucket;
    private final String region;

    public S3FileStorageService(@Value("${aws.s3.bucket}") String bucket,
                                @Value("${aws.s3.region}") String region) {
        this.bucket = bucket;
        this.region = region;
        this.s3 = S3Client.builder()
                .region(Region.of(region))
                .build();
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
        String key = UUID.randomUUID() + ext;

        // Resize into a byte buffer (rather than writing to disk).
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Thumbnails.of(file.getInputStream())
                    .size(MAX_DIMENSION, MAX_DIMENSION)
                    .outputFormat(ext.equals(".webp") ? "webp" : ext.substring(1))
                    .toOutputStream(out);
        } catch (IOException e) {
            throw new RuntimeException("Failed to resize: " + e.getMessage(), e);
        }
        byte[] bytes = out.toByteArray();

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();
        s3.putObject(req, RequestBody.fromBytes(bytes));

        // Public virtual-hosted URL — works when the bucket / object is publicly readable.
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}
