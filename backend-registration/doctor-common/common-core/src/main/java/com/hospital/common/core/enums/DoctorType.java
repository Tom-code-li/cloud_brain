package com.hospital.common.core.enums;

public enum DoctorType {
    REGISTRATION("挂号医生"),
    OUTPATIENT("门诊医生"),
    EXAM("检查医生"),
    LAB("检验医生"),
    PHARMACY("药房医生");

    private final String description;

    DoctorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
