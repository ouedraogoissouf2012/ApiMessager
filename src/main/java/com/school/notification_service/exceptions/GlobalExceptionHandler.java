package com.school.notification_service.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.school.notification_service.Dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    
    /**
     * Gestion des erreurs de validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("Erreurs de validation: {}", errors);
        
        ApiResponse<Map<String, String>> response = new ApiResponse<>(
            false, "Erreurs de validation", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Gestion des exceptions métier personnalisées
     */
    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ApiResponse<String>> handleNotificationException(
            NotificationException ex, WebRequest request) {
        
        logger.error("Erreur de notification: {}", ex.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Gestion des exceptions de parent non trouvé
     */
    @ExceptionHandler(ParentNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleParentNotFoundException(
            ParentNotFoundException ex, WebRequest request) {
        
        logger.error("Parent non trouvé: {}", ex.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Gestion des exceptions de conflit (email/téléphone existant)
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<String>> handleConflictException(
            ConflictException ex, WebRequest request) {
        
        logger.error("Conflit de données: {}", ex.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Gestion des erreurs d'envoi de services externes
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<String>> handleExternalServiceException(
            ExternalServiceException ex, WebRequest request) {
        
        logger.error("Erreur de service externe: {}", ex.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(
            "Erreur de communication avec les services externes: " + ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * Gestion des erreurs génériques
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        logger.error("Erreur runtime: {}", ex.getMessage(), ex);
        
        ApiResponse<String> response = ApiResponse.error(
            "Erreur interne: " + ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Gestion des erreurs non capturées
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(
            Exception ex, WebRequest request) {
        
        logger.error("Erreur non gérée: {}", ex.getMessage(), ex);
        
        ApiResponse<String> response = ApiResponse.error(
            "Une erreur inattendue s'est produite. Veuillez réessayer plus tard.");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

/**
 * Exceptions personnalisées pour le système de notification
 */

// Exception générale pour les notifications
class NotificationException extends RuntimeException {
    public NotificationException(String message) {
        super(message);
    }
    
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Exception pour parent non trouvé
class ParentNotFoundException extends RuntimeException {
    public ParentNotFoundException(String message) {
        super(message);
    }
    
    public ParentNotFoundException(Long parentId) {
        super("Parent non trouvé avec l'ID: " + parentId);
    }
}

// Exception pour les conflits de données
class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

// Exception pour les erreurs de services externes
class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) {
        super(message);
    }
    
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
