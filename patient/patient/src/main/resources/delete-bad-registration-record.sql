SET NAMES utf8mb4;
USE doctor_platform;

START TRANSACTION;

DELETE FROM fee_order
WHERE registration_id IN (
  SELECT registration_id FROM registration
  WHERE registration_no = 'RG1782300885214'
);

DELETE FROM registration
WHERE registration_no = 'RG1782300885214';

COMMIT;
