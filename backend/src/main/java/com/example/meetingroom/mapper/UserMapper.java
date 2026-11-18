package com.example.meetingroom.mapper;

import com.example.meetingroom.dto.UserQueryDTO;
import com.example.meetingroom.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    User findByUsername(@Param("username") String username);

    User findById(@Param("id") Long id);

    int insert(User user);

    int updateProfile(User user);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /** 管理端列表查询，条件为空时返回全部 */
    List<User> selectByCondition(UserQueryDTO query);

    /** 管理端编辑：只允许改姓名、电话、角色，账号不可变 */
    int updateByAdmin(User user);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 统计除指定用户外、仍处于启用状态的管理员数量，用于防止把最后一个管理员停用或降级 */
    int countOtherActiveAdmin(@Param("excludeId") Long excludeId);
}
