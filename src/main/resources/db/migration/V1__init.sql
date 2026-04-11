CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE users (
    id                BIGSERIAL       PRIMARY KEY,
    oauth_provider    VARCHAR(50)     NOT NULL,
    oauth_id          VARCHAR(255)    NOT NULL,
    username          VARCHAR(255)    NOT NULL UNIQUE,
    email_hash        VARCHAR(255)    UNIQUE,
    verified          BOOLEAN         NOT NULL DEFAULT FALSE,
    ad_free           BOOLEAN         NOT NULL DEFAULT FALSE,
    ad_free_until     TIMESTAMP,
    discord_id        VARCHAR(255),
    xp                INTEGER         NOT NULL DEFAULT 0,
    profile_public    BOOLEAN         NOT NULL DEFAULT FALSE,
    bio               VARCHAR(500),
    website           VARCHAR(255),
    github            VARCHAR(255),
    linkedin          VARCHAR(255),
    discord           VARCHAR(255),
    theme_pref        VARCHAR(50)     NOT NULL DEFAULT 'dark',
    show_in_carousel  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE sections (
    id                BIGSERIAL       PRIMARY KEY,
    name              VARCHAR(255)    NOT NULL UNIQUE,
    icon              VARCHAR(255),
    approved          BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE TABLE courses (
    id                BIGSERIAL       PRIMARY KEY,
    section_id        BIGINT          NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    name              VARCHAR(255)    NOT NULL,
    approved          BOOLEAN         NOT NULL DEFAULT FALSE,
    created_by        BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE professors (
    id                BIGSERIAL       PRIMARY KEY,
    name              VARCHAR(255)    NOT NULL,
    approved          BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE TABLE documents (
    id                BIGSERIAL       PRIMARY KEY,
    course_id         BIGINT          NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    category          VARCHAR(50)     NOT NULL,
    title             VARCHAR(255)    NOT NULL,
    file_key          VARCHAR(512)    NOT NULL,
    user_id           BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    anonymous         BOOLEAN         NOT NULL DEFAULT FALSE,
    verified          BOOLEAN         NOT NULL DEFAULT FALSE,
    language          VARCHAR(10)     NOT NULL DEFAULT 'FR',
    ai_generated      BOOLEAN         NOT NULL DEFAULT FALSE,
    summary_ai        VARCHAR(2000),
    year              VARCHAR(20),
    professor_id      BIGINT          REFERENCES professors(id) ON DELETE SET NULL,
    file_size         BIGINT          NOT NULL,
    created_at        TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE tags (
    id                BIGSERIAL       PRIMARY KEY,
    document_id       BIGINT          NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    label             VARCHAR(255)    NOT NULL
);

CREATE TABLE ratings (
    id                BIGSERIAL       PRIMARY KEY,
    document_id       BIGINT          NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    user_id           BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    score             INTEGER         NOT NULL CHECK (score >= 1 AND score <= 5),
    UNIQUE (document_id, user_id)
);

CREATE TABLE favorites (
    id                BIGSERIAL       PRIMARY KEY,
    user_id           BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    document_id       BIGINT          NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    UNIQUE (user_id, document_id)
);

CREATE TABLE reports (
    id                BIGSERIAL       PRIMARY KEY,
    document_id       BIGINT          NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    user_id           BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason            VARCHAR(255)    NOT NULL,
    status            VARCHAR(50)     NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE badges (
    id                BIGSERIAL       PRIMARY KEY,
    user_id           BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    badge_type        VARCHAR(255)    NOT NULL,
    awarded_at        TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE donations (
    id                    BIGSERIAL       PRIMARY KEY,
    user_id               BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount                NUMERIC(10, 2)  NOT NULL,
    kofi_transaction_id   VARCHAR(255)    NOT NULL,
    ad_free_until         TIMESTAMP
);

CREATE TABLE downloads (
    id                BIGSERIAL       PRIMARY KEY,
    document_id       BIGINT          NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    user_id           BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    downloaded_at     TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE delegate_history (
    id                BIGSERIAL       PRIMARY KEY,
    user_id           BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    section_id        BIGINT          NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    start_date        DATE            NOT NULL,
    end_date          DATE
);

-- Indexes
CREATE INDEX idx_documents_course_category ON documents(course_id, category);
CREATE INDEX idx_documents_user ON documents(user_id);
CREATE INDEX idx_documents_created_at ON documents(created_at);
CREATE INDEX idx_ratings_document ON ratings(document_id);
CREATE INDEX idx_favorites_user ON favorites(user_id);
CREATE INDEX idx_downloads_document ON downloads(document_id);
CREATE INDEX idx_tags_document ON tags(document_id);
CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_badges_user ON badges(user_id);
CREATE INDEX idx_delegate_history_end_date ON delegate_history(end_date);
CREATE INDEX idx_users_xp ON users(xp DESC);
