package com.hospital.ai.common;

import java.util.List;
import java.util.Map;

public abstract class AbstractRoleAiAssistant implements RoleAiAssistant {
    protected static final String SAFETY_PREFIX = "AI 辅助内容仅供医生参考，最终结论必须由对应医生确认。";

    private final RoleAiConfigRegistry configRegistry;
    private final String roleCode;
    private final List<String> supportedScenes;

    protected AbstractRoleAiAssistant(
            RoleAiConfigRegistry configRegistry,
            String roleCode,
            List<String> supportedScenes
    ) {
        this.configRegistry = configRegistry;
        this.roleCode = roleCode;
        this.supportedScenes = List.copyOf(supportedScenes);
    }

    @Override
    public String roleCode() {
        return roleCode;
    }

    @Override
    public List<String> supportedScenes() {
        return supportedScenes;
    }

    @Override
    public List<String> assist(RoleAiRequest request) {
        RoleAiConfig config = configRegistry.roleConfig(roleCode);
        String sceneCode = RoleAiConfigRegistry.normalize(request.sceneCode());
        String context = stringifyContext(request.contextData());
        return List.of(
                SAFETY_PREFIX,
                "当前 AI 配置：" + config.roleName() + " / " + config.modelName() + " / " + config.apiKeyRef() + "。",
                buildScenePrompt(sceneCode, context)
        );
    }

    protected abstract String buildScenePrompt(String sceneCode, String context);

    protected String stringifyContext(Map<String, Object> contextData) {
        return String.valueOf(contextData == null ? Map.of() : contextData);
    }
}
