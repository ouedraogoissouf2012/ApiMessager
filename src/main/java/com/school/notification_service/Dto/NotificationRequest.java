package com.school.notification_service.Dto;

import java.time.LocalDateTime;

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
public class NotificationRequest {

	 @NotNull(message = "L'ID du parent est obligatoire")
	    private Long parentId;
	    
	    @NotNull(message = "Le type de notification est obligatoire")
	    private NotificationType type;
	    
	    private MessageChannel channel; // Si null, utilise la préférence du parent
	    
	    @NotBlank(message = "Le sujet est obligatoire")
	    private String subject;
	    
	    @NotBlank(message = "Le message est obligatoire")
	    private String message;
	    
	    private LocalDateTime scheduledAt; // Pour envoi différé
}
