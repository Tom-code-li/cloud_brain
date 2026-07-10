export class Constants {
  static readonly FEE_STATUS_UNPAID: string = '待支付';
  static readonly FEE_STATUS_PAID: string = '已支付';
  static readonly FEE_STATUS_REFUNDED: string = '已退费';

  static readonly REG_STATUS_PENDING: string = '待支付';
  static readonly REG_STATUS_WAITING: string = '待确认';
  static readonly REG_STATUS_IN_PROGRESS: string = '接诊中';
  static readonly REG_STATUS_COMPLETED: string = '已完成';
  static readonly REG_STATUS_CANCELLED: string = '已取消';
  static readonly REG_STATUS_NO_SHOW: string = '爽约';

  static readonly SOURCE_ONLINE: string = '线上';
  static readonly SOURCE_OFFLINE: string = '线下';

  static readonly SCHEDULE_AVAILABLE: string = '可预约';
  static readonly SCHEDULE_FULL: string = '约满';
  static readonly SCHEDULE_CLOSED: string = '停诊';

  static readonly BUSINESS_REGISTRATION: string = 'REGISTRATION';
  static readonly BUSINESS_EXAM_LAB: string = 'EXAM_LAB_ORDER';
  static readonly BUSINESS_PRESCRIPTION: string = 'PRESCRIPTION';
  static readonly BUSINESS_OTHER: string = 'OTHER';

  static readonly AI_STATUS_GENERATED: string = '已生成';
  static readonly AI_STATUS_ADOPTED: string = '已采纳';
  static readonly AI_STATUS_IGNORED: string = '已忽略';
  static readonly RISK_NORMAL: string = '普通';
  static readonly RISK_URGENT: string = '紧急';
}
