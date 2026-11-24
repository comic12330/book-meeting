package com.example.meetingroom.task;

import com.example.meetingroom.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 预定状态定时任务：把使用时间已结束的预定自动置为「已完成」。
 *
 * 本类只承担「什么时候跑」，业务逻辑在 {@link BookService#completeExpired()}，管理端的手动触发接口调用的是同一个方法。
 * 扫描间隔由 {@code meeting.task.complete-expired-cron} 配置，默认每 5 分钟一次。
 * 之所以不设成每分钟：状态从「已通过」变为「已完成」晚几分钟没有任何业务影响，而更短的间隔只是徒增无用查询。
 * 异常必须在这里吃掉：定时任务抛异常虽然不会终止后续调度（Spring 默认记录日志后继续下一轮），但会输出整段堆栈，把真正的问题淹没。这里只记一行错误，其余交给调用方排查。
 */
@Component
public class BookStatusTask {

    private static final Logger log = LoggerFactory.getLogger(BookStatusTask.class);

    @Resource
    private BookService bookService;

    /**
     * 周期扫描并更新。没有记录被处理时不打日志，避免每轮都刷一行无用输出。
     */
    @Scheduled(cron = "${meeting.task.complete-expired-cron}")
    public void completeExpiredBooks() {
        try {
            int rows = bookService.completeExpired();
            if (rows > 0) {
                log.info("定时任务：{} 条已结束的预定已置为「已完成」", rows);
            }
        } catch (Exception e) {
            log.error("定时任务执行失败：更新已结束预定状态", e);
        }
    }
}
