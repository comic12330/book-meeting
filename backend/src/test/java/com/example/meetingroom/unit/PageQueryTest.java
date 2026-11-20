package com.example.meetingroom.unit;

import com.example.meetingroom.common.AppProperties;
import com.example.meetingroom.common.PageQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 分页参数防护的单元测试。
 * <p>
 * {@link PageQuery} 的上下限来自配置文件，这里显式写回静态字段，避免依赖容器是否启动过。
 * 守的是两个防护点：非法页码不能把 PageHelper 弄崩，超大 pageSize 不能把整表拉出来。
 */
class PageQueryTest {

    private static final int DEFAULT_SIZE = 10;

    private static final int MAX_SIZE = 100;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        properties.setDefaultPageSize(DEFAULT_SIZE);
        properties.setMaxPageSize(MAX_SIZE);
    }

    @Test
    @DisplayName("页码为空或小于 1 时按第 1 页处理")
    void safePageNum() {
        PageQuery query = new PageQuery();
        assertEquals(1, query.safePageNum(), "默认页码应为 1");

        query.setPageNum(0);
        assertEquals(1, query.safePageNum());

        query.setPageNum(-5);
        assertEquals(1, query.safePageNum());

        query.setPageNum(3);
        assertEquals(3, query.safePageNum());
    }

    @Test
    @DisplayName("单页条数为空时取配置默认值")
    void safePageSizeUsesDefault() {
        assertEquals(DEFAULT_SIZE, new PageQuery().safePageSize());
    }

    @Test
    @DisplayName("单页条数超过上限时按上限截断")
    void safePageSizeTruncated() {
        PageQuery query = new PageQuery();
        query.setPageSize(1000);

        assertEquals(MAX_SIZE, query.safePageSize(),
                "不截断会让前端一次拉走整张表");
    }

    @Test
    @DisplayName("单页条数为 0 或负数时回落到默认值")
    void safePageSizeWithIllegalValue() {
        PageQuery query = new PageQuery();
        query.setPageSize(0);
        assertEquals(DEFAULT_SIZE, query.safePageSize());

        query.setPageSize(-1);
        assertEquals(DEFAULT_SIZE, query.safePageSize());
    }

    @Test
    @DisplayName("合法范围内的单页条数原样返回")
    void safePageSizeWithinRange() {
        PageQuery query = new PageQuery();
        query.setPageSize(20);
        assertEquals(20, query.safePageSize());

        query.setPageSize(MAX_SIZE);
        assertEquals(MAX_SIZE, query.safePageSize());
    }
}
