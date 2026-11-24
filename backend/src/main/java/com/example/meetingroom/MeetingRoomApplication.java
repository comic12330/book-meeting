package com.example.meetingroom;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类。
 *
 * {@code @EnableScheduling} 用于开启定时任务，当前只有一项：把使用时间已结束的预定自动置为「已完成」，见{@code com.example.meetingroom.task.BookStatusTask}。
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.example.meetingroom.mapper")
public class MeetingRoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingRoomApplication.class, args);
    }
}
