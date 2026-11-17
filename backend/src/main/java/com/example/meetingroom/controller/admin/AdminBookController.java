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
 * 管理端预定审核。
 */
@RestController
@RequestMapping("/api/admin/books")
public class AdminBookController {

    @Resource
    private BookService bookService;

    @GetMapping
    public Result<PageResult<BookVO>> page(BookQueryDTO query) {
        return Result.success(bookService.pageForAdmin(query));
    }

    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody @Valid AuditDTO dto) {
        bookService.audit(id, dto);
        return Result.success("审核已提交", null);
    }
}
