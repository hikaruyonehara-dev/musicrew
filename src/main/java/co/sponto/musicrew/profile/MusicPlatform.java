package co.sponto.musicrew.profile;

import java.net.URI;
import java.util.Optional;

public enum MusicPlatform {
    TUNECORE,
    DISTROKID,
    CDBABY,
    UNITEDMASTERS,
    ANONYXGHOST;

    public static Optional<MusicPlatform> detect(String url) {
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

        if (host.equals("tunecore.com") || host.equals("www.tunecore.com") || host.equals("tunecore.co.jp")) {
            return Optional.of(TUNECORE);
        }

        if (host.equals("distrokid.com") || host.equals("www.distrokid.com") || host.equals("distrokid.com/vip")) {
            return Optional.of(DISTROKID);
        }

        if (host.equals("cdbaby.com") || host.equals("www.cdbaby.com") || host.equals("members.cdbaby.com")) {
            return Optional.of(CDBABY);
        }

        if (host.equals("unitedmasters.com") || host.equals("www.unitedmasters.com")) {
            return Optional.of(UNITEDMASTERS);
        }

        if (host.equals("anonyxghost.com") || host.equals("www.anonyxghost.com")) {
            return Optional.of(ANONYXGHOST);
        }

        return Optional.empty();
    }
}
