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
 * 管理端会议室维护：分页查询、新增、编辑、删除。
 *
 * 与用户端 RoomController 的区别是这里不限制状态，停用的会议室也要能查看和维护。
 * 删除有前置保护：存在历史预定的会议室不允许物理删除，只能改为停用，否则预定表里的 room_id 会指向不存在的记录。
 */
@RestController
@RequestMapping("/api/admin/rooms")
public class AdminRoomController {

    @Resource
    private RoomService roomService;

    /**
     * 分页查询全部会议室，含停用的。
     */
    @GetMapping
    public Result<PageResult<MeetingRoom>> page(RoomQueryDTO query) {
        return Result.success(roomService.pageForAdmin(query));
    }

    /**
     * 新增会议室。状态未填时默认启用。
     */
    @PostMapping
    public Result<Long> save(@RequestBody @Valid RoomSaveDTO dto) {
        return Result.success("会议室已新增", roomService.save(dto));
    }

    /**
     * 编辑会议室信息。历史预定不受影响，不会按新容量回头校验原有记录。
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid RoomSaveDTO dto) {
        roomService.update(id, dto);
        return Result.success("会议室已更新", null);
    }

    /**
     * 删除会议室。有历史预定时会抛业务异常并提示改为停用。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return Result.success("会议室已删除", null);
    }
}
