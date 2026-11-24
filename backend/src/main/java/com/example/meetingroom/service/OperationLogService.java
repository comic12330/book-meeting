package com.example.meetingroom.service;

import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.dto.LogQueryDTO;
import com.example.meetingroom.entity.OperationLog;
import com.example.meetingroom.mapper.OperationLogMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 操作日志的写入与查询。
 *
 * 写入用 {@link Propagation#REQUIRES_NEW} 独立事务，这一点是必须的：业务成功时，日志由切面在事务提交之后才调用，若与业务同事务会找不到可加入的事务；业务失败时，外层事务会被回滚，同事务写的日志会被一起回滚，而「失败记录」恰恰是最该留下的痕迹。
 * 挂起当前事务、另起一个短事务插入，两种情况都能落库。
 */
@Service
public class OperationLogService {

    @Resource
    private OperationLogMapper logMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void record(OperationLog log) {
        logMapper.insert(log);
    }

    /** 管理端：分页查询，按操作时间倒序 */
    public PageResult<OperationLog> page(LogQueryDTO query) {
        PageHelper.startPage(query.safePageNum(), query.safePageSize());
        List<OperationLog> list = logMapper.selectByCondition(query);
        return PageResult.of(new PageInfo<>(list).getTotal(), list);
    }
}
