package com.cmms11.common.error;

/**
 * 인증이 필요한 요청에서 인증 정보가 없거나 유효하지 않을 때 발생시키는 예외.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
