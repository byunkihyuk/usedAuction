package com.example.usedAuction.errors;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException{
    private final HttpStatus httpStatus;
    private final String errorCode;

    public ApiException(ErrorEnum errorEnum){
        super(errorEnum.getMessage());
        this.httpStatus = errorEnum.getHttpStatus();
        this.errorCode = errorEnum.toString();
    }
}
