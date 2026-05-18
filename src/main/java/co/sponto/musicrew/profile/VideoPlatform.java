package co.sponto.musicrew.profile;

import java.net.URI;
import java.util.Optional;

public enum VideoPlatform {
    YOUTUBE,
    VIMEO,
    SOUNDCLOUD;

    public static Optional<VideoPlatform> detect(String url) {

        if (url == null || url.isBlank()) {
            return Optional.empty();
        }

        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        String host = uri.getHost();
        if (host == null) {
            return Optional.empty();
        }
        host = host.toLowerCase();

        if (host.equals("youtube.com") || host.equals("www.youtube.com") || host.equals("youtu.be")
                || host.equals("m.youtube.com")) {
            return Optional.of(YOUTUBE);
        }

        if (host.equals("vimeo.com") || host.equals("www.vimeo.com")) {
            return Optional.of(VIMEO);
        }

        if (host.equals("soundcloud.com") || host.equals("www.soundcloud.com") || host.equals("m.soundcloud.com")) {
            return Optional.of(SOUNDCLOUD);
        }

        return Optional.empty();
    }
}
