package com.lidengju.redeq.exception;


import com.lidengju.redeq.constant.ErrorCodeEnum;

/**
 * Redeq related exception
 *
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * @date 2021/9/4
 **/
public class RedeqException extends Exception {
    private final ErrorCodeEnum errorCode;

    /**
     * Constructs a new RedeqException with default ErrorCodeEnum.
     */
    public RedeqException() {
        super(String.format("ErrorCode: [%s], ErrorMsg: [%s]", ErrorCodeEnum.DEFAULT_ERROR.getCode(), ErrorCodeEnum.DEFAULT_ERROR.getMsg()));
        this.errorCode = ErrorCodeEnum.DEFAULT_ERROR;
    }

    /**
     * Constructs a new RedeqException with specified ErrorCodeEnum.
     */
    public RedeqException(ErrorCodeEnum errorCode) {
        super(String.format("ErrorCode: [%s], ErrorMsg: [%s]", errorCode.getCode(), errorCode.getMsg()));
        this.errorCode = errorCode;
    }

    public ErrorCodeEnum getErrorCode() {
        return errorCode;
    }
}
