# Musicrew

> A web app for musicians to find bandmates — built end-to-end as a solo SDLC exercise.

Musicrew lets musicians create a profile (instruments, genres, location, performance videos, music distribution links), discover other musicians by filter, and chat in 1:1 conversations. Built from a formal Requirements → Design → Implementation → Test → Deploy cycle.

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
| Security | Spring Security 6 (form login, BCrypt, CSRF) |
| Views | Thymeleaf with fragment-based layout |
| Styling | Tailwind CSS via Play CDN, Inter font, Resend-style dark theme |
| Build | Maven, fat-JAR via `spring-boot-maven-plugin` |
| Image processing | Thumbnailator (server-side resize to 1024×1024) |
| File storage | Local disk (`LocalDiskFileStorageService`), S3-ready (`S3FileStorageService` via AWS SDK v2) |
| Testing | JUnit 5, AssertJ, MockMvc, `@DataJpaTest`, `spring-security-test` |
| Deployment | EC2 (Ubuntu 24.04) + `systemd` |

---

## Features

### Authentication
- Email + password signup with 18+ age gate
- BCrypt password hashing
- Session-based login, CSRF protection, secure POST logout
- Skill-level selection at signup (Beginner / Intermediate / Advanced / Pro)

### Profile
- Display name, bio, country (ISO 3166), city, skill badge
- Multi-select instruments and genres from curated lists
- Profile picture upload with server-side resize and content-type whitelisting
- Multiple performance video links (YouTube / Vimeo / SoundCloud whitelist)
- Multiple music distribution links (TuneCore / DistroKid / CDBaby / UnitedMasters)
- Hidden from search until profile has picture + ≥1 performance video

### Search
- Dynamic filters via JPA Specifications (composable, null-safe)
- Multi-select checkboxes in collapsible accordions
- Filter by instrument, genre, country, city
- Self-exclusion (you never appear in your own search results)
- Bookmarkable URL with filter state preserved

### Messaging
- 1:1 conversations with canonical user ordering (no duplicates per pair)
- Inbox sorted by recency, unread badges, Instagram-style relative timestamps
- Avatar + display name header, click-through to profile
- Multi-line message input
- Auto mark-as-read on view, read/unread tracking via `readAt` timestamp

---

## Architecture

Standard Spring layered architecture, package-by-feature:


Data model: 10 tables (User, Profile, Instrument, Genre, VideoLink, MusicLink, Conversation, Message + 2 M:N join tables). 1:1 between User and Profile keeps auth concerns separate from public profile data.

---

## Running locally

Prereqs: Java 21, Maven 3.8+.

```bash
git clone https://github.com/<your-username>/musicrew.git
cd musicrew
mvn spring-boot:run

