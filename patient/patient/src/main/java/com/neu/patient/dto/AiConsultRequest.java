package com.neu.patient.dto;

public class AiConsultRequest {
    private Long patientId;
    private String mode;
    private String content;

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
