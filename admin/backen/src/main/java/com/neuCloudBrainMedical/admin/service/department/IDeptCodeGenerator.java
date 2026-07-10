package com.neuCloudBrainMedical.admin.service.department;

/**
 * 科室编号生成器接口。
 * 单一职责：根据科室名称生成唯一编码。
 */
public interface IDeptCodeGenerator {

	/**
	 * 根据科室名称生成英文缩写代码。
	 * 例如："神经内科" -> "NEURO"，"心内科" -> "CARDIO"。
	 *
	 * @param deptName 中文科室名称
	 * @return 大写英文缩写代码（2~10位），失败时返回备选编码
	 */
	String generateCode(String deptName);
}