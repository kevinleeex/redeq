package com.lidengju.redeq.exception;

import com.lidengju.redeq.constant.ErrorCodeEnum;

/**
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * @date 2021/9/4
 */
public class RedeqRuntimeException extends RuntimeException {
    private final ErrorCodeEnum errorCode;

    /**
     * Constructs a new RedeqRuntimeException with default ErrorCodeEnum.
     */
    public RedeqRuntimeException() {
        super(String.format("ErrorCode: [%s], ErrorMsg: [%s]", ErrorCodeEnum.DEFAULT_ERROR.getCode(), ErrorCodeEnum.DEFAULT_ERROR.getMsg()));
        this.errorCode = ErrorCodeEnum.DEFAULT_ERROR;
    }

    /**
     * Constructs a new RedeqRuntimeException with specified ErrorCodeEnum.
     */
    public RedeqRuntimeException(ErrorCodeEnum errorCode) {
        super(String.format("ErrorCode: [%s], ErrorMsg: [%s]", errorCode.getCode(), errorCode.getMsg()));
        this.errorCode = errorCode;
    }

    public ErrorCodeEnum getErrorCode() {
        return errorCode;
    }
}
