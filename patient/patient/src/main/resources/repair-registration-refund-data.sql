SET NAMES utf8mb4;
USE doctor_platform;

START TRANSACTION;

UPDATE fee_order fo
JOIN registration r ON r.registration_id = fo.registration_id
SET
  fo.status = '已退费',
  fo.refund_amount = fo.total_amount,
  fo.updated_at = NOW(),
  r.fee_status = '已退费',
  r.updated_at = NOW()
WHERE r.status = '已取消'
  AND fo.business_type = 'REGISTRATION'
  AND fo.status = '已支付';

UPDATE registration r
JOIN fee_order fo ON fo.registration_id = r.registration_id
SET
  r.fee_status = '已退费',
  r.updated_at = NOW()
WHERE r.status = '已取消'
  AND fo.business_type = 'REGISTRATION'
  AND fo.status = '已退费'
  AND r.fee_status <> '已退费';

COMMIT;
