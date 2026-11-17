package com.example.meetingroom.mapper;

import com.example.meetingroom.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    User findByUsername(@Param("username") String username);

    User findById(@Param("id") Long id);

    int insert(User user);

    int updateProfile(User user);

    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
