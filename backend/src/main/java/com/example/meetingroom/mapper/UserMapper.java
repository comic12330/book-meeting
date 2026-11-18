package com.example.meetingroom.mapper;

import com.example.meetingroom.dto.UserQueryDTO;
import com.example.meetingroom.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    /** 登录与注册重名检查用，username 在表上是唯一索引，LIMIT 1 只是防止意外多行 */
    User findByUsername(@Param("username") String username);

    /** 按主键取用户，拦截器每次请求都会调用它回查账号状态 */
    User findById(@Param("id") Long id);

    /** 新增账号，回填自增主键 */
    int insert(User user);

    /** 自助修改资料：只写姓名与电话两列，即使实体里带了 role 也不会生效 */
    int updateProfile(User user);

    /** 改密：传入的已是加密后的值 */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /** 管理端列表查询，条件为空时返回全部 */
    List<User> selectByCondition(UserQueryDTO query);

    /** 管理端编辑：可改姓名、电话、角色与状态，账号与密码不在其中 */
    int updateByAdmin(User user);

    /** 单改启用状态，停用后下一次请求就会被拦截器拒掉 */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 统计除指定用户外、仍处于启用状态的管理员数量，用于防止把最后一个管理员停用或降级 */
    int countOtherActiveAdmin(@Param("excludeId") Long excludeId);
}
