package com.school.notification_service.controllers;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.notification_service.Dto.BulletinNotificationRequest;
import com.school.notification_service.Dto.NotificationResponse;
import com.school.notification_service.enumr.DeliveryStatus;
import com.school.notification_service.enumr.MessageChannel;
import com.school.notification_service.enumr.NotificationType;
import com.school.notification_service.services.NotificationService;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {

	

	

	
	
	    @Autowired
	    private MockMvc mockMvc;
	    
	    @MockBean
	    private NotificationService notificationService;
	    
	    @Autowired
	    private ObjectMapper objectMapper;
	    
	    @Test
	    void sendBulletinNotification_Success() throws Exception {
	        // Arrange
	        BulletinNotificationRequest request = new BulletinNotificationRequest();
	        request.setParentId(1L);
	        request.setStudentName("John Doe");
	        request.setPeriod("Trimestre 1");
	        
	        NotificationResponse response = new NotificationResponse();
	        response.setId(1L);
	        response.setParentId(1L);
	        response.setParentName("Test Parent");
	        response.setType(NotificationType.BULLETIN_AVAILABLE);
	        response.setChannel(MessageChannel.EMAIL);
	        response.setSubject("Bulletin de John Doe disponible - Trimestre 1");
	        response.setStatus(DeliveryStatus.SENT);
	        
	        when(notificationService.sendBulletinNotification(any(BulletinNotificationRequest.class)))
	                .thenReturn(response);
	        
	        // Act & Assert
	        mockMvc.perform(post("/api/notifications/bulletin")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(objectMapper.writeValueAsString(request)))
	                .andExpect(status().isOk())
	                .andExpect(jsonPath("$.success").value(true))
	                .andExpect(jsonPath("$.data.id").value(1))
	                .andExpect(jsonPath("$.data.parentId").value(1))
	                .andExpect(jsonPath("$.data.status").value("SENT"));
	    }
	    
	    @Test
	    void sendBulletinNotification_ValidationError() throws Exception {
	        // Arrange - Request with missing required fields
	        BulletinNotificationRequest request = new BulletinNotificationRequest();
	        // Missing parentId, studentName, period
	        
	        // Act & Assert
	        mockMvc.perform(post("/api/notifications/bulletin")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(objectMapper.writeValueAsString(request)))
	                .andExpect(status().isBadRequest())
	                .andExpect(jsonPath("$.success").value(false));
	    }
	    
	    @Test
	    void getDeliveryStatus_Success() throws Exception {
	        // Arrange
	        NotificationResponse response = new NotificationResponse();
	        response.setId(1L);
	        response.setStatus(DeliveryStatus.DELIVERED);
	        
	        when(notificationService.getDeliveryStatus(1L)).thenReturn(response);
	        
	        // Act & Assert
	        mockMvc.perform(get("/api/notifications/1/status"))
	                .andExpect(status().isOk())
	                .andExpect(jsonPath("$.success").value(true))
	                .andExpect(jsonPath("$.data.id").value(1))
	                .andExpect(jsonPath("$.data.status").value("DELIVERED"));
	    }
	    
	    @Test
	    void healthCheck_Success() throws Exception {
	        mockMvc.perform(get("/api/notifications/health"))
	                .andExpect(status().isOk())
	                .andExpect(jsonPath("$.success").value(true))
	                .andExpect(jsonPath("$.message").value("Service de notification opérationnel"));
	    }
	

}
