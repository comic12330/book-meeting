package com.example.meetingroom.controller;

import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.common.Result;
import com.example.meetingroom.dto.RoomQueryDTO;
import com.example.meetingroom.entity.MeetingRoom;
import com.example.meetingroom.service.RoomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端会议室查询。
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Resource
    private RoomService roomService;

    @GetMapping
    public Result<PageResult<MeetingRoom>> page(RoomQueryDTO query) {
        return Result.success(roomService.pageForUser(query));
    }

    @GetMapping("/{id}")
    public Result<MeetingRoom> detail(@PathVariable Long id) {
        return Result.success(roomService.get(id));
    }
}
