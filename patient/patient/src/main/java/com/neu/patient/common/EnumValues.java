package com.neu.patient.common;

public final class EnumValues {
    private EnumValues() {}

    public static final String SOURCE_ONLINE = "线上";
    public static final String SOURCE_OFFLINE = "线下";

    public static final String FEE_WAITING_PAYMENT = "待支付";
    public static final String FEE_PAID = "已支付";
    public static final String FEE_REFUNDED = "已退费";

    public static final String REGISTRATION_WAITING_PAYMENT = "待支付";
    public static final String REGISTRATION_WAITING_CONFIRMATION = "待确认";
    public static final String REGISTRATION_IN_VISIT = "接诊中";
    public static final String REGISTRATION_COMPLETED = "已完成";
    public static final String REGISTRATION_CANCELLED = "已取消";
    public static final String REGISTRATION_NO_SHOW = "爽约";

    public static final String SCHEDULE_MORNING = "上午";
    public static final String SCHEDULE_AFTERNOON = "下午";
    public static final String SCHEDULE_NIGHT = "夜间";
    public static final String SCHEDULE_AVAILABLE = "可预约";
    public static final String SCHEDULE_FULL = "约满";
    public static final String SCHEDULE_STOPPED = "停诊";
    public static final String SCHEDULE_EXPIRED = "已过期";

    public static final String BUSINESS_REGISTRATION = "REGISTRATION";
    public static final String BUSINESS_EXAM_LAB_ORDER = "EXAM_LAB_ORDER";
    public static final String BUSINESS_PRESCRIPTION = "PRESCRIPTION";
    public static final String BUSINESS_OTHER = "OTHER";

    public static final String FEE_ORDER_WAITING_PAYMENT = "待支付";
    public static final String FEE_ORDER_PAID = "已支付";
    public static final String FEE_ORDER_REFUNDED = "已退费";

    public static final String OUTPATIENT_WAITING_VISIT = "待接诊";
    public static final String OUTPATIENT_IN_VISIT = "接诊中";
    public static final String OUTPATIENT_WAITING_EXAM_LAB = "待检查检验";
    public static final String OUTPATIENT_EXAM_LAB_IN_PROGRESS = "检查检验中";
    public static final String OUTPATIENT_REPORT_WAITING_REVIEW = "报告待回阅";
    public static final String OUTPATIENT_WAITING_DIAGNOSIS = "待确诊";
    public static final String OUTPATIENT_WAITING_DISPOSAL = "待处置";
    public static final String OUTPATIENT_COMPLETED = "已完成";

    public static final String MEDICAL_RECORD_DRAFT = "初诊暂存";
    public static final String MEDICAL_RECORD_WAITING_SUPPLEMENT = "待补充";
    public static final String MEDICAL_RECORD_COMPLETED = "已完成";
    public static final String MEDICAL_RECORD_VOIDED = "已作废";

    public static final String ITEM_EXAM = "检查";
    public static final String ITEM_LAB = "检验";

    public static final String EXAM_LAB_WAITING_PAYMENT = "待缴费";
    public static final String EXAM_LAB_WAITING_EXECUTION = "待执行";
    public static final String EXAM_LAB_EXECUTING = "执行中";
    public static final String EXAM_LAB_COMPLETED = "已完成";

    public static final String ITEM_REGISTRATION = "挂号";
    public static final String REPORT_DRAFT = "草稿";
    public static final String REPORT_PUBLISHED = "已发布";
    public static final String REPORT_REVIEWED = "已回阅";

    public static final String PRESCRIPTION_AUDIT_PENDING = "待审核";
    public static final String PRESCRIPTION_AUDIT_APPROVED = "审核通过";
    public static final String PRESCRIPTION_AUDIT_REJECTED = "审核驳回";
    public static final String PRESCRIPTION_WAITING_PAYMENT = "待缴费";
    public static final String PRESCRIPTION_WAITING_DISPENSE = "待发药";
    public static final String PRESCRIPTION_DISPENSING = "发药中";
    public static final String PRESCRIPTION_DISPENSED = "已发药";
    public static final String PRESCRIPTION_COMPLETED = "已完成";
    public static final String PRESCRIPTION_RETURNED = "已退药";
    public static final String PRESCRIPTION_CANCELLED = "已取消";

    public static final String RISK_NORMAL = "普通";
    public static final String RISK_URGENT = "紧急";
    public static final String AI_GENERATED = "已生成";
    public static final String AI_ADOPTED = "已采纳";
    public static final String AI_IGNORED = "已忽略";
}
