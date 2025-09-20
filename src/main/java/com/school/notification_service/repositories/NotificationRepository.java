package com.school.notification_service.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.school.notification_service.entities.Notification;
import com.school.notification_service.enumr.DeliveryStatus;
import com.school.notification_service.enumr.NotificationType;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	 // Trouver toutes les notifications d'un parent
    List<Notification> findByParentIdOrderByCreatedAtDesc(Long parentId);
    
    // Trouver les notifications par statut
    List<Notification> findByStatus(DeliveryStatus status);
    
    // Trouver les notifications en attente (pour retry)
    List<Notification> findByStatusAndRetryCountLessThan(DeliveryStatus status, Integer maxRetryCount);
    
    // Trouver les notifications programmées prêtes à être envoyées
    @Query("SELECT n FROM Notification n WHERE n.status = com.school.notification_service.enumr.DeliveryStatus.PENDING " +
           "AND n.scheduledAt IS NOT NULL AND n.scheduledAt <= :now")
    List<Notification> findScheduledNotificationsReadyToSend(@Param("now") LocalDateTime now);
    
    // Trouver les notifications échouées à reprendre
    @Query("SELECT n FROM Notification n WHERE n.status = com.school.notification_service.enumr.DeliveryStatus.FAILED " +
           "AND n.retryCount < :maxRetryCount " +
           "AND n.updatedAt <= :retryAfter")
    List<Notification> findFailedNotificationsForRetry(
            @Param("maxRetryCount") Integer maxRetryCount,
            @Param("retryAfter") LocalDateTime retryAfter
    );
    
    // Statistiques par type de notification
    @Query("SELECT n.type, COUNT(n) FROM Notification n GROUP BY n.type")
    List<Object[]> getNotificationCountByType();
    
    // Statistiques par statut
    @Query("SELECT n.status, COUNT(n) FROM Notification n GROUP BY n.status")
    List<Object[]> getNotificationCountByStatus();
    
    // Statistiques par période
    @Query("SELECT DATE(n.createdAt), COUNT(n) FROM Notification n " +
           "WHERE n.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(n.createdAt) ORDER BY DATE(n.createdAt)")
    List<Object[]> getNotificationCountByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    // Trouver les notifications récentes d'un parent
    @Query("SELECT n FROM Notification n WHERE n.parent.id = :parentId " +
           "AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotificationsByParent(
            @Param("parentId") Long parentId,
            @Param("since") LocalDateTime since
    );
    // Trouver par ID de message externe (pour les webhooks)
    Optional<Notification> findByExternalMessageId(String externalMessageId);
    
    // Compter les notifications par parent et statut
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.parent.id = :parentId AND n.status = :status")
    long countByParentIdAndStatus(@Param("parentId") Long parentId, @Param("status") DeliveryStatus status);
    
    // Trouver les notifications par type et période
    List<Notification> findByTypeAndCreatedAtBetween(
            NotificationType type, 
            LocalDateTime startDate, 
            LocalDateTime endDate
    );
    
    // Supprimer les anciennes notifications (pour le nettoyage)
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    void deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}
