package com.neuCloudBrainMedical.admin.service.schedule;

import com.neuCloudBrainMedical.admin.dto.doctor.DoctorInfo;
import java.time.LocalDate;
import java.util.List;

/**
 * AI 排班建议客户端接口。
 * 调用外部 AI 服务生成排班建议 JSON。
 */
public interface IAISchedulingClient {

	String requestSchedulingSuggestion(Long departmentId,
	                                   LocalDate startDate,
	                                   LocalDate endDate,
	                                   List<DoctorInfo> doctorList,
	                                   String contextInfo);
}