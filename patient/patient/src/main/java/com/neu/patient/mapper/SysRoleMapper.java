package com.neu.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.patient.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    SysRole findByRoleCode(String roleCode);
}
