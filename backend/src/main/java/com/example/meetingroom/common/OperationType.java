package com.example.meetingroom.common;

/**
 * 操作类型字典。
 *
 * 只收录管理端的数据变更动作，即增、删、改。普通用户的提交、取消、改资料不在其中：
 * 本功能的安全目标是防止管理员误改或恶意破坏全站数据，不是做用户行为分析；
 * 而用户自己的操作在 meeting_book、user 表里本来就带 user_id 与时间戳，重复记录没有信息量。
 * 与 {@link BookStatus} 的处理方式一致：常量集中在这里，日志表里存 code，页面按 code 翻译，
 * 枚举只增不改，一旦改 code 就会让历史日志翻译不出来。
 * module 用于页面上的模块筛选。不单独建一份模块表，是因为「哪些类型属于哪个模块」
 * 是这份枚举的派生信息，另存一处只会多出两份可能互相矛盾的口径。
 */
public enum OperationType {

    /** 会议室 */
    ROOM_CREATE(Module.ROOM, "新增会议室"),
    ROOM_UPDATE(Module.ROOM, "编辑会议室"),
    ROOM_DELETE(Module.ROOM, "删除会议室"),

    /** 预定审核 */
    BOOK_AUDIT_APPROVE(Module.BOOK, "审核通过预定"),
    BOOK_AUDIT_REJECT(Module.BOOK, "审核驳回预定"),
    BOOK_CANCEL_ADMIN(Module.BOOK, "取消他人预定"),

    /** 用户管理 */
    USER_CREATE(Module.USER, "新增账号"),
    USER_UPDATE(Module.USER, "编辑账号"),
    USER_STATUS(Module.USER, "启用或停用账号"),
    USER_PASSWORD_RESET(Module.USER, "重置账号密码"),

    /** 公告管理 */
    NOTICE_CREATE(Module.NOTICE, "发布公告"),
    NOTICE_UPDATE(Module.NOTICE, "编辑公告"),
    NOTICE_DELETE(Module.NOTICE, "删除公告"),

    /** 系统参数 */
    CONFIG_UPDATE(Module.CONFIG, "修改系统参数");

    /**
     * 模块。name() 直接作为 operation_log.module 的取值，
     * 中文名只用于展示，因此改动展示文案不会影响历史数据。
     */
    public enum Module {
        ROOM("会议室"),
        BOOK("预定审核"),
        USER("用户管理"),
        NOTICE("公告管理"),
        CONFIG("系统参数");

        private final String label;

        Module(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        /** 按取值反查模块，用于把前端传来的筛选条件转成枚举；找不到返回 null */
        public static Module of(String name) {
            for (Module item : values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
            return null;
        }
    }

    private final Module module;

    private final String label;

    OperationType(Module module, String label) {
        this.module = module;
        this.label = label;
    }

    public Module getModule() {
        return module;
    }

    public String getLabel() {
        return label;
    }
}
