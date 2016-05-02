package com.carlos;

/**
 * Created by carlos on 16/4/30.
 * 异常类
 */
public class RLException extends RuntimeException{
    public RLException() {
        super();
    }

    public RLException(String detailMessage) {
        super(detailMessage);
    }

    public RLException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RLException(Throwable throwable) {
        super(throwable);
    }
}
