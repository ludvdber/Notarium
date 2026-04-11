-- ============================================================
-- V4: Split users, denormalize documents, fix cascades
-- ============================================================

-- 1. Create user_profiles table (split from users)
CREATE TABLE user_profiles (
    user_id           BIGINT          PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    bio               VARCHAR(500),
    website           VARCHAR(255),
    github            VARCHAR(255),
    linkedin          VARCHAR(255),
    discord           VARCHAR(255),
    discord_id        VARCHAR(255),
    profile_public    BOOLEAN         NOT NULL DEFAULT FALSE,
    show_in_carousel  BOOLEAN         NOT NULL DEFAULT FALSE,
    theme_pref        VARCHAR(50)     NOT NULL DEFAULT 'dark',
    ad_free           BOOLEAN         NOT NULL DEFAULT FALSE,
    ad_free_until     TIMESTAMP
);

-- 2. Migrate data from users to user_profiles
INSERT INTO user_profiles (user_id, bio, website, github, linkedin, discord, discord_id,
                           profile_public, show_in_carousel, theme_pref, ad_free, ad_free_until)
SELECT id, bio, website, github, linkedin, discord, discord_id,
       profile_public, show_in_carousel, theme_pref, ad_free, ad_free_until
FROM users;

-- 3. Drop migrated columns from users
ALTER TABLE users DROP COLUMN bio;
ALTER TABLE users DROP COLUMN website;
ALTER TABLE users DROP COLUMN github;
ALTER TABLE users DROP COLUMN linkedin;
ALTER TABLE users DROP COLUMN discord;
ALTER TABLE users DROP COLUMN discord_id;
ALTER TABLE users DROP COLUMN profile_public;
ALTER TABLE users DROP COLUMN show_in_carousel;
ALTER TABLE users DROP COLUMN theme_pref;
ALTER TABLE users DROP COLUMN ad_free;
ALTER TABLE users DROP COLUMN ad_free_until;

-- 4. Add unique constraint on OAuth identity
ALTER TABLE users ADD CONSTRAINT uq_oauth_identity UNIQUE (oauth_provider, oauth_id);

-- 5. Denormalize documents: add cached counters
ALTER TABLE documents ADD COLUMN download_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE documents ADD COLUMN average_rating NUMERIC(3, 2) NOT NULL DEFAULT 0;
ALTER TABLE documents ADD COLUMN rating_count INTEGER NOT NULL DEFAULT 0;

-- 6. Backfill denormalized counters from existing data
UPDATE documents d SET
    download_count = (SELECT COUNT(*) FROM downloads dl WHERE dl.document_id = d.id),
    average_rating = COALESCE((SELECT AVG(r.score) FROM ratings r WHERE r.document_id = d.id), 0),
    rating_count = (SELECT COUNT(*) FROM ratings r WHERE r.document_id = d.id);

-- 7. Drop downloads table (replaced by download_count counter)
DROP TABLE downloads;

-- 8. Fix cascades: user deletion must NOT destroy courses or documents
ALTER TABLE courses DROP CONSTRAINT courses_created_by_fkey;
ALTER TABLE courses ALTER COLUMN created_by DROP NOT NULL;
ALTER TABLE courses ADD CONSTRAINT courses_created_by_fkey
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE documents DROP CONSTRAINT documents_user_id_fkey;
ALTER TABLE documents ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE documents ADD CONSTRAINT documents_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE donations DROP CONSTRAINT donations_user_id_fkey;
ALTER TABLE donations ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE donations ADD CONSTRAINT donations_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE delegate_history DROP CONSTRAINT delegate_history_user_id_fkey;
ALTER TABLE delegate_history ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE delegate_history ADD CONSTRAINT delegate_history_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- 9. Unique constraint: one report per user per document
ALTER TABLE reports ADD CONSTRAINT uq_report_document_user UNIQUE (document_id, user_id);

-- 10. Unique active delegate per section
CREATE UNIQUE INDEX uq_active_delegate_per_section
    ON delegate_history(section_id) WHERE end_date IS NULL;

-- 11. Missing indexes
CREATE INDEX idx_users_oauth ON users(oauth_provider, oauth_id);
CREATE INDEX idx_tags_label ON tags(label);
CREATE INDEX idx_courses_section_approved ON courses(section_id, approved);
CREATE INDEX idx_reports_document ON reports(document_id);
CREATE INDEX idx_documents_download_count ON documents(download_count DESC);
