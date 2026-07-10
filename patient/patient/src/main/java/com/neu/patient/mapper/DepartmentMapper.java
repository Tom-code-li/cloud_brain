package com.neu.patient.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.patient.entity.Department;
import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    @Select("SELECT * FROM department WHERE status = 1 ORDER BY COALESCE(parent_id, dept_id), parent_id IS NOT NULL, sort_order, dept_id")
    List<Department> findAllActive();
}
