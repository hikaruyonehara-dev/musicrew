# Musicrew — Design

**Last updated:** 2026-04-23
**SDLC phase:** Design frozen, moving to Implementation.

---

## 1. Architecture

Classic Spring layered app.

```
Browser
  ↓
Controller layer (@Controller)          HTTP handling, picks Thymeleaf template
  ↓
Service layer (@Service)                 Business rules
  ↓
Repository layer (Spring Data JPA)       CRUD
  ↓
H2 (dev) / PostgreSQL (prod, future)
```

Cross-cutting: Spring Security filter chain in front of controllers. Thymeleaf templates server-side. Static assets (Bootstrap) in `resources/static`. Uploaded pics to local `./uploads/` (S3 later).

---

## 2. Data model

### Entities

**User** — the login account
- `id` PK
- `email` unique
- `passwordHash` (BCrypt)
- `dateOfBirth`
- `createdAt`
- `enabled` (soft disable flag)

**Profile** — public-facing info (1:1 with User)
- `id` PK
- `userId` FK → User
- `displayName` (not unique)
- `bio` (max 500 chars)
- `country` enum (ISO 3166-1 alpha-2)
- `city` free text
- `skillBadge` enum (PRO / AMATEUR)
- `profilePicPath` nullable
- `createdAt`, `updatedAt`

**Instrument** — curated list, M:N with Profile
**Genre** — curated list, M:N with Profile

**VideoLink** — N:1 with Profile
- `id`, `profileId`, `url`, `platform` (YOUTUBE / VIMEO / SOUNDCLOUD)

**Conversation** — 1:1 between two users
- `id` PK
- `userAId` FK (always the smaller user id)
- `userBId` FK (always the larger user id)
- `createdAt`
- Unique constraint on (userAId, userBId)

**Message** — N:1 with Conversation
- `id`, `conversationId`, `senderId`, `body`, `sentAt`, `readAt` (nullable)

### Profile visibility rule
A profile is hidden from search results until it has:
1. `profilePicPath != null`, AND
2. At least one VideoLink

### Seed data

**Instruments (22):** Guitar, Bass, Drums, Vocals, Keyboard/Piano, Synthesizer, Violin, Viola, Cello, Double Bass, Saxophone, Trumpet, Trombone, Flute, Clarinet, Harmonica, DJ/Turntables, Percussion, Banjo, Mandolin, Harp, Producer

**Genres (18):** Rock, Pop, Jazz, Blues, Funk, Soul, R&B, Hip-Hop, Electronic, Metal, Punk, Folk, Country, Classical, Reggae, Latin, World, Experimental

**Countries:** ISO 3166-1 alpha-2 list (loaded at startup or as a Java enum)

### Constraints & rules

- **Profile pic upload:** 5MB max; JPEG/PNG/WebP only; auto-resize to max 1024×1024 on upload
- **Video link validation:** hostname must match one of `youtube.com`, `youtu.be`, `vimeo.com`, `soundcloud.com`
- **Age gate:** must be 18+ at signup (calculated from DOB)
- **Passwords:** BCrypt hashed
- **Emails:** never displayed publicly

---

## 3. URL routes

### Public
| Method | Path | Purpose |
|---|---|---|
| GET | `/` | Landing page |
| GET | `/signup` | Signup form |
| POST | `/signup` | Submit signup |
| GET | `/login` | Login form |
| POST | `/login` | Submit login (Spring Security) |
| POST | `/logout` | Log out (Spring Security) |

### Authenticated
| Method | Path | Purpose |
|---|---|---|
| GET | `/home` | Authed home |
| GET | `/profile/me` | My profile |
| GET | `/profile/me/edit` | Edit form |
| POST | `/profile/me/edit` | Save edits |
| POST | `/profile/me/picture` | Upload pic |
| POST | `/profile/me/videos` | Add video link |
| POST | `/profile/me/videos/{id}/delete` | Remove video link |
| GET | `/profile/{id}` | View another's profile |
| GET | `/search` | Search page + results |
| GET | `/messages` | Inbox |
| GET | `/messages/{conversationId}` | Conversation view |
| POST | `/messages/{conversationId}` | Send message |
| POST | `/messages/start/{userId}` | Start new conversation |

---

## 4. Project structure

Maven, base package `co.sponto.musicrew`, **package-by-feature** layout.

```
musicrew/
├── pom.xml
├── REQUIREMENTS.md
├── DESIGN.md
├── .gitignore
├── uploads/                              (gitignored)
└── src/
    ├── main/
    │   ├── java/co/sponto/musicrew/
    │   │   ├── MusicrewApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   └── WebConfig.java
    │   │   ├── user/
    │   │   │   ├── User.java
    │   │   │   ├── UserRepository.java
    │   │   │   ├── UserService.java
    │   │   │   └── AuthController.java
    │   │   ├── profile/
    │   │   │   ├── Profile.java
    │   │   │   ├── Instrument.java
    │   │   │   ├── Genre.java
    │   │   │   ├── VideoLink.java
    │   │   │   ├── Country.java
    │   │   │   ├── SkillBadge.java
    │   │   │   ├── ProfileRepository.java
    │   │   │   ├── ProfileService.java
    │   │   │   └── ProfileController.java
    │   │   ├── search/
    │   │   │   ├── SearchService.java
    │   │   │   └── SearchController.java
    │   │   ├── messaging/
    │   │   │   ├── Conversation.java
    │   │   │   ├── Message.java
    │   │   │   ├── ConversationRepository.java
    │   │   │   ├── MessageRepository.java
    │   │   │   ├── MessagingService.java
    │   │   │   └── MessagingController.java
    │   │   └── upload/
    │   │       └── FileStorageService.java
    │   └── resources/
    │       ├── application.properties
    │       ├── data.sql
    │       ├── messages.properties
    │       ├── static/css/
    │       ├── static/js/
    │       └── templates/
    │           ├── layout.html
    │           ├── index.html
    │           ├── auth/{signup,login}.html
    │           ├── profile/{view,edit}.html
    │           ├── search/search.html
    │           └── messaging/{inbox,conversation}.html
    └── test/java/co/sponto/musicrew/     (JUnit 5)
```

### Build tool
Maven (more common in Spring tutorials, easier to find beginner docs).

### Key dependencies (pom.xml)
- `spring-boot-starter-web`
- `spring-boot-starter-thymeleaf`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `com.h2database:h2` (dev/runtime)
- `spring-boot-devtools` (dev convenience)
- `spring-boot-starter-test` (test)
- Image resizing: `net.coobird:thumbnailator` (small, focused, no transitive bloat)
