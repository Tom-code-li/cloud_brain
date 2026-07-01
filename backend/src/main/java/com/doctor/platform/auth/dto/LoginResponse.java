package com.doctor.platform.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String token;
    private String roleCode;
    private String roleName;
    private String workbenchRoute;
    private UserProfile user;

    @Data
    @Builder
    public static class UserProfile {
        private Long userId;
        private Long doctorId;
        private String username;
        private String realName;
        private String doctorNo;
    }
}
