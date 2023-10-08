package com.example.usedAuction.dto.result;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseResultError {
    private String status;
    private String message;
}
