package com.school.notification_service.Dto;

import java.time.LocalDateTime;
import java.util.List;

import com.school.notification_service.enumr.MessageChannel;
import com.school.notification_service.enumr.NotificationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkNotificationRequest {

	@NotNull(message = "Les IDs des parents sont obligatoires")
    private List<Long> parentIds;
    
    @NotNull(message = "Le type de notification est obligatoire")
    private NotificationType type;
    
    @NotBlank(message = "Le sujet est obligatoire")
    private String subject;
    
    @NotBlank(message = "Le message est obligatoire")
    private String message;
    
    private MessageChannel channel;
    
    private LocalDateTime scheduledAt;
}
