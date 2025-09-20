package com.school.notification_service.exceptions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.school.notification_service.entities.Parent;
import com.school.notification_service.enumr.MessageChannel;

@Component
public class NotificationValidator {
    
    /**
     * Valide qu'un parent peut recevoir des notifications sur le canal spécifié
     */
    public void validateParentForChannel(Parent parent, MessageChannel channel) {
        if (!parent.getIsActive()) {
            throw new NotificationException("Le parent est inactif et ne peut pas recevoir de notifications");
        }
        
        switch (channel) {
            case EMAIL:
                if (parent.getEmail() == null || parent.getEmail().trim().isEmpty()) {
                    throw new NotificationException("Impossible d'envoyer par email : adresse email non configurée");
                }
                if (!isValidEmail(parent.getEmail())) {
                    throw new NotificationException("Adresse email invalide : " + parent.getEmail());
                }
                break;
                
            case WHATSAPP:
                if (parent.getWhatsappNumber() == null || parent.getWhatsappNumber().trim().isEmpty()) {
                    throw new NotificationException("Impossible d'envoyer par WhatsApp : numéro non configuré");
                }
                if (!isValidWhatsAppNumber(parent.getWhatsappNumber())) {
                    throw new NotificationException("Numéro WhatsApp invalide : " + parent.getWhatsappNumber());
                }
                break;
                
            case BOTH:
                boolean hasValidEmail = parent.getEmail() != null && 
                                      !parent.getEmail().trim().isEmpty() && 
                                      isValidEmail(parent.getEmail());
                                      
                boolean hasValidWhatsApp = parent.getWhatsappNumber() != null && 
                                         !parent.getWhatsappNumber().trim().isEmpty() && 
                                         isValidWhatsAppNumber(parent.getWhatsappNumber());
                
                if (!hasValidEmail && !hasValidWhatsApp) {
                    throw new NotificationException("Aucun moyen de contact valide configuré pour ce parent");
                }
                break;
        }
    }
    
    /**
     * Valide un message de notification
     */
    public void validateNotificationContent(String subject, String message) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new NotificationException("Le sujet de la notification est obligatoire");
        }
        
        if (subject.length() > 200) {
            throw new NotificationException("Le sujet ne peut pas dépasser 200 caractères");
        }
        
        if (message == null || message.trim().isEmpty()) {
            throw new NotificationException("Le contenu du message est obligatoire");
        }
        
        if (message.length() > 4000) {
            throw new NotificationException("Le message ne peut pas dépasser 4000 caractères");
        }
        
        // Vérifier les caractères dangereux pour WhatsApp
        if (containsUnsafeCharacters(message)) {
            throw new NotificationException("Le message contient des caractères non autorisés");
        }
    }
    
    /**
     * Valide une liste d'IDs de parents pour l'envoi en masse
     */
    public void validateBulkNotificationRequest(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            throw new NotificationException("La liste des parents ne peut pas être vide");
        }
        
        if (parentIds.size() > 500) {
            throw new NotificationException("L'envoi en masse est limité à 500 parents maximum");
        }
        
        // Vérifier les doublons
        Set<Long> uniqueIds = new HashSet<>(parentIds);
        if (uniqueIds.size() != parentIds.size()) {
            throw new NotificationException("La liste contient des doublons");
        }
    }
    
    /**
     * Valide les données d'un parent
     */
    public void validateParentData(String firstName, String lastName, String email, String whatsappNumber) {
        // Validation du prénom
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ConflictException("Le prénom est obligatoire");
        }
        if (firstName.length() > 50) {
            throw new ConflictException("Le prénom ne peut pas dépasser 50 caractères");
        }
        if (!isValidName(firstName)) {
            throw new ConflictException("Le prénom contient des caractères non autorisés");
        }
        
        // Validation du nom
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ConflictException("Le nom est obligatoire");
        }
        if (lastName.length() > 50) {
            throw new ConflictException("Le nom ne peut pas dépasser 50 caractères");
        }
        if (!isValidName(lastName)) {
            throw new ConflictException("Le nom contient des caractères non autorisés");
        }
        
        // Validation de l'email
        if (email != null && !email.trim().isEmpty()) {
            if (!isValidEmail(email)) {
                throw new ConflictException("Format d'email invalide");
            }
        }
        
        // Validation du numéro WhatsApp
        if (whatsappNumber != null && !whatsappNumber.trim().isEmpty()) {
            if (!isValidWhatsAppNumber(whatsappNumber)) {
                throw new ConflictException("Format de numéro WhatsApp invalide (format requis: +1234567890)");
            }
        }
        
        // Au moins un moyen de contact
        if ((email == null || email.trim().isEmpty()) &&
            (whatsappNumber == null || whatsappNumber.trim().isEmpty())) {
            throw new ConflictException("Au moins un email ou un numéro WhatsApp doit être fourni");
        }
    }
    
    // Méthodes utilitaires de validation
    
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    
    private boolean isValidWhatsAppNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        String whatsappRegex = "^\\+[1-9]\\d{1,14}$";
        return phoneNumber.matches(whatsappRegex);
    }
    
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        // Autorise lettres, espaces, tirets et apostrophes
        String nameRegex = "^[a-zA-ZÀ-ÿ\\s'-]+$";
        return name.matches(nameRegex);
    }
    
    private boolean containsUnsafeCharacters(String message) {
        // Vérifier les caractères potentiellement dangereux
        String[] unsafePatterns = {
            "<script", "javascript:", "onclick=", "onerror=", "onload="
        };
        
        String lowerMessage = message.toLowerCase();
        for (String pattern : unsafePatterns) {
            if (lowerMessage.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}
