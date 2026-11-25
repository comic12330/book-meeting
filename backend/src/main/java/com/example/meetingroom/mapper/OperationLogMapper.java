package com.example.meetingroom.mapper;

import com.example.meetingroom.dto.LogQueryDTO;
import com.example.meetingroom.entity.OperationLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志数据访问。
 *
 * 只有插入与查询两个方法：审计日志不提供更新与删除，一旦可以改，追查价值就没了。
 */
public interface OperationLogMapper {

    /** 新增一条操作日志 */
    int insert(OperationLog log);

    /** 管理端按条件查询日志，条件为空时返回全部 */
    List<OperationLog> selectByCondition(@Param("query") LogQueryDTO query);
}
