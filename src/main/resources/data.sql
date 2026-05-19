-- Seed curated lists. MERGE = upsert: if a row with the same KEY already
-- exists, do nothing. Safe to re-run on every startup.
MERGE INTO instrument (id, name) KEY (name)
VALUES
    (1, 'Guitar'),
    (2, 'Bass'),
    (3, 'Drums'),
    (4, 'Vocals'),
    (5, 'Keyboard/Piano'),
    (6, 'Synthesizer'),
    (7, 'Violin'),
    (8, 'Viola'),
    (9, 'Cello'),
    (10, 'Double Bass'),
    (11, 'Saxophone'),
    (12, 'Trumpet'),
    (13, 'Trombone'),
    (14, 'Flute'),
    (15, 'Clarinet'),
    (16, 'Harmonica'),
    (17, 'DJ/Turntables'),
    (18, 'Percussion'),
    (19, 'Banjo'),
    (20, 'Mandolin'),
    (21, 'Harp'),
    (22, 'Producer');

MERGE INTO genre (id, name) KEY (name)
VALUES
    (1, 'Rock'),
    (2, 'Pop'),
    (3, 'Jazz'),
    (4, 'Blues'),
    (5, 'Funk'),
    (6, 'Soul'),
    (7, 'R&B'),
    (8, 'Hip-Hop'),
    (9, 'Electronic'),
    (10, 'Metal'),
    (11, 'Punk'),
    (12, 'Folk'),
    (13, 'Country'),
    (14, 'Classical'),
    (15, 'Reggae'),
    (16, 'Latin'),
    (17, 'World'),
    (18, 'Experimental');

-- Seed admin user. Password: "Password"  (change in prod!)
-- Hash is BCrypt cost-10. Spring Security accepts $2a/$2b/$2y prefixes interchangeably.
-- INSERT ... WHERE NOT EXISTS is idempotent against both fresh and populated DBs.
INSERT INTO users (email, password_hash, date_of_birth, created_at, enabled, role)
SELECT 'admin@test.com',
       '$2b$10$s0muS.0QDWUNBwkJNANsFueMHG.AYJiy277nJWCFFP9KmBwVXtjqS',
       '1990-01-01',
       CURRENT_TIMESTAMP,
       TRUE,
       'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@test.com');

-- Seed admin's profile (one Profile per User is required).
-- Look up the admin's id by email so we don't depend on a fixed value.
INSERT INTO profile (user_id, display_name, skill_badge, hidden, created_at, updated_at)
SELECT u.id, 'Admin', 'PRO', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u
WHERE u.email = 'admin@test.com'
  AND NOT EXISTS (SELECT 1 FROM profile WHERE user_id = u.id);