package com.neuCloudBrainMedical.admin.service.department.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuCloudBrainMedical.admin.service.department.IDeptCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * 通过 AI 接口将中文科室名称翻译为英文缩写代码。
 * 例如：神经内科 -> NEURO，心内科 -> CARDIO，骨科 -> ORTHO。
 *
 * <p>若 AI 不可用或返回异常，自动回退为字符拼接+随机数字方案。</p>
 */
@Component
public class DeptCodeGeneratorImpl implements IDeptCodeGenerator {

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Value("${ai.deepseek.base-url:https://api.deepseek.com}")
	private String baseUrl;

	@Value("${ai.deepseek.api-key:}")
	private String apiKey;

	@Value("${ai.deepseek.model:deepseek-v4-flash}")
	private String model;

	private static final String SYSTEM_PROMPT = "请将中文医院科室名称翻译为对应的英文医学缩写代码。"
			+ "规则：全大写，2~10个字母，只返回代码本身，不要解释。"
			+ "示例：神经内科->NEURO, 心内科->CARDIO, 骨科->ORTHO, 急诊科->ER,"
			+ "检验科->LAB, 放射科->RADIO, 药剂科->PHARM, 儿科->PEDIA,"
			+ "外科->SURG, 内科->MED, 耳鼻喉科->ENT, 眼科->OPHTH,"
			+ "皮肤科->DERMA, 中医科->TCM, 产科->OB, 妇科->GYN,"
			+ "口腔科->DENT, 精神科->PSYCH, 康复科->REHAB, 营养科->NUTRI,"
			+ "麻醉科->ANES, 病理科->PATH, ICU->ICU";

	public DeptCodeGeneratorImpl(RestTemplate aiRestTemplate, ObjectMapper objectMapper) {
		this.restTemplate = aiRestTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public String generateCode(String deptName) {
		if (deptName == null || deptName.isBlank()) {
			return "DEPT" + (System.currentTimeMillis() % 1000000);
		}
		if (apiKey == null || apiKey.isBlank()) {
			return fallbackCode(deptName);
		}
		try {
			String code = callAi(deptName);
			return code != null && !code.isBlank() ? code : fallbackCode(deptName);
		} catch (Exception e) {
			return fallbackCode(deptName);
		}
	}

	private String callAi(String deptName) throws Exception {
		String prompt = SYSTEM_PROMPT + "\n现在请翻译：" + deptName;

		Map<String, Object> body = Map.of(
				"model", model,
				"temperature", 0.1,
				"messages", List.of(Map.of("role", "user", "content", prompt)));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(apiKey);

		String response = restTemplate.postForObject(
				baseUrl.replaceAll("/+$", "") + "/chat/completions",
				new HttpEntity<>(body, headers),
				String.class);

		if (response == null) return null;
		JsonNode root = objectMapper.readTree(response);
		JsonNode content = root.path("choices").path(0).path("message").path("content");
		if (!content.isTextual()) return null;

		String text = content.asText().trim().toUpperCase().replaceAll("[^A-Z]", "");
		return text.length() >= 2 && text.length() <= 10 ? text : null;
	}

	private String fallbackCode(String deptName) {
		StringBuilder sb = new StringBuilder();
		for (char c : deptName.toCharArray()) {
			if (Character.isLetter(c)) {
				sb.append(Character.toUpperCase(c));
			}
		}
		String prefix = sb.length() > 0 ? sb.toString() : "DEPT";
		int random = (int) (Math.random() * 9000) + 1000;
		return prefix + random;
	}
}