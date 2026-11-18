package com.example.meetingroom.controller.admin;

import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.common.Result;
import com.example.meetingroom.dto.AuditDTO;
import com.example.meetingroom.dto.BookQueryDTO;
import com.example.meetingroom.service.BookService;
import com.example.meetingroom.vo.BookVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 管理端预定审核：分页查看全部申请、执行通过或驳回。
 * <p>
 * 管理员身份由 AuthInterceptor 按路径前缀 /api/admin/ 统一校验，
 * 所以本类方法体内不再重复判断角色。
 * <p>
 * 审核通过会触发本系统最重要的一次冲突校验，实现细节见 BookService.audit。
 */
@RestController
@RequestMapping("/api/admin/books")
public class AdminBookController {

    @Resource
    private BookService bookService;

    /**
     * 分页查询全部预定，支持按状态、会议室与关键字筛选。
     * 排序为状态升序，待审核的排在最前。
     */
    @GetMapping
    public Result<PageResult<BookVO>> page(BookQueryDTO query) {
        return Result.success(bookService.pageForAdmin(query));
    }

    /**
     * 审核单条申请：通过（status=1）或驳回（status=2，必须填写理由）。
     * 通过时会锁住会议室行并再校验一次冲突，防止两条重叠的待审申请都通过。
     */
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody @Valid AuditDTO dto) {
        bookService.audit(id, dto);
        return Result.success("审核已提交", null);
    }
}
