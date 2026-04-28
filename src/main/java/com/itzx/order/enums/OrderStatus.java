package com.itzx.order.enums;

public enum OrderStatus {
    WAIT_PAY(0),
    WAIT_SHIP(1),
    WAIT_RECEIVE(2),
    WAIT_COMMENT(3),
    FINISHED(4),
    CANCELLED(5);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static OrderStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
