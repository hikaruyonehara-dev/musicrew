-- Seed curated lists. Cross-compatible upsert: INSERT ... SELECT ... WHERE NOT EXISTS
-- with a VALUES table expression. Works in both H2 (dev) and PostgreSQL (prod).
INSERT INTO
    instrument (name)
SELECT
    t.name
FROM
    (
        VALUES
            ('Guitar'),
            ('Bass'),
            ('Drums'),
            ('Vocals'),
            ('Keyboard/Piano'),
            ('Synthesizer'),
            ('Violin'),
            ('Viola'),
            ('Cello'),
            ('Double Bass'),
            ('Saxophone'),
            ('Trumpet'),
            ('Trombone'),
            ('Flute'),
            ('Clarinet'),
            ('Harmonica'),
            ('DJ/Turntables'),
            ('Percussion'),
            ('Banjo'),
            ('Mandolin'),
            ('Harp'),
            ('Producer')
    ) AS t (name)
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            instrument i
        WHERE
            i.name = t.name
    );

INSERT INTO
    genre (name)
SELECT
    t.name
FROM
    (
        VALUES
            ('Rock'),
            ('Pop'),
            ('Jazz'),
            ('Blues'),
            ('Funk'),
            ('Soul'),
            ('R&B'),
            ('Hip-Hop'),
            ('Electronic'),
            ('Metal'),
            ('Punk'),
            ('Folk'),
            ('Country'),
            ('Classical'),
            ('Reggae'),
            ('Latin'),
            ('World'),
            ('Experimental')
    ) AS t (name)
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            genre g
        WHERE
            g.name = t.name
    );

-- Seed admin user. Password: "Password"  (change in prod!)
-- Hash is BCrypt cost-10. Spring Security accepts $2a/$2b/$2y prefixes interchangeably.
-- INSERT ... WHERE NOT EXISTS is idempotent against both fresh and populated DBs.
INSERT INTO
    users (
        email,
        password_hash,
        date_of_birth,
        created_at,
        enabled,
        role
    )
SELECT
    'admin@test.com',
    '$2b$10$s0muS.0QDWUNBwkJNANsFueMHG.AYJiy277nJWCFFP9KmBwVXtjqS',
    '1990-01-01',
    CURRENT_TIMESTAMP,
    TRUE,
    'ADMIN'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            users
        WHERE
            email = 'admin@test.com'
    );

-- Seed admin's profile (one Profile per User is required).
-- Look up the admin's id by email so we don't depend on a fixed value.
INSERT INTO
    profile (
        user_id,
        display_name,
        skill_badge,
        hidden,
        created_at,
        updated_at
    )
SELECT
    u.id,
    'Admin',
    'PRO',
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.email = 'admin@test.com'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile
        WHERE
            user_id = u.id
    );

-- =====================================================================
-- Three seed portfolio users. All passwords = "Password".
-- Each has a profile picture (ui-avatars.com placeholder) and one video
-- link so they pass the isVisible() filter and appear in /home and /search.
-- All inserts idempotent (INSERT ... SELECT WHERE NOT EXISTS).
-- =====================================================================
-- ---------- Aki Tanaka — vocalist in Tokyo ----------
INSERT INTO
    users (
        email,
        password_hash,
        date_of_birth,
        created_at,
        enabled,
        role
    )
SELECT
    'aki@musicrew.test',
    '$2b$10$s0muS.0QDWUNBwkJNANsFueMHG.AYJiy277nJWCFFP9KmBwVXtjqS',
    '1996-03-15',
    CURRENT_TIMESTAMP,
    TRUE,
    'USER'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            users
        WHERE
            email = 'aki@musicrew.test'
    );

INSERT INTO
    profile (
        user_id,
        display_name,
        bio,
        country,
        city,
        skill_badge,
        profile_pic_path,
        hidden,
        created_at,
        updated_at
    )
SELECT
    u.id,
    'Aki Tanaka',
    'Soul vocalist in Tokyo. Looking for a band. Open to collaborations on originals.',
    'JP',
    'Tokyo',
    'INTERMEDIATE',
    'https://ui-avatars.com/api/?name=Aki+Tanaka&size=512&background=4a5568&color=fff',
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.email = 'aki@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile
        WHERE
            user_id = u.id
    );

