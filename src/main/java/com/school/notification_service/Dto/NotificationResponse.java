package com.school.notification_service.Dto;

import java.time.LocalDateTime;

import com.school.notification_service.enumr.DeliveryStatus;
import com.school.notification_service.enumr.MessageChannel;
import com.school.notification_service.enumr.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NotificationResponse {

	private Long id;
    private Long parentId;
    private String parentName;
    private NotificationType type;
    private MessageChannel channel;
    private String subject;
    private DeliveryStatus status;
    private String externalMessageId;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
