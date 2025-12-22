-- SIGUNGU → SIDO 매핑
UPDATE region AS child
SET parent_id = parent.id
FROM region AS parent
WHERE child.level = 'SIGUNGU'
  AND parent.level = 'SIDO'
  AND SUBSTRING(child.sgg_code, 1, 2) = SUBSTRING(parent.sgg_code, 1, 2);


-- EUP_MYEON_DONG → SIGUNGU 매핑
UPDATE region AS child
SET parent_id = parent.id
FROM region AS parent
WHERE child.level = 'EUP_MYEON_DONG'
  AND parent.level = 'SIGUNGU'
  AND child.sgg_code = parent.sgg_code;

-- 아직 parent_id 비어 있는 EUP_MYEON_DONG → SIDO로 매핑((세종특별자치시)
UPDATE region AS child
SET parent_id = parent.id
FROM region AS parent
WHERE child.level = 'EUP_MYEON_DONG'
  AND child.parent_id IS NULL
  AND parent.level = 'SIDO'
  AND SUBSTRING(child.sgg_code, 1, 2) = SUBSTRING(parent.sgg_code, 1, 2);