INSERT INTO
    profile_instruments (profile_id, instrument_id)
SELECT
    p.id,
    i.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN instrument i ON i.name = 'Vocals'
WHERE
    u.email = 'aki@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_instruments pi
        WHERE
            pi.profile_id = p.id
            AND pi.instrument_id = i.id
    );

INSERT INTO
    profile_genre (profile_id, genre_id)
SELECT
    p.id,
    g.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN genre g ON g.name = 'Soul'
WHERE
    u.email = 'aki@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_genre pg
        WHERE
            pg.profile_id = p.id
            AND pg.genre_id = g.id
    );

INSERT INTO
    video_link (profile_id, url, platform)
SELECT
    p.id,
    'https://www.youtube.com/watch?v=hLQl3WQQoQ0',
    'YOUTUBE'
FROM
    profile p
    JOIN users u ON p.user_id = u.id
WHERE
    u.email = 'aki@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            video_link
        WHERE
            profile_id = p.id
    );

-- ---------- Sara Kim — guitarist in Los Angeles ----------
INSERT INTO
    users (
        email,
        password_hash,
        date_of_birth,
        created_at,
        enabled,
        role
    )
SELECT
    'sara@musicrew.test',
    '$2b$10$s0muS.0QDWUNBwkJNANsFueMHG.AYJiy277nJWCFFP9KmBwVXtjqS',
    '1993-08-22',
    CURRENT_TIMESTAMP,
    TRUE,
    'USER'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            users
        WHERE
            email = 'sara@musicrew.test'
    );

INSERT INTO
    profile (
        user_id,
        display_name,
        bio,
        country,
        city,
        skill_badge,
        profile_pic_path,
        hidden,
        created_at,
        updated_at
    )
SELECT
    u.id,
    'Sara Kim',
    'Indie rock guitarist in LA. 8 years gigging. Influences: boygenius, Phoebe Bridgers.',
    'US',
    'Los Angeles',
    'ADVANCED',
    'https://ui-avatars.com/api/?name=Sara+Kim&size=512&background=4a5568&color=fff',
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.email = 'sara@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile
        WHERE
            user_id = u.id
    );

INSERT INTO
    profile_instruments (profile_id, instrument_id)
SELECT
    p.id,
    i.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN instrument i ON i.name = 'Guitar'
WHERE
    u.email = 'sara@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_instruments pi
        WHERE
            pi.profile_id = p.id
            AND pi.instrument_id = i.id
    );

INSERT INTO
    profile_genre (profile_id, genre_id)
SELECT
    p.id,
    g.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN genre g ON g.name = 'Rock'
WHERE
    u.email = 'sara@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_genre pg
        WHERE
            pg.profile_id = p.id
            AND pg.genre_id = g.id
    );

INSERT INTO
    video_link (profile_id, url, platform)
SELECT
    p.id,
    'https://www.youtube.com/watch?v=fJ9rUzIMcZQ',
    'YOUTUBE'
FROM
    profile p
    JOIN users u ON p.user_id = u.id
WHERE
    u.email = 'sara@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            video_link
        WHERE
            profile_id = p.id
    );

-- ---------- Marie Dubois — drummer in Paris ----------
INSERT INTO
    users (
        email,
        password_hash,
        date_of_birth,
        created_at,
        enabled,
        role
    )
SELECT
    'marie@musicrew.test',
    '$2b$10$s0muS.0QDWUNBwkJNANsFueMHG.AYJiy277nJWCFFP9KmBwVXtjqS',
    '1990-11-05',
    CURRENT_TIMESTAMP,
    TRUE,
    'USER'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            users
        WHERE
            email = 'marie@musicrew.test'
    );

INSERT INTO
    profile (
        user_id,
        display_name,
        bio,
        country,
        city,
        skill_badge,
        profile_pic_path,
        hidden,
        created_at,
        updated_at
    )
