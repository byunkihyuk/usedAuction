package com.example.usedAuction.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Integer user_id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String address;
    private String detailAddress;
    private Timestamp created_at;
    private Timestamp updated_at;
}
