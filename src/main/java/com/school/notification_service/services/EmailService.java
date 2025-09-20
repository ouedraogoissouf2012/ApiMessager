package com.school.notification_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.thymeleaf.context.Context; // ✅ AJOUTEZ CETTE LIGNE
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;

import com.school.notification_service.entities.Parent;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${notification.email.from}")
    private String fromEmail;
    
    @Value("${notification.email.from-name}")
    private String fromName;
    
    @Value("${notification.school.name}")
    private String schoolName;
    
    /**
     * Envoie un email simple
     */
    public boolean sendSimpleEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true pour HTML
            
            mailSender.send(message);
            logger.info("Email envoyé avec succès à: {}", to);
            return true;
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email à {}: {}", to, e.getMessage());
            return false;
        }
    }
    
    /**
     * Envoie un email avec template
     */
    public boolean sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            // Préparer le contexte Thymeleaf
            Context context = new Context();
            context.setVariables(variables);
            
            // Générer le contenu HTML à partir du template
            String htmlContent = templateEngine.process(templateName, context);
            
            // Créer et envoyer l'email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("Email avec template '{}' envoyé avec succès à: {}", templateName, to);
            return true;
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email avec template à {}: {}", to, e.getMessage());
            return false;
        }
    }
    
    /**
     * Envoie une notification de bulletin disponible
     */
    public boolean sendBulletinNotification(Parent parent, String studentName, String period) {
        try {
            Map<String, Object> variables = Map.of(
                "parentName", parent.getFullName(),
                "studentName", studentName,
                "period", period,
                "currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                "schoolName", schoolName
            );
            
            String subject = String.format("Bulletin de %s disponible - %s", studentName, period);
            
            return sendTemplatedEmail(parent.getEmail(), subject, "bulletin-notification", variables);
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de bulletin pour {}: {}", 
                        parent.getEmail(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Envoie un message général aux parents
     */
    public boolean sendParentMessage(Parent parent, String subject, String message) {
        try {
            Map<String, Object> variables = Map.of(
                "parentName", parent.getFullName(),
                "messageContent", message,
                "currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                "schoolName", schoolName
            );
            
            return sendTemplatedEmail(parent.getEmail(), subject, "parent-message", variables);
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du message parent à {}: {}", 
                        parent.getEmail(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Envoie une notification d'urgence
     */
    public boolean sendUrgentNotification(Parent parent, String subject, String message) {
        try {
            Map<String, Object> variables = Map.of(
                "parentName", parent.getFullName(),
                "urgentMessage", message,
                "currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                "schoolName", schoolName
            );
            
            String urgentSubject = "🚨 URGENT - " + subject;
            
            return sendTemplatedEmail(parent.getEmail(), urgentSubject, "urgent-notification", variables);
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification urgente à {}: {}", 
                        parent.getEmail(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Envoie une notification d'absence
     */
    public boolean sendAbsenceAlert(Parent parent, String studentName, String date, String reason) {
        try {
            Map<String, Object> variables = Map.of(
                "parentName", parent.getFullName(),
                "studentName", studentName,
                "absenceDate", date,
                "reason", reason != null ? reason : "Non spécifiée",
                "currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                "schoolName", schoolName
            );
            
            String subject = String.format("Absence de %s - %s", studentName, date);
            
            return sendTemplatedEmail(parent.getEmail(), subject, "absence-alert", variables);
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'alerte d'absence à {}: {}", 
                        parent.getEmail(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Valide une adresse email
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Test de connectivité email
     */
    public boolean testEmailConnection() {
        try {
            // Tenter d'envoyer un email de test
            return sendSimpleEmail(fromEmail, "Test de connectivité", 
                                 "<p>Email de test envoyé avec succès.</p>");
        } catch (Exception e) {
            logger.error("Test de connectivité email échoué: {}", e.getMessage());
            return false;
        }
    }
}