SELECT
    u.id,
    'Marie Dubois',
    'Drummer in Paris. Sessions, gigs, rehearsals. 12+ years. Funk, jazz, anything groovy.',
    'FR',
    'Paris',
    'PRO',
    'https://ui-avatars.com/api/?name=Marie+Dubois&size=512&background=4a5568&color=fff',
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.email = 'marie@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile
        WHERE
            user_id = u.id
    );

INSERT INTO
    profile_instruments (profile_id, instrument_id)
SELECT
    p.id,
    i.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN instrument i ON i.name = 'Drums'
WHERE
    u.email = 'marie@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_instruments pi
        WHERE
            pi.profile_id = p.id
            AND pi.instrument_id = i.id
    );

INSERT INTO
    profile_genre (profile_id, genre_id)
SELECT
    p.id,
    g.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN genre g ON g.name = 'Funk'
WHERE
    u.email = 'marie@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_genre pg
        WHERE
            pg.profile_id = p.id
            AND pg.genre_id = g.id
    );

INSERT INTO
    video_link (profile_id, url, platform)
SELECT
    p.id,
    'https://www.youtube.com/watch?v=Y8JFxS1HlDo',
    'YOUTUBE'
FROM
    profile p
    JOIN users u ON p.user_id = u.id
WHERE
    u.email = 'marie@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            video_link
        WHERE
            profile_id = p.id
    );

-- =====================================================================
-- Seven more seed portfolio users (10 total with Aki / Sara / Marie above).
-- Same pattern; all passwords = "Password".
-- =====================================================================
-- ---------- Liam Walsh — bassist in London ----------
INSERT INTO
    users (
        email,
        password_hash,
        date_of_birth,
        created_at,
        enabled,
        role
    )
SELECT
    'liam@musicrew.test',
    '$2b$10$s0muS.0QDWUNBwkJNANsFueMHG.AYJiy277nJWCFFP9KmBwVXtjqS',
    '1994-02-18',
    CURRENT_TIMESTAMP,
    TRUE,
    'USER'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            users
        WHERE
            email = 'liam@musicrew.test'
    );

INSERT INTO
    profile (
        user_id,
        display_name,
        bio,
        country,
        city,
        skill_badge,
        profile_pic_path,
        hidden,
        created_at,
        updated_at
    )
SELECT
    u.id,
    'Liam Walsh',
    'Jazz bassist in London. Loves walking lines, upright when needed. Available for gigs and sessions.',
    'GB',
    'London',
    'INTERMEDIATE',
    'https://ui-avatars.com/api/?name=Liam+Walsh&size=512&background=4a5568&color=fff',
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.email = 'liam@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile
        WHERE
            user_id = u.id
    );

INSERT INTO
    profile_instruments (profile_id, instrument_id)
SELECT
    p.id,
    i.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN instrument i ON i.name = 'Bass'
WHERE
    u.email = 'liam@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_instruments pi
        WHERE
            pi.profile_id = p.id
            AND pi.instrument_id = i.id
    );

INSERT INTO
    profile_genre (profile_id, genre_id)
SELECT
    p.id,
    g.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN genre g ON g.name = 'Jazz'
WHERE
    u.email = 'liam@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_genre pg
        WHERE
            pg.profile_id = p.id
            AND pg.genre_id = g.id
    );

INSERT INTO
    video_link (profile_id, url, platform)
SELECT
    p.id,
    'https://www.youtube.com/watch?v=eVTXPUF4Oz4',
    'YOUTUBE'
FROM
    profile p
    JOIN users u ON p.user_id = u.id
WHERE
    u.email = 'liam@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            video_link
        WHERE
            profile_id = p.id
    );

-- ---------- Hana Sato — pianist in Osaka ----------
INSERT INTO
    users (
        email,
        password_hash,
        date_of_birth,
        created_at,
        enabled,
        role
    )
SELECT
    'hana@musicrew.test',
    '$2b$10$s0muS.0QDWUNBwkJNANsFueMHG.AYJiy277nJWCFFP9KmBwVXtjqS',
    '1997-06-09',
    CURRENT_TIMESTAMP,
    TRUE,
    'USER'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            users
        WHERE
            email = 'hana@musicrew.test'
    );

