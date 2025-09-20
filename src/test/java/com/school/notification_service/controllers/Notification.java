package com.school.notification_service.controllers;

// Fichier: src/test/java/com/school/notification/service/NotificationServiceTest.java


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.school.notification_service.Dto.BulletinNotificationRequest;
import com.school.notification_service.Dto.NotificationRequest;
import com.school.notification_service.Dto.NotificationResponse;
import com.school.notification_service.entities.Notification;
import com.school.notification_service.entities.Parent;
import com.school.notification_service.enumr.DeliveryStatus;
import com.school.notification_service.enumr.MessageChannel;
import com.school.notification_service.enumr.NotificationType;
import com.school.notification_service.repositories.NotificationRepository;
import com.school.notification_service.repositories.ParentRepository;
import com.school.notification_service.services.EmailService;
import com.school.notification_service.services.NotificationService;
import com.school.notification_service.services.WhatsAppService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    
    @Mock
    private ParentRepository parentRepository;
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private WhatsAppService whatsAppService;
    
    @InjectMocks
    private NotificationService notificationService;
    
    private Parent testParent;
    private Notification testNotification;
    
    @BeforeEach
    void setUp() {
        testParent = new Parent();
        testParent.setId(1L);
        testParent.setFirstName("Test");
        testParent.setLastName("Parent");
        testParent.setEmail("test@example.com");
        testParent.setWhatsappNumber("+33123456789");
        testParent.setPreferredChannel(MessageChannel.BOTH);
        testParent.setIsActive(true);
        
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setParent(testParent);
        testNotification.setType(NotificationType.BULLETIN_AVAILABLE);
        testNotification.setChannel(MessageChannel.EMAIL);
        testNotification.setSubject("Test Subject");
        testNotification.setMessage("Test Message");
        testNotification.setStatus(DeliveryStatus.PENDING);
    }
    
    @Test
    void sendBulletinNotification_Success() {
        // Arrange
        BulletinNotificationRequest request = new BulletinNotificationRequest();
        request.setParentId(1L);
        request.setStudentName("John Doe");
        request.setPeriod("Trimestre 1");
        
        when(parentRepository.findById(1L)).thenReturn(Optional.of(testParent));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(emailService.sendBulletinNotification(any(), any(), any())).thenReturn(true);
        
        // Act
        NotificationResponse response = notificationService.sendBulletinNotification(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getParentId());
        assertEquals("Test Parent", response.getParentName());
        
        verify(parentRepository).findById(1L);
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService).sendBulletinNotification(testParent, "John Doe", "Trimestre 1");
    }
    
    @Test
    void sendBulletinNotification_ParentNotFound() {
        // Arrange
        BulletinNotificationRequest request = new BulletinNotificationRequest();
        request.setParentId(999L);
        
        when(parentRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            notificationService.sendBulletinNotification(request);
        });
        
        verify(parentRepository).findById(999L);
        verify(notificationRepository, never()).save(any());
    }
    
    @Test
    void sendParentMessage_Success() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setParentId(1L);
        request.setType(NotificationType.GENERAL_MESSAGE);
        request.setSubject("Test Subject");
        request.setMessage("Test Message");
        request.setChannel(MessageChannel.EMAIL);
        
        when(parentRepository.findById(1L)).thenReturn(Optional.of(testParent));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(emailService.sendParentMessage(any(), any(), any())).thenReturn(true);
        
        // Act
        NotificationResponse response = notificationService.sendParentMessage(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        
        verify(emailService).sendParentMessage(testParent, "Test Subject", "Test Message");
    }
    
    @Test
    void sendNotification_InactiveParent_ThrowsException() {
        // Arrange
        testParent.setIsActive(false);
        NotificationRequest request = new NotificationRequest();
        request.setParentId(1L);
        request.setType(NotificationType.GENERAL_MESSAGE);
        request.setSubject("Test");
        request.setMessage("Test");
        
        when(parentRepository.findById(1L)).thenReturn(Optional.of(testParent));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            notificationService.sendParentMessage(request);
        });
    }
}


