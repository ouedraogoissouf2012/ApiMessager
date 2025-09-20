package com.school.notification_service.Dto;

import com.school.notification_service.enumr.MessageChannel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulletinNotificationRequest {

	@NotNull(message = "L'ID du parent est obligatoire")
    private Long parentId;
    
    @NotBlank(message = "Le nom de l'élève est obligatoire")
    private String studentName;
    
    @NotBlank(message = "La période est obligatoire")
    private String period; // Ex: "Trimestre 1", "Semestre 2"
    
    private MessageChannel channel;
}