INSERT INTO
    profile (
        user_id,
        display_name,
        bio,
        country,
        city,
        skill_badge,
        profile_pic_path,
        hidden,
        created_at,
        updated_at
    )
SELECT
    u.id,
    'Hana Sato',
    'Pianist and songwriter in Osaka. Pop arrangements, demo production, looking for a vocalist.',
    'JP',
    'Osaka',
    'ADVANCED',
    'https://ui-avatars.com/api/?name=Hana+Sato&size=512&background=4a5568&color=fff',
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.email = 'hana@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile
        WHERE
            user_id = u.id
    );

INSERT INTO
    profile_instruments (profile_id, instrument_id)
SELECT
    p.id,
    i.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN instrument i ON i.name = 'Keyboard/Piano'
WHERE
    u.email = 'hana@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_instruments pi
        WHERE
            pi.profile_id = p.id
            AND pi.instrument_id = i.id
    );

INSERT INTO
    profile_genre (profile_id, genre_id)
SELECT
    p.id,
    g.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN genre g ON g.name = 'Pop'
WHERE
    u.email = 'hana@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_genre pg
        WHERE
            pg.profile_id = p.id
            AND pg.genre_id = g.id
    );

INSERT INTO
    video_link (profile_id, url, platform)
SELECT
    p.id,
    'https://www.youtube.com/watch?v=L_jWHffIx5E',
    'YOUTUBE'
FROM
    profile p
    JOIN users u ON p.user_id = u.id
WHERE
    u.email = 'hana@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            video_link
        WHERE
            profile_id = p.id
    );

-- ---------- Diego Rivera — trumpeter in Mexico City ----------
INSERT INTO
    users (
        email,
        password_hash,
        date_of_birth,
        created_at,
        enabled,
        role
    )
SELECT
    'diego@musicrew.test',
    '$2b$10$s0muS.0QDWUNBwkJNANsFueMHG.AYJiy277nJWCFFP9KmBwVXtjqS',
    '1988-09-14',
    CURRENT_TIMESTAMP,
    TRUE,
    'USER'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            users
        WHERE
            email = 'diego@musicrew.test'
    );

INSERT INTO
    profile (
        user_id,
        display_name,
        bio,
        country,
        city,
        skill_badge,
        profile_pic_path,
        hidden,
        created_at,
        updated_at
    )
SELECT
    u.id,
    'Diego Rivera',
    'Trumpet player in CDMX. Latin, salsa, mariachi when needed. Conservatory trained.',
    'MX',
    'Mexico City',
    'PRO',
    'https://ui-avatars.com/api/?name=Diego+Rivera&size=512&background=4a5568&color=fff',
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.email = 'diego@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile
        WHERE
            user_id = u.id
    );

INSERT INTO
    profile_instruments (profile_id, instrument_id)
SELECT
    p.id,
    i.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN instrument i ON i.name = 'Trumpet'
WHERE
    u.email = 'diego@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_instruments pi
        WHERE
            pi.profile_id = p.id
            AND pi.instrument_id = i.id
    );

INSERT INTO
    profile_genre (profile_id, genre_id)
SELECT
    p.id,
    g.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN genre g ON g.name = 'Latin'
WHERE
    u.email = 'diego@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_genre pg
        WHERE
            pg.profile_id = p.id
            AND pg.genre_id = g.id
    );

INSERT INTO
    video_link (profile_id, url, platform)
SELECT
    p.id,
    'https://www.youtube.com/watch?v=jbe1L3JZbgU',
    'YOUTUBE'
FROM
    profile p
    JOIN users u ON p.user_id = u.id
WHERE
    u.email = 'diego@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            video_link
        WHERE
            profile_id = p.id
    );

-- ---------- Anna Lindberg — violinist in Stockholm ----------
INSERT INTO
    users (
        email,
        password_hash,
        date_of_birth,
        created_at,
        enabled,
        role
    )
SELECT
    'anna@musicrew.test',
    '$2b$10$s0muS.0QDWUNBwkJNANsFueMHG.AYJiy277nJWCFFP9KmBwVXtjqS',
    '1992-12-03',
    CURRENT_TIMESTAMP,
    TRUE,
    'USER'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            users
        WHERE
            email = 'anna@musicrew.test'
    );

