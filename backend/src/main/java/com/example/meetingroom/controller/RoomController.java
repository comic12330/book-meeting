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
 *
 * 列表只返回启用中的会议室（status = 1），停用的不会出现在预定候选里；
 * 详情不限制状态，便于已预定过的用户回看会议室信息。
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Resource
    private RoomService roomService;

    /**
     * 分页查询可用会议室，支持按容纳人数、位置与设备条件筛选。
     */
    @GetMapping
    public Result<PageResult<MeetingRoom>> page(RoomQueryDTO query) {
        return Result.success(roomService.pageForUser(query));
    }

    /**
     * 查看会议室详情。
     */
    @GetMapping("/{id}")
    public Result<MeetingRoom> detail(@PathVariable Long id) {
        return Result.success(roomService.get(id));
    }
}
