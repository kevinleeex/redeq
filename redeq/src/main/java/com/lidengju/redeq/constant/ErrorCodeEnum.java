package com.lidengju.redeq.constant;

/**
 * Error code enums
 *
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * @date 2021/9/4
 **/
public enum ErrorCodeEnum {
    /**
     * Default redeq error!
     */
    DEFAULT_ERROR("REDEQ500", "Default error!"),
    /**
     * Require retry this process!
     */
    REQUIRE_RETRY("REDEQ000", "Require retry this process!"),
    /**
     * Acquire lock failed!
     */
    ACQUIRE_LOCK_FAIL("REDEQ001", "Acquire lock failed!"),
    /**
     * Lock already exists!
     */
    JOB_ALREADY_EXIST("REDEQ002", "Lock already exists!"),
    /**
     * Pulling failed!
     */
    PULLING_SCHEDULE_FAILED("REDEQ003", "Pulling failed!"),
    /**
     * Add job failed!
     */
    JOB_POOL_EXCEEDED("REDEQ004", "Job pool exceeds."),

    /**
     * Topics can not be empty!
     */
    TOPIC_CONFIG_ERROR("REDEQ005", "Topics can not be empty!"),

    /**
     * Number of topic consuming threads exceeds the maximum.
     */
    TOPIC_EXCEEDS("REDEQ006", "Number of topic consuming threads exceeds the maximum."),
    /**
     * Redis execution return false!
     */
    REDIS_ERROR("REDEQ007", "Redis execution return false!");

    /**
     * error code
     */
    private final String code;

    /**
     * error message
     */
    private final String msg;

    ErrorCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return String.format("Code: [%s], Msg: [%s]", this.getCode(), this.getMsg());
    }
}
