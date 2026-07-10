package com.hospital.ai.common;

import com.hospital.common.core.BusinessException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RoleAiConfigRegistry {
    private final Map<String, RoleAiConfig> roleConfigs;

    public RoleAiConfigRegistry() {
        this(defaultRoleConfigs());
    }

    RoleAiConfigRegistry(Map<String, RoleAiConfig> roleConfigs) {
        this.roleConfigs = Map.copyOf(roleConfigs);
    }

    public static RoleAiConfigRegistry defaults() {
        return new RoleAiConfigRegistry(defaultRoleConfigs());
    }

    public RoleAiConfig roleConfig(String roleCode) {
        String normalizedRole = normalize(roleCode);
        RoleAiConfig config = roleConfigs.get(normalizedRole);
        if (config == null || !config.enabled()) {
            throw new BusinessException("未启用的 AI 角色配置：" + normalizedRole);
        }
        return config;
    }

    public Map<String, RoleAiConfig> roleConfigs() {
        return roleConfigs;
    }

    private static Map<String, RoleAiConfig> defaultRoleConfigs() {
        Map<String, RoleAiConfig> configs = new LinkedHashMap<>();
        configs.put("REGISTRATION", new RoleAiConfig(
                "REGISTRATION",
                "挂号医生",
                "deepseek",
                "deepseek-chat",
                "DEEPSEEK_API_KEY_REGISTRATION",
                true
        ));
        configs.put("OUTPATIENT", new RoleAiConfig(
                "OUTPATIENT",
                "门诊医生",
                "simulated",
                "simulated-outpatient-ai",
                "AI_KEY_OUTPATIENT",
                true
        ));
        configs.put("EXAM", new RoleAiConfig(
                "EXAM",
                "检查医生",
                "simulated",
                "simulated-exam-ai",
                "AI_KEY_EXAM",
                true
        ));
        configs.put("LAB", new RoleAiConfig(
                "LAB",
                "检验医生",
                "simulated",
                "simulated-lab-ai",
                "AI_KEY_LAB",
                true
        ));
        configs.put("PHARMACY", new RoleAiConfig(
                "PHARMACY",
                "药房医生",
                "simulated",
                "simulated-pharmacy-ai",
                "AI_KEY_PHARMACY",
                true
        ));
        return configs;
    }

    static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
