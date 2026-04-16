-- Track explicit CGU/Terms acceptance per user (GDPR requirement).
ALTER TABLE user_profiles ADD COLUMN terms_accepted_at TIMESTAMP;
