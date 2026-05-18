# Musicrew — Requirements

Musicrew is a free, global web app for individual musicians to find bandmates.
Long-term vision also covers gigs and session work, but those are explicitly out of MVP scope.

**Last updated:** 2026-04-23
**Current SDLC phase:** Analysis complete, moving to Design.

---

## Tech stack

| Layer | Choice |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.x (latest stable) |
| Web / views | Thymeleaf + Bootstrap (mobile-responsive, server-rendered) |
| Database (dev) | H2 in-memory |
| Database (prod, future) | PostgreSQL on AWS RDS |
| ORM | Spring Data JPA / Hibernate |
| Auth | Spring Security, email/password, BCrypt |
| File storage (dev) | Local filesystem |
| File storage (prod, future) | AWS S3 |
| Deployment target | AWS (specific service TBD in Design phase) |
| i18n | Spring MessageSource, English first; Japanese later |

Database-portability rule: only JPA/Hibernate, no H2-specific SQL features.

---

## MVP scope

### In scope
- User registration, email/password login, 18+ age gate
- Musician profile CRUD
- Search/filter musicians by instrument, genre, location
- In-app 1:1 text messaging (polling refresh, not WebSockets)
- English UI
- Mobile-responsive UI

### Out of scope (explicitly deferred)
- Posted listings ("drummer wanted" ads) → Phase 1.5
- Gigs feature → Phase 2
- Session work feature → Phase 2
- Group chats, real-time chat, attachments
- OAuth / social login (Google, Spotify, etc.)
- Payments / monetization
- Verification of pro status
- Japanese UI (structure ready, translation added later)

---

## Profile fields

| Field | Type | Notes |
|---|---|---|
| Display name | string | Public username |
| Email | string | Private: login + notifications only, never shown publicly |
| Password | string | BCrypt hashed |
| Date of birth | date | 18+ required at signup |
| Instruments | list | Multi-select from curated list |
| Genres | list | Multi-select from curated list |
| Country | enum | Dropdown |
| City | string | Free text |
| Pro/Amateur | enum | Self-declared, no verification |
| Bio | text | Free text, ~500 char cap |
| Profile picture | image | One image, **required for search visibility** |
| Performance video links | list of URLs | Multiple allowed, **at least one required for search visibility**. URL whitelist: YouTube, Vimeo, SoundCloud |

### Profile visibility rule
Signup is frictionless — users can register without a pic or video.
A profile is **hidden from search results** until it has:
1. A profile picture, AND
2. At least one performance video link

This encourages profile completion without blocking signup.

---

## Discovery (MVP)

- Search page with filters: instrument, genre, country, city
- Results rendered as a grid of profile cards (photo, name, instruments, location)
- Clicking a card opens the full profile detail page
- Profile detail page has a "Message" button → opens a new conversation

---

## Messaging (MVP)

- 1:1 only
- Text only, no attachments
- Inbox view lists all conversations
- Conversation view shows messages in chronological order
- Refresh-based; no WebSockets
- Basic read / unread state

---

## Non-functional requirements

- Mobile-responsive via Bootstrap
- Small expected scale initially (dozens of users)
- English UI for MVP, i18n infrastructure in place from day one
- 18+ age minimum, enforced at signup
- Passwords stored as BCrypt hashes
- Email addresses never exposed publicly

---

## User stories (MVP — Must-have only)

### Epic 1: Account & Auth
- **A1** As a visitor, I can create an account with email, password, and date of birth.
- **A2** As a visitor under 18, I am blocked from creating an account.
- **A3** As a registered user, I can log in with email + password.
- **A4** As a logged-in user, I can log out.

### Epic 2: Profile
- **P1** I can fill in display name, instruments, genres, country, city, pro/amateur badge, and bio.
- **P2** I can upload a profile picture.
- **P3** I can add one or more performance video links (YouTube/Vimeo/SoundCloud whitelist).
- **P4** I can edit any field on my profile.
- **P5** I can view my own profile as others would see it.
- **P6** I can view another user's public profile.
- **P7** My profile is hidden from search until it has a picture AND at least one video link.

### Epic 3: Discovery
- **D1** I can open a search page and see a grid of visible musician profiles.
- **D2** I can filter by instrument (multi), genre (multi), country, and city.
- **D3** I can click a profile card to open its detail page.

### Epic 4: Messaging
- **M1** From another user's profile, I can click "Message" to start a conversation.
- **M2** I can see my inbox — conversations sorted by most recent.
- **M3** I can open a conversation and send/receive text messages.
- **M4** I can see which conversations have unread messages.
- **M5** I cannot message myself.

### Post-MVP (deferred)
Password reset, pagination, delete account, block/report user, change email/password, email verification, Japanese i18n, posted listings, gigs, session work, real-time chat, S3 storage, PostgreSQL migration.
