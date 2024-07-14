package com.example.usedAuction.dto.notification;

import com.example.usedAuction.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    @Schema(description = "알림 아이디")
    private Long notificationId;
    @Schema(description = "알림 내용")
    private String message;
    @Schema(description = "연결 주소")
    private String url;
    @Schema(description = "알림 생성 시간")
    private Timestamp createdAt;
    @Schema(description = "알림 읽음 여부")
    private boolean readornot;
    @Schema(description = "사용자 번호")
    private Integer userId;

}
