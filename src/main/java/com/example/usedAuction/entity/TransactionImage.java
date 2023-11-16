package com.example.usedAuction.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
public class TransactionImage {
    private String originName;
    private String imageName;
    private String uploadUrl;
}
