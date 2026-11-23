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
 * 用户端预定操作：提交申请、查我的预定、看详情、取消。
 *
 * 四个接口都在拦截器保护范围内。当前用户 ID 一律取自 UserContext（由令牌解析得到），
 * 不从请求参数接收，否则改个 userId 就能替别人提交或取消。
 * 前端的时间选择器只负责交互体验，冲突判定完全由后端完成，见 BookService 的说明。
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Resource
    private BookService bookService;

    /**
     * 提交预定申请，写入一条待审核记录。
     * 会议室是否可用、时间是否合法、人数是否超容、时段是否冲突，都在服务层依次校验。
     *
     * @return 新记录的 id
     */
    @PostMapping
    public Result<Long> submit(@RequestBody @Valid BookSubmitDTO dto) {
        Long id = bookService.submit(dto, UserContext.userId());
        return Result.success("预定申请已提交，等待管理员审核", id);
    }

    /**
     * 分页查询本人预定记录，可按状态筛选。
     */
    @GetMapping("/my")
    public Result<PageResult<BookVO>> myBooks(BookQueryDTO query) {
        return Result.success(bookService.pageMyBooks(query, UserContext.userId()));
    }

    /**
     * 查看单条预定详情。服务层会校验归属，非本人且非管理员返回 403。
     */
    @GetMapping("/{id}")
    public Result<BookVO> detail(@PathVariable Long id) {
        return Result.success(bookService.getVO(id));
    }

    /**
     * 取消预定。只有尚未开始、且状态为待审核或已通过的记录可以取消；
     * 取消后该时段立即释放，可以重新被别人预定。
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        bookService.cancel(id, UserContext.userId());
        return Result.success("预定已取消", null);
    }
}
