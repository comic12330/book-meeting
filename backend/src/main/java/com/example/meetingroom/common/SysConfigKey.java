package com.example.meetingroom.common;

/**
 * 可在线维护的业务参数定义。
 * <p>
 * 每一项都在 sys_config 表里有一行对应记录，管理员可以在「系统参数」页面直接改值，
 * 改完即时生效、无需重启。这里的枚举承担三件事：
 * 集中声明参数键与默认值、限定取值范围、给管理页面提供名称与单位。
 * <p>
 * 分成「配置文件」与「数据表」两层的判断依据：
 * 影响全站业务规则、需要运营随时调整的（如时长上限、可预定窗口）放数据表；
 * 与部署环境或安全相关的技术参数（如令牌密钥、分页上限）放 application.yml，不给管理员改。
 */
public enum SysConfigKey {

    /** 单次预定最长时长，单位小时 */
    BOOKING_MAX_HOURS("booking.max-hours", "单次预定最长时长", "小时", "8",
            ValueType.INT, 1, 12, 10,
            "一场会议最多能占用会议室多久"),

    /** 最多可以提前多少天预定 */
    BOOKING_MAX_ADVANCE_DAYS("booking.max-advance-days", "最多提前预定天数", "天", "30",
            ValueType.INT, 0, 30, 20,
            "使用者最多能提前多久预约，设为 30 表示只能选 30 天以内的日期；设为 0 表示只接受当天预约"),

    /** 每日最早可预定时刻 */
    BOOKING_DAILY_START("booking.daily-start", "每日可预定起始时间", "", "08:00",
            ValueType.TIME, 0, 0, 30,
            "每天最早从几点起可以使用会议室，早于该时刻的申请不予受理"),

    /** 每日最晚可开始的时刻（结束时间可跨天，不受此限制） */
    BOOKING_DAILY_END("booking.daily-end", "每日最晚开始时间", "", "22:00",
            ValueType.TIME, 0, 0, 40,
            "每天最晚可以从几点起使用会议室；跨夜会议也须在此刻之前开始，结束时间不限"),

    /** 会议室容纳人数上限，防止录入离谱数值 */
    ROOM_MAX_CAPACITY("room.max-capacity", "会议室容纳人数上限", "人", "200",
            ValueType.INT, 1, 200, 50,
            "登记会议室时允许填写的最大人数，用于拦住明显录错的数字");

    /** 参数值的类型，决定校验方式与前端输入控件 */
    public enum ValueType {
        INT,
        TIME
    }

    private final String key;
    private final String name;
    private final String unit;
    private final String defaultValue;
    private final ValueType type;
    private final int min;
    private final int max;
    private final int sortNo;
    private final String remark;

    SysConfigKey(String key, String name, String unit, String defaultValue,
                 ValueType type, int min, int max, int sortNo, String remark) {
        this.key = key;
        this.name = name;
        this.unit = unit;
        this.defaultValue = defaultValue;
        this.type = type;
        this.min = min;
        this.max = max;
        this.sortNo = sortNo;
        this.remark = remark;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public ValueType getType() {
        return type;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getSortNo() {
        return sortNo;
    }

    public String getRemark() {
        return remark;
    }

    /** 按 key 反查枚举，找不到返回 null（用于管理端提交时校验参数是否合法） */
    public static SysConfigKey of(String key) {
        for (SysConfigKey item : values()) {
            if (item.key.equals(key)) {
                return item;
            }
        }
        return null;
    }
}
