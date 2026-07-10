package com.hospital.ai.common;

import java.util.List;

public interface RoleAiAssistant {
    String roleCode();

    List<String> supportedScenes();

    List<String> assist(RoleAiRequest request);
}