INSERT INTO
    profile (
        user_id,
        display_name,
        bio,
        country,
        city,
        skill_badge,
        profile_pic_path,
        hidden,
        created_at,
        updated_at
    )
SELECT
    u.id,
    'Anna Lindberg',
    'Violinist in Stockholm. Classical training, open to indie-folk crossovers and chamber-pop sessions.',
    'SE',
    'Stockholm',
    'ADVANCED',
    'https://ui-avatars.com/api/?name=Anna+Lindberg&size=512&background=4a5568&color=fff',
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.email = 'anna@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile
        WHERE
            user_id = u.id
    );

INSERT INTO
    profile_instruments (profile_id, instrument_id)
SELECT
    p.id,
    i.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN instrument i ON i.name = 'Violin'
WHERE
    u.email = 'anna@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_instruments pi
        WHERE
            pi.profile_id = p.id
            AND pi.instrument_id = i.id
    );

INSERT INTO
    profile_genre (profile_id, genre_id)
SELECT
    p.id,
    g.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN genre g ON g.name = 'Classical'
WHERE
    u.email = 'anna@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_genre pg
        WHERE
            pg.profile_id = p.id
            AND pg.genre_id = g.id
    );

INSERT INTO
    video_link (profile_id, url, platform)
SELECT
    p.id,
    'https://www.youtube.com/watch?v=hT_nvWreIhg',
    'YOUTUBE'
FROM
    profile p
    JOIN users u ON p.user_id = u.id
WHERE
    u.email = 'anna@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            video_link
        WHERE
            profile_id = p.id
    );

-- ---------- Carlos Silva — producer in São Paulo ----------
INSERT INTO
    users (
        email,
        password_hash,
        date_of_birth,
        created_at,
        enabled,
        role
    )
SELECT
    'carlos@musicrew.test',
    '$2b$10$s0muS.0QDWUNBwkJNANsFueMHG.AYJiy277nJWCFFP9KmBwVXtjqS',
    '1991-05-27',
    CURRENT_TIMESTAMP,
    TRUE,
    'USER'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            users
        WHERE
            email = 'carlos@musicrew.test'
    );

INSERT INTO
    profile (
        user_id,
        display_name,
        bio,
        country,
        city,
        skill_badge,
        profile_pic_path,
        hidden,
        created_at,
        updated_at
    )
SELECT
    u.id,
    'Carlos Silva',
    'Electronic producer in São Paulo. House, brazilian bass, occasional film scoring. DM for collabs.',
    'BR',
    'São Paulo',
    'PRO',
    'https://ui-avatars.com/api/?name=Carlos+Silva&size=512&background=4a5568&color=fff',
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.email = 'carlos@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile
        WHERE
            user_id = u.id
    );

INSERT INTO
    profile_instruments (profile_id, instrument_id)
SELECT
    p.id,
    i.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN instrument i ON i.name = 'Producer'
WHERE
    u.email = 'carlos@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_instruments pi
        WHERE
            pi.profile_id = p.id
            AND pi.instrument_id = i.id
    );

INSERT INTO
    profile_genre (profile_id, genre_id)
SELECT
    p.id,
    g.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN genre g ON g.name = 'Electronic'
WHERE
    u.email = 'carlos@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_genre pg
        WHERE
            pg.profile_id = p.id
            AND pg.genre_id = g.id
    );

INSERT INTO
    video_link (profile_id, url, platform)
SELECT
    p.id,
    'https://www.youtube.com/watch?v=oRdxUFDoQe0',
    'YOUTUBE'
FROM
    profile p
    JOIN users u ON p.user_id = u.id
WHERE
    u.email = 'carlos@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            video_link
        WHERE
            profile_id = p.id
    );

-- ---------- Priya Sharma — vocalist in Mumbai ----------
INSERT INTO
    users (
        email,
        password_hash,
        date_of_birth,
        created_at,
        enabled,
        role
    )
