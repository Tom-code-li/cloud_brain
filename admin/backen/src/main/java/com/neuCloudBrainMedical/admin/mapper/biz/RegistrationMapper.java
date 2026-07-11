package com.neuCloudBrainMedical.admin.mapper.biz;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neuCloudBrainMedical.admin.entity.Registration;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RegistrationMapper extends BaseMapper<Registration> {

	@Select("<script>SELECT patient_id, user_id FROM patient WHERE patient_id IN "
			+ "<foreach collection='patientIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
	@MapKey("patient_id")
	Map<Long, Map<String, Object>> getUserIdMapByPatientIds(@Param("patientIds") List<Long> patientIds);
}
