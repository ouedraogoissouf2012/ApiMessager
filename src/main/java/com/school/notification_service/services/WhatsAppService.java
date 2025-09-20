package com.school.notification_service.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.notification_service.entities.Parent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {
    
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);
    
    // Configuration compatible avec le nouveau application.yml
    @Value("${notification.whatsapp.web-api.url:http://localhost:3000}")
    private String whatsappApiUrl;
    
    @Value("${notification.whatsapp.web-api.enabled:true}")
    private boolean whatsappEnabled;
    
    @Value("${notification.school.name:Université Aube Nouvelle}")
    private String schoolName;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public WhatsAppService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Envoie un message WhatsApp via notre serveur Web API
     */
    public String sendMessage(String phoneNumber, String message) {
        if (!whatsappEnabled) {
            logger.info("🔕 WhatsApp désactivé - Génération d'un lien à la place");
            return generateWhatsAppLink(phoneNumber, message);
        }

        try {
            String url = whatsappApiUrl + "/send-message";
            
            // Nettoyer le numéro de téléphone
            String cleanPhoneNumber = phoneNumber.replaceAll("[^0-9]", "");
            
            // Construire le payload pour notre serveur Node.js
            Map<String, Object> payload = new HashMap<>();
            payload.put("number", cleanPhoneNumber);
            payload.put("message", message);
            
            // Configurer les headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            // Créer la requête
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            logger.info("📤 Envoi WhatsApp vers: {} via {}", phoneNumber, url);
            
            // Envoyer la requête
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                boolean success = responseBody != null && Boolean.TRUE.equals(responseBody.get("success"));
                
                if (success) {
                    String messageId = (String) responseBody.get("messageId");
                    logger.info("✅ Message WhatsApp envoyé avec succès à {} - ID: {}", phoneNumber, messageId);
                    return messageId != null ? messageId : "sent";
                } else {
                    logger.error("❌ Échec envoi WhatsApp à {}: {}", phoneNumber, responseBody);
                    return generateWhatsAppLink(phoneNumber, message); // Fallback
                }
            } else {
                logger.error("❌ Erreur HTTP WhatsApp à {}: {}", phoneNumber, response.getStatusCode());
                return generateWhatsAppLink(phoneNumber, message); // Fallback
            }
            
        } catch (Exception e) {
            logger.error("💥 Exception lors de l'envoi WhatsApp à {}: {}", phoneNumber, e.getMessage());
            return generateWhatsAppLink(phoneNumber, message); // Fallback vers lien
        }
    }
    
    /**
     * Génère un lien WhatsApp cliquable en fallback
     */
    private String generateWhatsAppLink(String phoneNumber, String message) {
        try {
            String cleanNumber = phoneNumber.replace("+", "");
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
            String link = String.format("https://wa.me/%s?text=%s", cleanNumber, encodedMessage);
            logger.info("🔗 Lien WhatsApp généré pour {}: {}", phoneNumber, link);
            return "link:" + link; // Préfixe pour identifier un lien
        } catch (Exception e) {
            logger.error("❌ Erreur génération lien WhatsApp: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Envoie une notification de bulletin par WhatsApp
     */
    public String sendBulletinNotification(Parent parent, String studentName, String period, String bulletinUrl) {
        try {
            String message = String.format(
                "🎓 *Bulletin Disponible*\n\n" +
                "Bonjour %s,\n\n" +
                "Le bulletin de *%s* pour la période *%s* est maintenant disponible.\n\n" +
                "📋 Consultez le bulletin: %s\n\n" +
                "📅 %s\n" +
                "🏫 %s",
                parent.getFirstName(),
                studentName,
                period,
                bulletinUrl != null ? bulletinUrl : "Disponible sur la plateforme",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                schoolName
            );
            
            return sendMessage(parent.getWhatsappNumber(), message);
            
        } catch (Exception e) {
            logger.error("💥 Erreur notification bulletin WhatsApp pour {}: {}", 
                        parent.getWhatsappNumber(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Envoie un message général aux parents par WhatsApp
     */
    public String sendParentMessage(Parent parent, String subject, String message) {
        try {
            String formattedMessage = String.format(
                "🏫 *%s*\n\n" +
                "Bonjour %s,\n\n" +
                "%s\n\n" +
                "📅 %s\n" +
                "Cordialement,\n%s",
                subject,
                parent.getFirstName(),
                message,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                schoolName
            );
            
            return sendMessage(parent.getWhatsappNumber(), formattedMessage);
            
        } catch (Exception e) {
            logger.error("💥 Erreur message parent WhatsApp à {}: {}", 
                        parent.getWhatsappNumber(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Envoie une notification d'urgence par WhatsApp
     */
    public String sendUrgentNotification(Parent parent, String subject, String message) {
        try {
            String urgentMessage = String.format(
                "🚨 *URGENT - %s*\n\n" +
                "Bonjour %s,\n\n" +
                "%s\n\n" +
                "📅 %s\n" +
                "Cordialement,\n%s\n\n" +
                "_Message urgent - Merci de prendre connaissance rapidement_",
                subject,
                parent.getFirstName(),
                message,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                schoolName
            );
            
            return sendMessage(parent.getWhatsappNumber(), urgentMessage);
            
        } catch (Exception e) {
            logger.error("💥 Erreur notification urgente WhatsApp à {}: {}", 
                        parent.getWhatsappNumber(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Envoie une alerte d'absence par WhatsApp
     */
    public String sendAbsenceAlert(Parent parent, String studentName, String date, String reason) {
        try {
            String message = String.format(
                "⚠️ *Alerte Absence*\n\n" +
                "Bonjour %s,\n\n" +
                "Nous vous informons que *%s* a été marqué(e) absent(e) le *%s*.\n\n" +
                "Motif: %s\n\n" +
                "Si cette absence est justifiée, merci de nous faire parvenir un justificatif.\n\n" +
                "📅 %s\n" +
                "Cordialement,\n%s",
                parent.getFirstName(),
                studentName,
                date,
                reason != null ? reason : "Non spécifié",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                schoolName
            );
            
            return sendMessage(parent.getWhatsappNumber(), message);
            
        } catch (Exception e) {
            logger.error("💥 Erreur alerte absence WhatsApp à {}: {}", 
                        parent.getWhatsappNumber(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Vérifie le statut de WhatsApp Web
     */
    public Map<String, Object> checkWhatsAppStatus() {
        Map<String, Object> status = new HashMap<>();
        
        if (!whatsappEnabled) {
            status.put("enabled", false);
            status.put("connected", false);
            status.put("message", "WhatsApp Web API désactivé");
            return status;
        }

        try {
            String url = whatsappApiUrl + "/status";
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                status.put("enabled", true);
                status.put("connected", responseBody != null && 
                    Boolean.TRUE.equals(responseBody.get("connected")));
                status.put("ready", responseBody != null && 
                    Boolean.TRUE.equals(responseBody.get("ready")));
                status.put("qr_needed", responseBody != null && 
                    Boolean.TRUE.equals(responseBody.get("qr_needed")));
                status.put("message", "Connexion au serveur WhatsApp réussie");
            } else {
                status.put("enabled", true);
                status.put("connected", false);
                status.put("message", "Serveur WhatsApp non disponible");
            }

        } catch (Exception e) {
            status.put("enabled", true);
            status.put("connected", false);
            status.put("message", "Erreur de connexion: " + e.getMessage());
        }

        return status;
    }
    
    /**
     * Obtient le QR code WhatsApp
     */
    public String getQRCode() {
        if (!whatsappEnabled) {
            return null;
        }

        try {
            String url = whatsappApiUrl + "/qr";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("qr");
            }
            
        } catch (Exception e) {
            logger.error("❌ Erreur récupération QR code: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Valide un numéro WhatsApp
     */
    public boolean isValidWhatsAppNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Format international requis: +[country code][number]
        String whatsappRegex = "^\\+[1-9]\\d{1,14}$";
        return phoneNumber.matches(whatsappRegex);
    }
    
    /**
     * Obtient le statut d'un message
     */
    public String getMessageStatus(String messageId) {
        try {
            // Cette fonctionnalité nécessite les webhooks WhatsApp
            // Pour l'instant, on retourne un statut par défaut
            return "sent";
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération du statut du message {}: {}", messageId, e.getMessage());
            return "unknown";
        }
    }
    
    /**
     * Test de connectivité WhatsApp
     */
    public boolean testWhatsAppConnection() {
        try {
            Map<String, Object> status = checkWhatsAppStatus();
            return Boolean.TRUE.equals(status.get("connected"));
            
        } catch (Exception e) {
            logger.error("❌ Test connectivité WhatsApp échoué: {}", e.getMessage());
            return false;
        }
    }
}