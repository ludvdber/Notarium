-- Allow multiple active delegates per section.
-- The old unique index enforced exactly one active delegate per section;
-- business rule now allows several (e.g. co-delegates).
DROP INDEX IF EXISTS uq_active_delegate_per_section;
