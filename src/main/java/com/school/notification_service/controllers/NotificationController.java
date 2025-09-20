package com.school.notification_service.controllers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.notification_service.Dto.ApiResponse;
import com.school.notification_service.Dto.BulkNotificationRequest;
import com.school.notification_service.Dto.BulletinNotificationRequest;
import com.school.notification_service.Dto.NotificationRequest;
import com.school.notification_service.Dto.NotificationResponse;
import com.school.notification_service.services.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "API de gestion des notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Envoie une notification de bulletin disponible
     */
    
    @PostMapping("/bulletin")
    @Operation(summary = "Notifier qu'un bulletin est disponible", 
               description = "Envoie une notification aux parents pour les informer qu'un bulletin est disponible")
   
    public ResponseEntity<ApiResponse<NotificationResponse>> sendBulletinNotification(
            @Valid @RequestBody BulletinNotificationRequest request) {
        
        try {
            logger.info("Demande de notification de bulletin pour le parent ID: {}", request.getParentId());
            
            NotificationResponse response = notificationService.sendBulletinNotification(request);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Notification de bulletin envoyée avec succès", response));
                
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de bulletin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de l'envoi de la notification: " + e.getMessage()));
        }
    }
    
    /**
     * Envoie un message général aux parents
     */
    @PostMapping("/message")
    @Operation(summary = "Envoyer un message aux parents", 
               description = "Envoie un message personnalisé à un parent via email et/ou WhatsApp")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendParentMessage(
            @Valid @RequestBody NotificationRequest request) {
        
        try {
            logger.info("Demande d'envoi de message pour le parent ID: {}", request.getParentId());
            
            NotificationResponse response = notificationService.sendParentMessage(request);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Message envoyé avec succès", response));
                
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de l'envoi du message: " + e.getMessage()));
        }
    }
    
    /**
     * Envoie des notifications en masse
     */
    @PostMapping("/bulk")
    @Operation(summary = "Envoyer des notifications en masse", 
               description = "Envoie le même message à plusieurs parents en une seule fois")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> sendBulkNotifications(
            @Valid @RequestBody BulkNotificationRequest request) {
        
        try {
            logger.info("Demande d'envoi en masse pour {} parents", request.getParentIds().size());
            
            List<NotificationResponse> responses = notificationService.sendBulkNotifications(request);
            
            return ResponseEntity.ok(ApiResponse.success(
                String.format("Envoi en masse terminé - %d notifications traitées", responses.size()), 
                responses));
                
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi en masse: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de l'envoi en masse: " + e.getMessage()));
        }
    }
    
    /**
     * Obtient le statut de livraison d'une notification
     */
    @GetMapping("/{notificationId}/status")
    @Operation(summary = "Obtenir le statut d'une notification", 
               description = "Récupère le statut de livraison d'une notification spécifique")
    public ResponseEntity<ApiResponse<NotificationResponse>> getDeliveryStatus(
            @Parameter(description = "ID de la notification") @PathVariable Long notificationId) {
        
        try {
            NotificationResponse response = notificationService.getDeliveryStatus(notificationId);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du statut: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Notification non trouvée: " + e.getMessage()));
        }
    }
    
    /**
     * Obtient l'historique des notifications d'un parent
     */
    @GetMapping("/parent/{parentId}/history")
    @Operation(summary = "Historique des notifications d'un parent", 
               description = "Récupère l'historique complet des notifications envoyées à un parent")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getParentNotificationHistory(
            @Parameter(description = "ID du parent") @PathVariable Long parentId) {
        
        try {
            List<NotificationResponse> history = notificationService.getParentNotificationHistory(parentId);
            
            return ResponseEntity.ok(ApiResponse.success(
                String.format("Historique récupéré - %d notifications trouvées", history.size()), 
                history));
                
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'historique: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de la récupération: " + e.getMessage()));
        }
    }
    
    /**
     * Reprend les notifications échouées
     */
    @PostMapping("/retry-failed")
    @Operation(summary = "Reprendre les notifications échouées", 
               description = "Relance l'envoi des notifications qui ont échoué")
    public ResponseEntity<ApiResponse<String>> retryFailedNotifications() {
        
        try {
            notificationService.retryFailedNotifications();
            
            return ResponseEntity.ok(ApiResponse.success(
                "Reprise des notifications échouées lancée en arrière-plan"));
                
        } catch (Exception e) {
            logger.error("Erreur lors de la reprise des notifications échouées: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la reprise: " + e.getMessage()));
        }
    }
    /**
     * Obtient les statistiques des notifications
     */
    @GetMapping("/statistics")
    @Operation(summary = "Statistiques des notifications", 
               description = "Récupère les statistiques générales des notifications")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotificationStatistics() {
        
        try {
            Map<String, Object> stats = notificationService.getNotificationStatistics();
            
            return ResponseEntity.ok(ApiResponse.success("Statistiques récupérées", stats));
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la récupération des statistiques: " + e.getMessage()));
        }
    }
    /**
     * Endpoint de santé pour vérifier le service
     */
    @GetMapping("/health")
    @Operation(summary = "Vérification de santé du service", 
               description = "Vérifie que le service de notification fonctionne correctement")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        
        try {
            // Vérifier les services externes si nécessaire
            return ResponseEntity.ok(ApiResponse.success("Service de notification opérationnel"));
            
        } catch (Exception e) {
            logger.error("Vérification de santé échouée: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("Service indisponible: " + e.getMessage()));
        }
    }
}
