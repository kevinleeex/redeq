package com.lidengju.redeq.constant;

/**
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/5
 */
public enum StatusEnum {

    /**
     * Ready to start
     */
    READY(0),
    /**
     * Consumer is running
     */
    RUNNING(1),
    /**
     * Consumer stopped
     */
    CANCELED(2);

    private final int value;

    StatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
