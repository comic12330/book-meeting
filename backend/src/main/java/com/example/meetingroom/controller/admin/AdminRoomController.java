package com.example.meetingroom.controller.admin;

import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.common.Result;
import com.example.meetingroom.dto.RoomQueryDTO;
import com.example.meetingroom.dto.RoomSaveDTO;
import com.example.meetingroom.entity.MeetingRoom;
import com.example.meetingroom.service.RoomService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 管理端会议室维护。
 */
@RestController
@RequestMapping("/api/admin/rooms")
public class AdminRoomController {

    @Resource
    private RoomService roomService;

    @GetMapping
    public Result<PageResult<MeetingRoom>> page(RoomQueryDTO query) {
        return Result.success(roomService.pageForAdmin(query));
    }

    @PostMapping
    public Result<Long> save(@RequestBody @Valid RoomSaveDTO dto) {
        return Result.success("会议室已新增", roomService.save(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid RoomSaveDTO dto) {
        roomService.update(id, dto);
        return Result.success("会议室已更新", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return Result.success("会议室已删除", null);
    }
}