SELECT
    'priya@musicrew.test',
    '$2b$10$s0muS.0QDWUNBwkJNANsFueMHG.AYJiy277nJWCFFP9KmBwVXtjqS',
    '1995-10-21',
    CURRENT_TIMESTAMP,
    TRUE,
    'USER'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            users
        WHERE
            email = 'priya@musicrew.test'
    );

INSERT INTO
    profile (
        user_id,
        display_name,
        bio,
        country,
        city,
        skill_badge,
        profile_pic_path,
        hidden,
        created_at,
        updated_at
    )
SELECT
    u.id,
    'Priya Sharma',
    'Vocalist in Mumbai. Hindi, English, conversational Bengali. Pop and R&B, comfortable in studio.',
    'IN',
    'Mumbai',
    'INTERMEDIATE',
    'https://ui-avatars.com/api/?name=Priya+Sharma&size=512&background=4a5568&color=fff',
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.email = 'priya@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile
        WHERE
            user_id = u.id
    );

INSERT INTO
    profile_instruments (profile_id, instrument_id)
SELECT
    p.id,
    i.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN instrument i ON i.name = 'Vocals'
WHERE
    u.email = 'priya@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_instruments pi
        WHERE
            pi.profile_id = p.id
            AND pi.instrument_id = i.id
    );

INSERT INTO
    profile_genre (profile_id, genre_id)
SELECT
    p.id,
    g.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN genre g ON g.name = 'Pop'
WHERE
    u.email = 'priya@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_genre pg
        WHERE
            pg.profile_id = p.id
            AND pg.genre_id = g.id
    );

INSERT INTO
    video_link (profile_id, url, platform)
SELECT
    p.id,
    'https://www.youtube.com/watch?v=Yh0AhrY9GjA',
    'YOUTUBE'
FROM
    profile p
    JOIN users u ON p.user_id = u.id
WHERE
    u.email = 'priya@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            video_link
        WHERE
            profile_id = p.id
    );

-- ---------- Min-jun Park — DJ in Seoul ----------
INSERT INTO
    users (
        email,
        password_hash,
        date_of_birth,
        created_at,
        enabled,
        role
    )
SELECT
    'minjun@musicrew.test',
    '$2b$10$s0muS.0QDWUNBwkJNANsFueMHG.AYJiy277nJWCFFP9KmBwVXtjqS',
    '1996-07-30',
    CURRENT_TIMESTAMP,
    TRUE,
    'USER'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            users
        WHERE
            email = 'minjun@musicrew.test'
    );

INSERT INTO
    profile (
        user_id,
        display_name,
        bio,
        country,
        city,
        skill_badge,
        profile_pic_path,
        hidden,
        created_at,
        updated_at
    )
SELECT
    u.id,
    'Min-jun Park',
    'DJ in Seoul. Hip-hop and trap sets. Open to producing for vocalists and rappers.',
    'KR',
    'Seoul',
    'ADVANCED',
    'https://ui-avatars.com/api/?name=Min+jun+Park&size=512&background=4a5568&color=fff',
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.email = 'minjun@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile
        WHERE
            user_id = u.id
    );

INSERT INTO
    profile_instruments (profile_id, instrument_id)
SELECT
    p.id,
    i.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN instrument i ON i.name = 'DJ/Turntables'
WHERE
    u.email = 'minjun@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_instruments pi
        WHERE
            pi.profile_id = p.id
            AND pi.instrument_id = i.id
    );

INSERT INTO
    profile_genre (profile_id, genre_id)
SELECT
    p.id,
    g.id
FROM
    profile p
    JOIN users u ON p.user_id = u.id
    JOIN genre g ON g.name = 'Hip-Hop'
WHERE
    u.email = 'minjun@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            profile_genre pg
        WHERE
            pg.profile_id = p.id
            AND pg.genre_id = g.id
    );

INSERT INTO
    video_link (profile_id, url, platform)
SELECT
    p.id,
    'https://www.youtube.com/watch?v=qjmoeMP0RuI',
    'YOUTUBE'
FROM
    profile p
    JOIN users u ON p.user_id = u.id
WHERE
    u.email = 'minjun@musicrew.test'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            video_link
        WHERE
            profile_id = p.id
    );