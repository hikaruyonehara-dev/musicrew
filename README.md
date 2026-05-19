# Musicrew

> A web app for musicians to find bandmates — built end-to-end as a solo SDLC exercise.

Musicrew lets musicians create a profile (instruments, genres, location, performance videos, music distribution links), browse an Instagram-style feed of active band-searches, discover others by filter, and chat in 1:1 conversations. Account management (hide / delete) and a moderator-led block/report system round out the social layer. Continuous deploy via GitHub Actions to a single Ubuntu EC2 instance.

Built from a formal Requirements → Design → Implementation → Test → Deploy cycle.

**デモ:** `http://54.250.174.66:8080`
**USER:** `test1@test.com`
**PASSWORD:** `Password`

---

## Tech stack

| Layer | Choice |
|---|---|
| Language / runtime | Java 21 (LTS) |
| Framework | Spring Boot 3.3 |
| Persistence | Spring Data JPA + Hibernate, H2 (dev), PostgreSQL-ready (prod) |
| Security | Spring Security 6 — form login, BCrypt, CSRF, role-based access (`USER` / `ADMIN`) |
| Views | Thymeleaf with fragment-based layout |
| Styling | Tailwind CSS via Play CDN, Inter font, Resend-style dark theme |
| Build | Maven, fat-JAR via `spring-boot-maven-plugin` |
| Image processing | Thumbnailator (server-side resize to 1024×1024) |
| File storage | Local disk (`LocalDiskFileStorageService`), S3-ready (`S3FileStorageService` via AWS SDK v2) |
| Testing | JUnit 5, AssertJ, MockMvc, `@DataJpaTest`, `spring-security-test` |
| CI / CD | GitHub Actions — build + scp + `systemctl restart` on push to `main` |
| Deployment | EC2 (Ubuntu 24.04) + `systemd` service |

---

## Features

### Authentication
- Email + password signup with 18+ age gate
- BCrypt password hashing
- Session-based login, CSRF protection, secure POST logout
- Skill-level selection at signup (Beginner / Intermediate / Advanced / Pro)
- Role-based access (`USER`, `ADMIN`) granted by Spring Security authorities

### Profile
- Display name, bio, country (ISO 3166), city, skill badge
- Multi-select instruments and genres from curated lists
- Profile picture upload with server-side resize and content-type whitelisting
- Multiple performance video links (YouTube / Vimeo / SoundCloud whitelist)
- Multiple music distribution links (TuneCore / DistroKid / CDBaby / UnitedMasters)
- Hidden from public discovery until profile has picture + ≥1 performance video

### Listings (home feed)
- Instagram-style feed of active band-searches at `/home`
- Each card shows poster avatar, display name, title, instruments needed, location
- Post / edit / close / delete your own listings
- Filterable by instrument, genre, country
- Click-through to detail page with one-tap reply via direct message

### Search
- Dynamic filters via JPA Specifications (composable, null-safe)
- Multi-select checkboxes in collapsible accordions
- Filter by instrument, genre, country, city
- Self-exclusion and block-aware (you and blocked users never see each other)
- Bookmarkable URL with filter state preserved

### Messaging
- 1:1 conversations with canonical user ordering (no duplicate threads per pair)
- Inbox sorted by recency, unread badges, hour:minute timestamps
- Avatar + display name header, click-through to profile
- Multi-line message input
- Auto mark-as-read on view, read/unread tracking via `readAt` timestamp
- Delete conversation (clears history for both participants)
- Send disabled and banner shown when a block exists in either direction

### Account management
- Toggle profile visibility (hide / unhide) — reversible from settings
- Soft-delete account: disables login, hard-deletes own listings, leaves prior messages visible to the other participant
- Custom 404 page (themed to match the rest of the app) for missing / hidden / blocked / deleted profiles and listings — same surface for all "not available" cases, no enumeration leaks

### Moderation
- **Block users** (symmetric): each disappears from the other's search, feed, profile pages, listings, and messaging
- Dedicated `/profile/me/blocks` page to review and unblock
- **Report users** with five reason categories (Spam, Harassment, Inappropriate content, Impersonation, Other) and optional free-text description
- Admin queue at `/admin/reports` with Open / Reviewed / Dismissed tabs
- Admin nav link visible only to `ROLE_ADMIN` accounts
- Audit trail (reviewer email + timestamp) recorded on every status change

---

## Architecture

Standard Spring layered architecture, package-by-feature:

```
co.sponto.musicrew
├── admin       — admin moderation controller (reports queue)
├── block       — Block entity, repository, service, controller
├── config      — SecurityConfig (role-based access), WebConfig
├── listing     — Listing entity + feed + CRUD
├── messaging   — Conversation, Message, threaded inbox
├── profile     — Profile, Instrument, Genre, VideoLink, MusicLink
├── report      — Report entity, reason / status enums, controller
├── search      — SearchService + ProfileSpecs (composable JPA Specifications)
├── upload      — FileStorageService (local + S3 implementations)
└── user        — User, AppUserDetailsService, AuthController, Role enum
```

Data model: 13 tables — `User`, `Profile`, `Instrument`, `Genre`, `VideoLink`, `MusicLink`, `Conversation`, `Message`, `Listing`, `Block`, `Report` plus M:N join tables. The 1:1 between `User` and `Profile` keeps auth concerns separate from public profile data.

**JPA Specifications** power composable, null-safe filtering across search and listings — blocks, hidden profiles, and disabled accounts are enforced at the query level, so excluded users never make it into the result set.

**Symmetric blocking** is modeled as a single directed `Block` row whose visibility check (`isBlockedBetween(a, b)`) covers both `(a → b)` and `(b → a)` via a JPQL OR, simplifying the data shape while preserving symmetric semantics.

---

## Running locally

Prereqs: Java 21, Maven 3.8+.

```bash
git clone https://github.com/hikaruyonehara-dev/musicrew.git
cd musicrew
mvn spring-boot:run
```

The app starts on `http://localhost:8080` with an H2 file-based DB at `./data/musicrew.mv.db`. The H2 web console is available at `/h2-console`. Seed data (instruments, genres, admin account) is loaded from `src/main/resources/data.sql` on first run.

To wipe local data: `rm -rf data/` and restart.

**Default admin account (seeded):** `admin@test.com` / `Password`

---

## Deployment

`main` is the deploy branch. Every push to `main` triggers the workflow at `.github/workflows/deploy.yml`, which:

1. Builds the fat JAR with Maven
2. SCPs it to the EC2 instance over SSH (uses `secrets.EC2_SSH_KEY`, `EC2_USER`, `EC2_HOST`)
3. Restarts the `musicrew` `systemd` service on the box

Production uses the `prod` Spring profile (`application-prod.properties`), backed by PostgreSQL and S3-compatible storage. See `S3FileStorageService` for the storage-abstraction pattern that swaps implementations based on the active profile.

---

## License

MIT — see [LICENSE](LICENSE).
