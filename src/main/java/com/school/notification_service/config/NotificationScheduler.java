package com.school.notification_service.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.school.notification_service.services.NotificationService;

@Service
@ConditionalOnProperty(value = "notification.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {

private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Reprend les notifications échouées toutes les 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void retryFailedNotifications() {
        logger.info("Démarrage de la tâche de reprise des notifications échouées");
        
        try {
            notificationService.retryFailedNotifications();
            logger.info("Tâche de reprise des notifications échouées terminée");
        } catch (Exception e) {
            logger.error("Erreur lors de la reprise des notifications échouées: {}", e.getMessage());

        }
    }
    /**
     * Traite les notifications programmées toutes les 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void processScheduledNotifications() {
        logger.info("Démarrage du traitement des notifications programmées");
        
        try {
            notificationService.processScheduledNotifications();
            logger.info("Traitement des notifications programmées terminé");
        } catch (Exception e) {
            logger.error("Erreur lors du traitement des notifications programmées: {}", e.getMessage());
        }
        
        
    }
    /**
     * Nettoie les anciennes notifications tous les jours à 2h du matin
     */
    @Scheduled(cron = "0 0 2 * * *") // Tous les jours à 2h00
    public void cleanupOldNotifications() {
        logger.info("Démarrage du nettoyage des anciennes notifications");
        
        try {
            // Garder les notifications pendant 90 jours
            notificationService.cleanupOldNotifications(90);
            logger.info("Nettoyage des anciennes notifications terminé");
        } catch (Exception e) {
            logger.error("Erreur lors du nettoyage des notifications: {}", e.getMessage());
        }
    }
    
    /**
     * Génère un rapport de statut toutes les heures
     */
    @Scheduled(fixedRate = 3600000) // 1 heure
    public void generateStatusReport() {
        logger.info("Génération du rapport de statut des notifications");
        
        try {
            Map<String, Object> stats = notificationService.getNotificationStatistics();
            
            logger.info("=== RAPPORT NOTIFICATION ===");
            logger.info("Total notifications: {}", stats.get("total"));
            logger.info("Parents actifs: {}", stats.get("activeParents"));
            
            @SuppressWarnings("unchecked")
            Map<String, Long> statusStats = (Map<String, Long>) stats.get("byStatus");
            if (statusStats != null) {
                statusStats.forEach((status, count) -> 
                    logger.info("Statut {}: {} notifications", status, count));
            }
            
            logger.info("=== FIN RAPPORT ===");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du rapport: {}", e.getMessage());
        }
    }
    
}
