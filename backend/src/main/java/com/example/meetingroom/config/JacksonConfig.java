package com.example.meetingroom.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * 统一 LocalDateTime 的出入参格式，避免前端拿到 ISO 的 "2026-11-18T09:00:00" 还要二次处理。
 * <p>
 * 输出与输入的宽容度刻意做成不对称：
 * <ul>
 *   <li><b>输出固定带秒</b>，仍是 yyyy-MM-dd HH:mm:ss，所有响应格式保持一致；</li>
 *   <li><b>输入允许省略秒</b>，预定表单的时间精度只到分钟，前端传的是 "2026-11-20 10:30"。
 *       同一个解析器也兼容带秒的写法（省略秒时补 0），不会因为少三个字符就返回 400。</li>
 * </ul>
 */
@Configuration
public class JacksonConfig {

    /** 输出格式：响应里的时间统一精确到秒 */
    private static final DateTimeFormatter OUTPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 输入格式：秒可省略。
     * optionalStart 段匹配不上就跳过，再由 parseDefaulting 把秒补成 0。
     */
    private static final DateTimeFormatter INPUT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm")
            .optionalStart().appendPattern(":ss").optionalEnd()
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localDateTimeCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(OUTPUT));
            module.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonParser parser, DeserializationContext context)
                        throws IOException {
                    String text = parser.getText();
                    if (text == null || text.trim().isEmpty()) {
                        return null;
                    }
                    return LocalDateTime.parse(text.trim(), INPUT);
                }
            });
            builder.modules(module);
        };
    }
}
