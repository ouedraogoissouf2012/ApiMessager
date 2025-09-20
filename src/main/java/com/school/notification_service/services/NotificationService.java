package com.school.notification_service.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.notification_service.Dto.BulkNotificationRequest;
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

@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private ParentRepository parentRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private WhatsAppService whatsAppService;
    
    @Value("${notification.scheduling.retry-attempts:3}")
    private Integer maxRetryAttempts;
    
    /**
     * Envoie une notification de bulletin disponible
     */
    public NotificationResponse sendBulletinNotification(BulletinNotificationRequest request) {
        logger.info("Envoi de notification de bulletin pour le parent ID: {}", request.getParentId());
        
        Optional<Parent> parentOpt = parentRepository.findById(request.getParentId());
        if (!parentOpt.isPresent()) {
            throw new RuntimeException("Parent non trouvé avec l'ID: " + request.getParentId());
        }
        
        Parent parent = parentOpt.get();
        if (!parent.getIsActive()) {
            throw new RuntimeException("Le parent est inactif");
        }
        
        // Déterminer le canal à utiliser
        MessageChannel channel = request.getChannel() != null ? 
            request.getChannel() : parent.getPreferredChannel();
        
        String subject = String.format("Bulletin de %s disponible - %s", 
                                      request.getStudentName(), request.getPeriod());
        String message = String.format("Le bulletin de %s pour la période %s est maintenant disponible.", 
                                      request.getStudentName(), request.getPeriod());
        
        return sendNotification(parent, NotificationType.BULLETIN_AVAILABLE, channel, subject, message);
    }
    
    /**
     * Envoie un message général aux parents
     */
    public NotificationResponse sendParentMessage(NotificationRequest request) {
        logger.info("Envoi de message parent pour l'ID: {}", request.getParentId());
        
        Optional<Parent> parentOpt = parentRepository.findById(request.getParentId());
        if (!parentOpt.isPresent()) {
            throw new RuntimeException("Parent non trouvé avec l'ID: " + request.getParentId());
        }
        
        Parent parent = parentOpt.get();
        MessageChannel channel = request.getChannel() != null ? 
            request.getChannel() : parent.getPreferredChannel();
        
        return sendNotification(parent, request.getType(), channel, 
                              request.getSubject(), request.getMessage());
    }
    
    /**
     * Envoie des notifications en masse
     */
    public List<NotificationResponse> sendBulkNotifications(BulkNotificationRequest request) {
        logger.info("Envoi en masse de {} notifications", request.getParentIds().size());
        
        List<NotificationResponse> responses = new ArrayList<>();
        
        for (Long parentId : request.getParentIds()) {
            try {
                Optional<Parent> parentOpt = parentRepository.findById(parentId);
                if (parentOpt.isPresent() && parentOpt.get().getIsActive()) {
                    Parent parent = parentOpt.get();
                    MessageChannel channel = request.getChannel() != null ? 
                        request.getChannel() : parent.getPreferredChannel();
                    
                    NotificationResponse response = sendNotification(parent, request.getType(), 
                                                                   channel, request.getSubject(), 
                                                                   request.getMessage());
                    responses.add(response);
                } else {
                    logger.warn("Parent ID {} non trouvé ou inactif", parentId);
                }
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi pour le parent ID {}: {}", parentId, e.getMessage());
                // Continuer avec les autres parents
            }
        }
        
        return responses;
    }
    
    /**
     * Méthode principale d'envoi de notification
     */
    private NotificationResponse sendNotification(Parent parent, NotificationType type, 
                                                MessageChannel channel, String subject, String message) {
        
        // Créer l'entité notification
        Notification notification = new Notification(parent, type, channel, subject, message);
        notification = notificationRepository.save(notification);
        
        // Envoyer selon le canal choisi
        boolean emailSent = false;
        boolean whatsappSent = false;
        String externalMessageId = null;
        
        try {
            switch (channel) {
                case EMAIL:
                    emailSent = sendEmailNotification(parent, type, subject, message);
                    break;
                    
                case WHATSAPP:
                    String messageId = sendWhatsAppNotification(parent, type, subject, message);
                    whatsappSent = messageId != null;
                    externalMessageId = messageId;
                    break;
                    
                case BOTH:
                    emailSent = sendEmailNotification(parent, type, subject, message);
                    String whatsappMessageId = sendWhatsAppNotification(parent, type, subject, message);
                    whatsappSent = whatsappMessageId != null;
                    externalMessageId = whatsappMessageId;
                    break;
            }
            
            // Mettre à jour le statut
            boolean success = (channel == MessageChannel.EMAIL && emailSent) ||
                             (channel == MessageChannel.WHATSAPP && whatsappSent) ||
                             (channel == MessageChannel.BOTH && (emailSent || whatsappSent));
            
             if (success) {
                notification.setStatus(DeliveryStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notification.setExternalMessageId(externalMessageId);
            } else {
                notification.setStatus(DeliveryStatus.FAILED);
                notification.setErrorMessage("Échec de l'envoi sur tous les canaux");
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de notification: {}", e.getMessage());
            notification.setStatus(DeliveryStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
        }
        
        notification = notificationRepository.save(notification);
        
        return mapToNotificationResponse(notification);
    }
    
    /**
     * Envoie une notification par email
     */
    private boolean sendEmailNotification(Parent parent, NotificationType type, String subject, String message) {
        if (parent.getEmail() == null || parent.getEmail().trim().isEmpty()) {
            logger.warn("Email non configuré pour le parent ID: {}", parent.getId());
            return false;
        }
        
        switch (type) {
            case BULLETIN_AVAILABLE:
                return emailService.sendParentMessage(parent, subject, message);
                
            case URGENT_ALERT:
                return emailService.sendUrgentNotification(parent, subject, message);
                
            case ABSENCE_ALERT:
                return emailService.sendParentMessage(parent, subject, message);
                
            default:
                return emailService.sendParentMessage(parent, subject, message);
        }
    }
    
    /**
     * Envoie une notification par WhatsApp
     */
    private String sendWhatsAppNotification(Parent parent, NotificationType type, String subject, String message) {
        if (parent.getWhatsappNumber() == null || parent.getWhatsappNumber().trim().isEmpty()) {
            logger.warn("Numéro WhatsApp non configuré pour le parent ID: {}", parent.getId());
            return null;
        }
        
        switch (type) {
            case BULLETIN_AVAILABLE:
                return whatsAppService.sendParentMessage(parent, subject, message);
                
            case URGENT_ALERT:
                return whatsAppService.sendUrgentNotification(parent, subject, message);
                
            case ABSENCE_ALERT:
                return whatsAppService.sendParentMessage(parent, subject, message);
                
            default:
                return whatsAppService.sendParentMessage(parent, subject, message);
        }
    }
    
    /**
     * Obtient le statut de livraison d'une notification
     */
    public NotificationResponse getDeliveryStatus(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (!notificationOpt.isPresent()) {
            throw new RuntimeException("Notification non trouvée avec l'ID: " + notificationId);
        }
        
        return mapToNotificationResponse(notificationOpt.get());
    }
    
    /**
     * Obtient l'historique des notifications d'un parent
     */
    public List<NotificationResponse> getParentNotificationHistory(Long parentId) {
        List<Notification> notifications = notificationRepository.findByParentIdOrderByCreatedAtDesc(parentId);
        return notifications.stream()
                           .map(this::mapToNotificationResponse)
                           .toList();
    }
    
    /**
     * Reprend les notifications échouées
     */
    @Async
    public CompletableFuture<Void> retryFailedNotifications() {
        logger.info("Début de la reprise des notifications échouées");
        
        LocalDateTime retryAfter = LocalDateTime.now().minusMinutes(5);
        List<Notification> failedNotifications = notificationRepository
                .findFailedNotificationsForRetry(maxRetryAttempts, retryAfter);
        
        for (Notification notification : failedNotifications) {
            try {
                logger.info("Tentative de renvoi de la notification ID: {}", notification.getId());
                
                Parent parent = notification.getParent();
                boolean success = false;
                String externalMessageId = null;
                
                switch (notification.getChannel()) {
                    case EMAIL:
                        success = sendEmailNotification(parent, notification.getType(), 
                                                      notification.getSubject(), notification.getMessage());
                        break;
                        
                    case WHATSAPP:
                        externalMessageId = sendWhatsAppNotification(parent, notification.getType(), 
                                                                   notification.getSubject(), notification.getMessage());
                        success = externalMessageId != null;
                        break;
                        
                    case BOTH:
                        boolean emailSent = sendEmailNotification(parent, notification.getType(), 
                                                                notification.getSubject(), notification.getMessage());
                        String whatsappMessageId = sendWhatsAppNotification(parent, notification.getType(), 
                                                                           notification.getSubject(), notification.getMessage());
                        success = emailSent || whatsappMessageId != null;
                        externalMessageId = whatsappMessageId;
                        break;
                }
                
                notification.setRetryCount(notification.getRetryCount() + 1);
                
                if (success) {
                    notification.setStatus(DeliveryStatus.SENT);
                    notification.setSentAt(LocalDateTime.now());
                    notification.setExternalMessageId(externalMessageId);
                    notification.setErrorMessage(null);
                    logger.info("Notification ID {} renvoyée avec succès", notification.getId());
                } else {
                    if (notification.getRetryCount() >= maxRetryAttempts) {
                        notification.setStatus(DeliveryStatus.FAILED);
                        notification.setErrorMessage("Échec après " + maxRetryAttempts + " tentatives");
                        logger.error("Notification ID {} abandonnée après {} tentatives", 
                                   notification.getId(), maxRetryAttempts);
                    } else {
                        notification.setStatus(DeliveryStatus.RETRY);
                        logger.warn("Notification ID {} échouée - tentative {}/{}", 
                                  notification.getId(), notification.getRetryCount(), maxRetryAttempts);
                    }
                }
                
                notificationRepository.save(notification);
                
            } catch (Exception e) {
                logger.error("Erreur lors du retry de la notification ID {}: {}", 
                           notification.getId(), e.getMessage());
                
                notification.setRetryCount(notification.getRetryCount() + 1);
                notification.setErrorMessage(e.getMessage());
                
                if (notification.getRetryCount() >= maxRetryAttempts) {
                    notification.setStatus(DeliveryStatus.FAILED);
                }
                
                notificationRepository.save(notification);
            }
        }
        
        logger.info("Fin de la reprise des notifications échouées - {} notifications traitées", 
                   failedNotifications.size());
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Traite les notifications programmées
     */
    @Async
    public CompletableFuture<Void> processScheduledNotifications() {
        logger.info("Traitement des notifications programmées");
        
        List<Notification> scheduledNotifications = notificationRepository
                .findScheduledNotificationsReadyToSend(LocalDateTime.now());
        
        for (Notification notification : scheduledNotifications) {
            try {
                Parent parent = notification.getParent();
                
                NotificationResponse response = sendNotification(parent, notification.getType(), 
                                                               notification.getChannel(), 
                                                               notification.getSubject(), 
                                                               notification.getMessage());
                
                logger.info("Notification programmée ID {} traitée avec le statut: {}", 
                           notification.getId(), response.getStatus());
                
            } catch (Exception e) {
                logger.error("Erreur lors du traitement de la notification programmée ID {}: {}", 
                           notification.getId(), e.getMessage());
            }
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Obtient les statistiques des notifications
     */
    public Map<String, Object> getNotificationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Statistiques par statut
        List<Object[]> statusStats = notificationRepository.getNotificationCountByStatus();
        Map<String, Long> statusCounts = new HashMap<>();
        for (Object[] stat : statusStats) {
            statusCounts.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("byStatus", statusCounts);
        
        // Statistiques par type
        List<Object[]> typeStats = notificationRepository.getNotificationCountByType();
        Map<String, Long> typeCounts = new HashMap<>();
        for (Object[] stat : typeStats) {
            typeCounts.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("byType", typeCounts);
        
        // Total des notifications
        stats.put("total", notificationRepository.count());
        
        // Parents actifs
        stats.put("activeParents", parentRepository.countByIsActiveTrue());
        
        return stats;
    }
    
    /**
     * Nettoie les anciennes notifications
     */
    public void cleanupOldNotifications(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        notificationRepository.deleteOldNotifications(cutoffDate);
        logger.info("Nettoyage des notifications antérieures au {}", cutoffDate);
    }
    
    /**
     * Mappe une entité Notification vers un DTO de réponse
     */
    private NotificationResponse mapToNotificationResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setParentId(notification.getParent().getId());
        response.setParentName(notification.getParent().getFullName());
        response.setType(notification.getType());
        response.setChannel(notification.getChannel());
        response.setSubject(notification.getSubject());
        response.setStatus(notification.getStatus());
        response.setExternalMessageId(notification.getExternalMessageId());
        response.setSentAt(notification.getSentAt());
        response.setCreatedAt(notification.getCreatedAt());
        
        return response;
    }
}