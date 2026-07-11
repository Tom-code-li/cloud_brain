-- 将已有数据库中的旧英文枚举值迁移为《枚举类型约束.docx》中的标准值。
-- 建议在执行前备份 doctor_platform 数据库。

UPDATE registration
SET source = CASE source
    WHEN 'online' THEN '线上'
    WHEN 'offline' THEN '线下'
    ELSE source
END
WHERE source IN ('online', 'offline');

UPDATE registration
SET fee_status = CASE fee_status
    WHEN 'unpaid' THEN '待支付'
    WHEN 'paid' THEN '已支付'
    WHEN 'refunded' THEN '已退费'
    ELSE fee_status
END
WHERE fee_status IN ('unpaid', 'paid', 'refunded');

UPDATE registration
SET status = CASE status
    WHEN 'registered' THEN '待支付'
    WHEN 'completed' THEN '已完成'
    WHEN 'cancelled' THEN '已取消'
    ELSE status
END
WHERE status IN ('registered', 'completed', 'cancelled');

UPDATE doctor_schedule
SET time_period = CASE time_period
    WHEN 'morning' THEN '上午'
    WHEN 'afternoon' THEN '下午'
    WHEN 'night' THEN '夜间'
    ELSE time_period
END
WHERE time_period IN ('morning', 'afternoon', 'night');

UPDATE doctor_schedule
SET status = CASE status
    WHEN 'active' THEN '可预约'
    ELSE status
END
WHERE status IN ('active');

UPDATE fee_order
SET business_type = CASE business_type
    WHEN 'registration' THEN 'REGISTRATION'
    WHEN 'exam' THEN 'EXAM_LAB_ORDER'
    WHEN 'lab' THEN 'EXAM_LAB_ORDER'
    WHEN 'prescription' THEN 'PRESCRIPTION'
    WHEN 'other' THEN 'OTHER'
    ELSE business_type
END
WHERE business_type IN ('registration', 'exam', 'lab', 'prescription', 'other');

UPDATE fee_order
SET status = CASE status
    WHEN 'unpaid' THEN '待支付'
    WHEN 'paid' THEN '已支付'
    WHEN 'refunded' THEN '已退费'
    ELSE status
END
WHERE status IN ('unpaid', 'paid', 'refunded');

UPDATE outpatient_visit
SET status = CASE status
    WHEN 'registered' THEN '待支付'
    WHEN 'completed' THEN '已完成'
    WHEN 'cancelled' THEN '已取消'
    ELSE status
END
WHERE status IN ('registered', 'completed', 'cancelled');

UPDATE medical_record
SET status = CASE status
    WHEN 'draft' THEN '初诊暂存'
    WHEN 'completed' THEN '已完成'
    WHEN 'cancelled' THEN '已作废'
    ELSE status
END
WHERE status IN ('draft', 'completed', 'cancelled');

UPDATE medical_item
SET item_type = CASE item_type
    WHEN 'exam' THEN '检查'
    WHEN 'lab' THEN '检验'
    ELSE item_type
END
WHERE item_type IN ('exam', 'lab');

UPDATE exam_lab_order
SET order_type = CASE order_type
    WHEN 'exam' THEN '检查'
    WHEN 'lab' THEN '检验'
    ELSE order_type
END
WHERE order_type IN ('exam', 'lab');

UPDATE exam_lab_order
SET fee_status = CASE fee_status
    WHEN 'unpaid' THEN '待支付'
    WHEN 'paid' THEN '已支付'
    WHEN 'refunded' THEN '已退费'
    ELSE fee_status
END
WHERE fee_status IN ('unpaid', 'paid', 'refunded');

UPDATE exam_lab_order
SET status = CASE status
    WHEN 'pending' THEN '待缴费'
    WHEN 'completed' THEN '已完成'
    WHEN 'cancelled' THEN '已取消'
    ELSE status
END
WHERE status IN ('pending', 'completed', 'cancelled');

UPDATE exam_lab_order_item
SET item_type = CASE item_type
    WHEN 'exam' THEN '检查'
    WHEN 'lab' THEN '检验'
    ELSE item_type
END
WHERE item_type IN ('exam', 'lab');

UPDATE exam_lab_order_item
SET status = CASE status
    WHEN 'pending' THEN '待缴费'
    WHEN 'completed' THEN '已完成'
    WHEN 'cancelled' THEN '已取消'
    ELSE status
END
WHERE status IN ('pending', 'completed', 'cancelled');

UPDATE exam_lab_report
SET report_type = CASE report_type
    WHEN 'exam' THEN '检查'
    WHEN 'lab' THEN '检验'
    ELSE report_type
END
WHERE report_type IN ('exam', 'lab');

UPDATE exam_lab_report
SET status = CASE status
    WHEN 'draft' THEN '草稿'
    WHEN 'published' THEN '已发布'
    ELSE status
END
WHERE status IN ('draft', 'published');

UPDATE prescription
SET fee_status = CASE fee_status
    WHEN 'unpaid' THEN '待支付'
    WHEN 'paid' THEN '已支付'
    WHEN 'refunded' THEN '已退费'
    ELSE fee_status
END
WHERE fee_status IN ('unpaid', 'paid', 'refunded');

UPDATE prescription
SET audit_status = CASE audit_status
    WHEN 'pending' THEN '待审核'
    WHEN 'approved' THEN '审核通过'
    WHEN 'rejected' THEN '审核驳回'
    ELSE audit_status
END
WHERE audit_status IN ('pending', 'approved', 'rejected');

UPDATE prescription
SET status = CASE status
    WHEN 'active' THEN '待缴费'
    WHEN 'dispensed' THEN '已发药'
    WHEN 'completed' THEN '已完成'
    WHEN 'cancelled' THEN '已取消'
    ELSE status
END
WHERE status IN ('active', 'dispensed', 'completed', 'cancelled');

UPDATE prescription_item
SET status = CASE status
    WHEN 'pending' THEN '待发药'
    WHEN 'active' THEN '待发药'
    WHEN 'dispensed' THEN '已发药'
    WHEN 'cancelled' THEN '已取消'
    ELSE status
END
WHERE status IN ('pending', 'active', 'dispensed', 'cancelled');

UPDATE ai_consultation
SET risk_level = CASE risk_level
    WHEN 'normal' THEN '普通'
    WHEN 'urgent' THEN '紧急'
    WHEN 'unknown' THEN '普通'
    ELSE risk_level
END
WHERE risk_level IN ('normal', 'urgent', 'unknown');

UPDATE ai_consultation
SET status = CASE status
    WHEN 'pending' THEN '已生成'
    WHEN 'completed' THEN '已生成'
    ELSE status
END
WHERE status IN ('pending', 'completed');

UPDATE triage_record
SET risk_level = CASE risk_level
    WHEN 'normal' THEN '普通'
    WHEN 'urgent' THEN '紧急'
    ELSE risk_level
END
WHERE risk_level IN ('normal', 'urgent');

UPDATE triage_record
SET status = CASE status
    WHEN 'pending' THEN '待分诊'
    WHEN 'completed' THEN '已完成'
    WHEN 'cancelled' THEN '已取消'
    ELSE status
END
WHERE status IN ('pending', 'completed', 'cancelled');
