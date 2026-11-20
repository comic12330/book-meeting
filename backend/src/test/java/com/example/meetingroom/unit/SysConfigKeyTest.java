package com.example.meetingroom.unit;

import com.example.meetingroom.common.SysConfigKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 业务参数定义的单元测试。
 * <p>
 * {@link SysConfigKey} 是参数校验的唯一事实来源：后端的保存校验和前端输入控件的范围
 * 都从这里取。它一旦写错（比如默认值落在自己的范围外、键名重复），会出现
 * 「页面能填、后端拒绝」或者「初始化脚本写入的值一开始就不合法」，所以这里守三条：
 * 键名唯一、默认值合法、类型与默认值形态匹配。
 */
class SysConfigKeyTest {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Test
    @DisplayName("键名唯一，且大小写敏感")
    void keysAreUniqueAndCaseSensitive() {
        Set<String> keys = new HashSet<>();
        for (SysConfigKey item : SysConfigKey.values()) {
            assertTrue(keys.add(item.getKey()), "参数键重复：" + item.getKey());
            assertEquals(item, SysConfigKey.of(item.getKey()));
        }
        assertNull(SysConfigKey.of("BOOKING.MAX-HOURS"), "键名区分大小写，不能模糊匹配");
        assertNull(SysConfigKey.of("booking.not-exists"));
        assertNull(SysConfigKey.of(null));
    }

    @Test
    @DisplayName("整数型参数的默认值落在自己声明的范围内")
    void defaultValuesWithinRange() {
        for (SysConfigKey item : SysConfigKey.values()) {
            if (item.getType() != SysConfigKey.ValueType.INT) {
                continue;
            }
            int value = Integer.parseInt(item.getDefaultValue());
            assertTrue(value >= item.getMin() && value <= item.getMax(),
                    item.getKey() + " 的默认值 " + value + " 不在 [" + item.getMin()
                            + ", " + item.getMax() + "] 之间");
        }
    }

    @Test
    @DisplayName("时间型参数的默认值是合法的 HH:mm")
    void timeDefaultIsParsable() {
        for (SysConfigKey item : SysConfigKey.values()) {
            if (item.getType() != SysConfigKey.ValueType.TIME) {
                continue;
            }
            assertNotNull(LocalTime.parse(item.getDefaultValue(), TIME_FORMAT),
                    item.getKey() + " 的默认值不是合法时刻");
        }
    }

    @Test
    @DisplayName("可预定窗口的默认值起始早于结束")
    void defaultDailyWindowIsOrdered() {
        LocalTime start = LocalTime.parse(
                SysConfigKey.BOOKING_DAILY_START.getDefaultValue(), TIME_FORMAT);
        LocalTime end = LocalTime.parse(
                SysConfigKey.BOOKING_DAILY_END.getDefaultValue(), TIME_FORMAT);

        assertTrue(start.isBefore(end), "默认窗口起止颠倒时，任何预定都会被判定为不在窗口内");
    }

    @Test
    @DisplayName("每个参数都有名称与说明，可直接用于管理页展示")
    void metadataIsPresent() {
        for (SysConfigKey item : SysConfigKey.values()) {
            assertTrue(!item.getName().isEmpty(), item.getKey() + " 缺少名称");
            assertTrue(!item.getRemark().isEmpty(), item.getKey() + " 缺少说明");
        }
    }
}
