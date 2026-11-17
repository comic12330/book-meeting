package com.example.meetingroom.controller;

import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.common.Result;
import com.example.meetingroom.common.UserContext;
import com.example.meetingroom.dto.BookQueryDTO;
import com.example.meetingroom.dto.BookSubmitDTO;
import com.example.meetingroom.service.BookService;
import com.example.meetingroom.vo.BookVO;
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
 * 用户端预定操作。
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Resource
    private BookService bookService;

    @PostMapping
    public Result<Long> submit(@RequestBody @Valid BookSubmitDTO dto) {
        Long id = bookService.submit(dto, UserContext.userId());
        return Result.success("预定申请已提交，等待管理员审核", id);
    }

    @GetMapping("/my")
    public Result<PageResult<BookVO>> myBooks(BookQueryDTO query) {
        return Result.success(bookService.pageMyBooks(query, UserContext.userId()));
    }

    @GetMapping("/{id}")
    public Result<BookVO> detail(@PathVariable Long id) {
        return Result.success(bookService.getVO(id));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        bookService.cancel(id, UserContext.userId());
        return Result.success("预定已取消", null);
    }
}
