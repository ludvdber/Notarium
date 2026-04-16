-- Normalize existing tags: lowercase + trim, then deduplicate.
-- Step 1: normalize all labels
UPDATE tags SET label = LOWER(TRIM(label));

-- Step 2: remove duplicates (keep lowest id per document_id + label)
DELETE FROM tags t1
USING tags t2
WHERE t1.document_id = t2.document_id
  AND t1.label = t2.label
  AND t1.id > t2.id;
