package com.neu.patient.service;

import com.neu.patient.entity.Department;
import com.neu.patient.mapper.DepartmentMapper;
import com.neu.patient.mapper.DoctorMapper;
import com.neu.patient.mapper.ExamLabReportMapper;
import com.neu.patient.mapper.FeeOrderMapper;
import com.neu.patient.mapper.MedicalRecordMapper;
import com.neu.patient.mapper.PrescriptionMapper;
import com.neu.patient.mapper.RegistrationMapper;
import com.neu.patient.service.impl.AiKnowledgeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiKnowledgeServiceImplTest {

    @Test
    void guideCandidatesIncludeEveryActiveDepartment() {
        AiKnowledgeServiceImpl service = new AiKnowledgeServiceImpl();
        DepartmentMapper departmentMapper = mock(DepartmentMapper.class);
        ReflectionTestUtils.setField(service, "departmentMapper", departmentMapper);
        ReflectionTestUtils.setField(service, "doctorMapper", mock(DoctorMapper.class));
        ReflectionTestUtils.setField(service, "medicalRecordMapper", mock(MedicalRecordMapper.class));
        ReflectionTestUtils.setField(service, "examLabReportMapper", mock(ExamLabReportMapper.class));
        ReflectionTestUtils.setField(service, "prescriptionMapper", mock(PrescriptionMapper.class));
        ReflectionTestUtils.setField(service, "feeOrderMapper", mock(FeeOrderMapper.class));
        ReflectionTestUtils.setField(service, "registrationMapper", mock(RegistrationMapper.class));

        List<Department> departments = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Department department = new Department();
            department.setDeptId((long) i);
            department.setDeptName("测试科室" + i);
            department.setDescription("科室说明" + i);
            departments.add(department);
        }
        when(departmentMapper.findAllActive()).thenReturn(departments);

        List<String> candidates = service.buildCandidates(1L, "guide", "头晕三天");

        assertThat(candidates)
                .filteredOn(item -> item.startsWith("科室:"))
                .hasSize(12)
                .anySatisfy(item -> assertThat(item).contains("测试科室12"));
    }
}
