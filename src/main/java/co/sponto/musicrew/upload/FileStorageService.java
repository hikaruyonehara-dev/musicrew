package co.sponto.musicrew.upload;

import org.springframework.web.multipart.MultipartFile;

/**
 * Storage abstraction for profile pictures.
 * Implementations: LocalDiskFileStorageService (dev), S3FileStorageService (prod).
 * Spring picks one based on the active profile.
 */
public interface FileStorageService {

    /**
     * Validate, resize, and persist the uploaded file.
     * @return the URL to use as the profile's profilePicPath.
     */
    String storeProfilePicture(MultipartFile file);
}
