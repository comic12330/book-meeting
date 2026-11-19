package com.example.meetingroom.controller.admin;

import com.example.meetingroom.common.BookStatus;
import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.common.Result;
import com.example.meetingroom.dto.AuditDTO;
import com.example.meetingroom.dto.BookQueryDTO;
import com.example.meetingroom.dto.CancelDTO;
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
 * 管理端预定审核：分页查看全部申请、执行通过或驳回、取消他人的预定。
 * <p>
 * 管理员身份由 AuthInterceptor 按路径前缀 /api/admin/ 统一校验，
 * 所以本类方法体内不再重复判断角色。
 * <p>
 * 审核通过会触发本系统最重要的一次冲突校验，实现细节见 BookService.approve。
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
     * <p>
     * 这里只做分派，校验仍在 Service 里：合法取值只有 1 和 2，传其它值会落到 approve，
     * 由它抛出「审核结果只能是通过或驳回」，与拆分前行为一致。
     * 拆成两个入口是为了让操作日志能区分「谁批的」与「谁驳的」。
     */
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody @Valid AuditDTO dto) {
        if (Integer.valueOf(BookStatus.REJECTED).equals(dto.getStatus())) {
            bookService.reject(id, dto);
        } else {
            bookService.approve(id, dto);
        }
        return Result.success("审核已提交", null);
    }

    /**
     * 取消他人的预定（行政取消），理由必填。
     * <p>
     * 管理员此前也能取消别人的预定，但走的是用户端接口，既没有页面入口也没有任何痕迹。
     * 现在收口到这里：路径受拦截器保护、理由落库供当事人查看、动作记入操作日志。
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestBody @Valid CancelDTO dto) {
        bookService.adminCancel(id, dto);
        return Result.success("已取消该预定", null);
    }

    /**
     * 手动触发一次「已结束预定转已完成」的扫描。
     * <p>
     * 定时任务每 5 分钟自动跑一次，本接口不是业务功能，存在的唯一原因是方便验证与演示：
     * 不必干等调度窗口，也不用临时改 cron 表达式重打包。返回本次更新的记录条数，
     * 连点两次第二次必然返回 0，可用来验证幂等。
     */
    @PostMapping("/complete-expired")
    public Result<Integer> completeExpired() {
        return Result.success("扫描完成", bookService.completeExpired());
    }
}
