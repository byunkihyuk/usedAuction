package com.example.usedAuction.errors;

import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.dto.result.ResponseResultError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> exceptiolnHandler(ApiException apiException){
        ResponseResultError error = new ResponseResultError("error",apiException.getMessage());
        return ResponseEntity.status(apiException.getHttpStatus()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> validExceptionHandler(MethodArgumentNotValidException e){

        Map<String, Object> data = new HashMap<>();

        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        data.put("message", message);

        //ResponseResult<Map<String, Object>> responseResult = new ResponseResult<>("fail",data);
        return ResponseEntity.badRequest().body( new ResponseResult<>("fail",data));
    }
}
