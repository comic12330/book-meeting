package com.example.meetingroom.service;

import com.example.meetingroom.annotation.LogOperation;
import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.common.OperationType;
import com.example.meetingroom.common.SysConfigKey;
import com.example.meetingroom.entity.SysConfig;
import com.example.meetingroom.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 业务参数读取与维护。
 * <p>
 * 设计要点：
 * <ul>
 *   <li><b>读取走内存缓存</b>：参数在每个请求的校验里都要用（时长上限、可预定窗口），
 *       每次都查库没必要。首次访问时整表加载进 ConcurrentHashMap，之后只读内存。</li>
 *   <li><b>改完即时生效</b>：管理员保存后直接回写缓存，不需要重启服务，也不必等缓存过期。
 *       这一点是"参数放进数据表"相对"写死在代码里"的核心收益。</li>
 *   <li><b>缺失或为空时回落默认值</b>：库里少了某一行不该让下单流程直接崩，
 *       用枚举里声明的默认值兜底，保证功能可用。</li>
 *   <li><b>只在管理页面显式刷新</b>：`list()` 每次从库读并重置缓存。
 *       如果绕过系统直接改数据库，需要打开一次参数页或重启才会生效，属已知边界。</li>
 * </ul>
 */
@Service
public class SysConfigService {

    /** 时间型参数的格式：HH:mm */
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    private volatile boolean loaded = false;

    @Resource
    private SysConfigMapper configMapper;

    /** 整数型参数 */
    public int getInt(SysConfigKey key) {
        String value = raw(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // 库里被人手改成非法值时退回默认值，不因一个参数让业务不可用
            return Integer.parseInt(key.getDefaultValue());
        }
    }

    public long getLong(SysConfigKey key) {
        return getInt(key);
    }

    /** 时间型参数，返回时分 */
    public LocalTime getTime(SysConfigKey key) {
        String value = raw(key);
        try {
            return LocalTime.parse(value, TIME_FORMATTER);
        } catch (RuntimeException e) {
            return LocalTime.parse(key.getDefaultValue(), TIME_FORMATTER);
        }
    }

    /** 供公开接口返回，前端据此限制日期范围与人数输入 */
    public Map<String, String> publicMap() {
        ensureLoaded();
        Map<String, String> map = new LinkedHashMap<>();
        for (SysConfigKey key : SysConfigKey.values()) {
            map.put(key.getKey(), raw(key));
        }
        return map;
    }

    /** 管理页列表：每次从库读，顺带把缓存刷新到最新 */
    public List<SysConfig> list() {
        List<SysConfig> list = configMapper.selectAll();
        cache.clear();
        for (SysConfig item : list) {
            cache.put(item.getConfigKey(), item.getConfigValue());
            fillMeta(item);
        }
        loaded = true;
        return list;
    }

    /**
     * 修改单个参数。只允许改值，不允许增删参数项。
     * <p>
     * 日志描述记「参数名 改为 新值」而不是键名：键名是实现细节，事后追查的人看的是
     * 「单次预定最长时长被改成了 24」，不是 booking.max-hours。
     */
    @Transactional(rollbackFor = Exception.class)
    @LogOperation(type = OperationType.CONFIG_UPDATE,
            target = "describe(#configKey) + ' 改为 ' + #configValue")
    public void update(String configKey, String configValue) {
        SysConfigKey def = SysConfigKey.of(configKey);
        if (def == null) {
            throw new BusinessException(400, "未知的参数项：" + configKey);
        }
        String normalized = validate(def, configValue);
        checkCrossRule(def, normalized);
        if (configMapper.updateValue(configKey, normalized) == 0) {
            throw new BusinessException(1001, "参数项不存在，请检查数据库初始化是否完整");
        }
        cache.put(configKey, normalized);
    }

    /**
     * 供操作日志取参数名称，如「单次预定最长时长」。
     * <p>
     * 由切面的 SpEL 调用（见 {@code @LogOperation(target = "describe(#configKey) + ...")}）。
     * 键名是代码与数据之间的契约，属于实现细节，日志里只留使用者能看懂的名称。
     */
    public String describe(String configKey) {
        SysConfigKey def = SysConfigKey.of(configKey);
        return def == null ? null : def.getName();
    }

    // ------------------------------------------------------------ 私有方法

    private String raw(SysConfigKey key) {
        ensureLoaded();
        String value = cache.get(key.getKey());
        return value == null || value.trim().isEmpty() ? key.getDefaultValue() : value;
    }

    /**
     * 把枚举里声明的类型与取值范围补到实体上，供管理页面渲染对应的输入控件。
     * 这些信息不在表里存一份，避免与代码中的枚举定义出现两份互相矛盾的口径。
     */
    private void fillMeta(SysConfig item) {
        SysConfigKey def = SysConfigKey.of(item.getConfigKey());
        if (def == null) {
            return;
        }
        item.setValueType(def.getType().name());
        if (def.getType() == SysConfigKey.ValueType.INT) {
            item.setMinValue(def.getMin());
            item.setMaxValue(def.getMax());
        }
    }

    /**
     * 类型与范围校验。参数值的合法区间由枚举声明，不在页面或 SQL 里重复写一遍。
     */
    private String validate(SysConfigKey def, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(400, def.getName() + " 不能为空");
        }
        String trimmed = value.trim();
        if (def.getType() == SysConfigKey.ValueType.TIME) {
            if (!TIME_PATTERN.matcher(trimmed).matches()) {
                throw new BusinessException(400, def.getName() + "需为 HH:mm 格式，例如 08:00");
            }
            return trimmed;
        }
        int number;
        try {
            number = Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            throw new BusinessException(400, def.getName() + "需为整数");
        }
        if (number < def.getMin() || number > def.getMax()) {
            throw new BusinessException(400, def.getName() + "需在 " + def.getMin()
                    + " 至 " + def.getMax() + " 之间");
        }
        return String.valueOf(number);
    }

    /**
     * 跨参数校验：可预定窗口的起始时间必须早于最晚开始时间。
     * 两个值分两条记录存，单看任何一条都无法判断大小关系。
     */
    private void checkCrossRule(SysConfigKey def, String normalized) {
        LocalTime start;
        LocalTime end;
        if (def == SysConfigKey.BOOKING_DAILY_START) {
            start = LocalTime.parse(normalized, TIME_FORMATTER);
            end = getTime(SysConfigKey.BOOKING_DAILY_END);
        } else if (def == SysConfigKey.BOOKING_DAILY_END) {
            start = getTime(SysConfigKey.BOOKING_DAILY_START);
            end = LocalTime.parse(normalized, TIME_FORMATTER);
        } else {
            return;
        }
        if (!start.isBefore(end)) {
            throw new BusinessException(400, "可预定起始时间必须早于最晚开始时间");
        }
    }

    /** 首次访问时整表加载，双检锁避免并发重复加载 */
    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        synchronized (this) {
            if (loaded) {
                return;
            }
            for (SysConfig item : configMapper.selectAll()) {
                cache.put(item.getConfigKey(), item.getConfigValue());
            }
            loaded = true;
        }
    }
}
