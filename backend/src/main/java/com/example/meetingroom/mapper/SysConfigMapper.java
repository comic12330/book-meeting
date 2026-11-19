package com.example.meetingroom.mapper;

import com.example.meetingroom.entity.SysConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysConfigMapper {

    /** 取全部参数，按 sort_no 升序 */
    List<SysConfig> selectAll();

    /** 只允许改值，不允许增删参数项——参数键是代码里写死的契约 */
    int updateValue(@Param("configKey") String configKey, @Param("configValue") String configValue);
}
