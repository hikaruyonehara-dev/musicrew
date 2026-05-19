package co.sponto.musicrew.listing;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import co.sponto.musicrew.profile.Country;
import co.sponto.musicrew.profile.Genre;
import co.sponto.musicrew.profile.Instrument;
import co.sponto.musicrew.user.User;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "listing_instruments", joinColumns = @JoinColumn(name = "listing_id"), inverseJoinColumns = @JoinColumn(name = "instrument_id"))
    private Set<Instrument> instrumentsNeeded = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "listing_genres", joinColumns = @JoinColumn(name = "listing_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Country country;

    private String city;

    @Column(nullable = false)
    private boolean active = true;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant updatedAt;

    public Listing(User user, String title, String description) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

}
