-- Allow reports to survive user deletion: user_id becomes nullable
-- and ON DELETE SET NULL replaces the previous CASCADE behavior.
ALTER TABLE reports ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE reports DROP CONSTRAINT IF EXISTS reports_user_id_fkey;
ALTER TABLE reports
    ADD CONSTRAINT reports_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
