SET NAMES utf8mb4;
USE doctor_platform;

START TRANSACTION;

INSERT INTO fee_order_item (
  fee_order_id, item_type, item_id, item_code, item_name, item_spec,
  unit_price, quantity, amount, status
)
SELECT fo.fee_order_id, '挂号', NULL, NULL, '挂号费', NULL,
       fo.total_amount, 1.00, fo.total_amount, fo.status
FROM fee_order fo
WHERE fo.order_no IN (
  'FO178283329090569',
  'FO178283049411368',
  'FO178282250815567',
  'FO178282192498066'
)
AND NOT EXISTS (
  SELECT 1
  FROM fee_order_item foi
  WHERE foi.fee_order_id = fo.fee_order_id
);

COMMIT;
