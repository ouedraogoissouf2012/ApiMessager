package com.school.notification_service.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.school.notification_service.Dto.CreateParentRequest;
import com.school.notification_service.entities.Parent;
import com.school.notification_service.enumr.MessageChannel;
import com.school.notification_service.repositories.ParentRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ParentService {

	 private static final Logger logger = LoggerFactory.getLogger(ParentService.class);
	    
	    @Autowired
	    private ParentRepository parentRepository;
	    
	    /**
	     * Crée un nouveau parent
	     */
	    public Parent createParent(CreateParentRequest request) {
	        logger.info("Création d'un nouveau parent: {} {}", request.getFirstName(), request.getLastName());
	        
	        // Vérifier si l'email existe déjà
	        if (request.getEmail() != null && parentRepository.existsByEmail(request.getEmail())) {
	            throw new RuntimeException("Un parent avec cet email existe déjà: " + request.getEmail());
	        }
	        
	        // Vérifier si le numéro WhatsApp existe déjà
	        if (request.getWhatsappNumber() != null && parentRepository.existsByWhatsappNumber(request.getWhatsappNumber())) {
	            throw new RuntimeException("Un parent avec ce numéro WhatsApp existe déjà: " + request.getWhatsappNumber());
	        }
	        
	        // Valider les données
	        validateParentData(request);
	        
	        // Créer le parent
	        Parent parent = new Parent();
	        parent.setFirstName(request.getFirstName());
	        parent.setLastName(request.getLastName());
	        parent.setEmail(request.getEmail());
	        parent.setWhatsappNumber(request.getWhatsappNumber());
	        parent.setPreferredChannel(request.getPreferredChannel());
	        parent.setIsActive(true);
	        
	        parent = parentRepository.save(parent);
	        logger.info("Parent créé avec succès - ID: {}", parent.getId());
	        
	        return parent;
	    }
	    
	    /**
	     * Met à jour un parent existant
	     */
	    public Parent updateParent(Long parentId, CreateParentRequest request) {
	        logger.info("Mise à jour du parent ID: {}", parentId);
	        
	        Optional<Parent> parentOpt = parentRepository.findById(parentId);
	        if (!parentOpt.isPresent()) {
	            throw new RuntimeException("Parent non trouvé avec l'ID: " + parentId);
	        }
	        
	        Parent parent = parentOpt.get();
	        
	        // Vérifier les conflits d'email (si changé)
	        if (request.getEmail() != null && !request.getEmail().equals(parent.getEmail())) {
	            if (parentRepository.existsByEmail(request.getEmail())) {
	                throw new RuntimeException("Un autre parent utilise déjà cet email: " + request.getEmail());
	            }
	        }
	        
	        // Vérifier les conflits de numéro WhatsApp (si changé)
	        if (request.getWhatsappNumber() != null && !request.getWhatsappNumber().equals(parent.getWhatsappNumber())) {
	            if (parentRepository.existsByWhatsappNumber(request.getWhatsappNumber())) {
	                throw new RuntimeException("Un autre parent utilise déjà ce numéro WhatsApp: " + request.getWhatsappNumber());
	            }
	        }
	        
	        // Valider les nouvelles données
	        validateParentData(request);
	        
	        // Mettre à jour les champs
	        parent.setFirstName(request.getFirstName());
	        parent.setLastName(request.getLastName());
	        parent.setEmail(request.getEmail());
	        parent.setWhatsappNumber(request.getWhatsappNumber());
	        parent.setPreferredChannel(request.getPreferredChannel());
	        
	        parent = parentRepository.save(parent);
	        logger.info("Parent mis à jour avec succès - ID: {}", parent.getId());
	        
	        return parent;
	    }
	    
	    /**
	     * Récupère un parent par ID
	     */
	    public Parent getParentById(Long parentId) {
	        Optional<Parent> parentOpt = parentRepository.findById(parentId);
	        if (!parentOpt.isPresent()) {
	            throw new RuntimeException("Parent non trouvé avec l'ID: " + parentId);
	        }
	        return parentOpt.get();
	    }
	    
	    /**
	     * Récupère un parent par email
	     */
	    public Optional<Parent> getParentByEmail(String email) {
	        return parentRepository.findByEmail(email);
	    }
	    
	    /**
	     * Récupère un parent par numéro WhatsApp
	     */
	    public Optional<Parent> getParentByWhatsAppNumber(String whatsappNumber) {
	        return parentRepository.findByWhatsappNumber(whatsappNumber);
	    }
	    
	    /**
	     * Récupère tous les parents actifs
	     */
	    public List<Parent> getAllActiveParents() {
	        return parentRepository.findByIsActiveTrue();
	    }
	    
	    /**
	     * Récupère tous les parents (actifs et inactifs)
	     */
	    public List<Parent> getAllParents() {
	        return parentRepository.findAll();
	    }
	    
	    /**
	     * Recherche des parents par nom
	     */
	    public List<Parent> searchParentsByName(String name) {
	        return parentRepository.findByNameContainingIgnoreCase(name);
	    }
	    
	    /**
	     * Active ou désactive un parent
	     */
	    public Parent toggleParentStatus(Long parentId) {
	        Parent parent = getParentById(parentId);
	        parent.setIsActive(!parent.getIsActive());
	        parent = parentRepository.save(parent);
	        
	        logger.info("Statut du parent ID {} changé à: {}", parentId, parent.getIsActive() ? "Actif" : "Inactif");
	        return parent;
	    }
	    
	    /**
	     * Désactive un parent
	     */
	    public Parent deactivateParent(Long parentId) {
	        Parent parent = getParentById(parentId);
	        parent.setIsActive(false);
	        parent = parentRepository.save(parent);
	        
	        logger.info("Parent ID {} désactivé", parentId);
	        return parent;
	    }
	    
	    /**
	     * Réactive un parent
	     */
	    public Parent reactivateParent(Long parentId) {
	        Parent parent = getParentById(parentId);
	        parent.setIsActive(true);
	        parent = parentRepository.save(parent);
	        
	        logger.info("Parent ID {} réactivé", parentId);
	        return parent;
	    }
	    
	    /**
	     * Met à jour les préférences de canal d'un parent
	     */
	    public Parent updateParentChannelPreference(Long parentId, MessageChannel preferredChannel) {
	        Parent parent = getParentById(parentId);
	        parent.setPreferredChannel(preferredChannel);
	        parent = parentRepository.save(parent);
	        
	        logger.info("Préférence de canal du parent ID {} mise à jour: {}", parentId, preferredChannel);
	        return parent;
	    }
	    
	    /**
	     * Supprime définitivement un parent (avec vérifications)
	     */
	    public void deleteParent(Long parentId) {
	        Parent parent = getParentById(parentId);
	        
	        // Vérifier s'il y a des notifications associées
	        // En production, vous pourriez vouloir vérifier d'autres relations
	        
	        parentRepository.delete(parent);
	        logger.info("Parent ID {} supprimé définitivement", parentId);
	    }
	    
	    /**
	     * Valide les données d'un parent
	     */
	    private void validateParentData(CreateParentRequest request) {
	        // Validation du prénom
	        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
	            throw new RuntimeException("Le prénom est obligatoire");
	        }
	        if (request.getFirstName().length() > 50) {
	            throw new RuntimeException("Le prénom ne peut pas dépasser 50 caractères");
	        }
	        
	        // Validation du nom
	        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
	            throw new RuntimeException("Le nom est obligatoire");
	        }
	        if (request.getLastName().length() > 50) {
	            throw new RuntimeException("Le nom ne peut pas dépasser 50 caractères");
	        }
	        
	        // Validation de l'email (si fourni)
	        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
	            if (!isValidEmail(request.getEmail())) {
	                throw new RuntimeException("Format d'email invalide");
	            }
	        }
	        
	        // Validation du numéro WhatsApp (si fourni)
	        if (request.getWhatsappNumber() != null && !request.getWhatsappNumber().trim().isEmpty()) {
	            if (!isValidWhatsAppNumber(request.getWhatsappNumber())) {
	                throw new RuntimeException("Format de numéro WhatsApp invalide (format requis: +1234567890)");
	            }
	        }
	        
	        // Au moins un moyen de contact doit être fourni
	        if ((request.getEmail() == null || request.getEmail().trim().isEmpty()) &&
	            (request.getWhatsappNumber() == null || request.getWhatsappNumber().trim().isEmpty())) {
	            throw new RuntimeException("Au moins un email ou un numéro WhatsApp doit être fourni");
	        }
	        
	        // Validation du canal préféré en fonction des moyens de contact disponibles
	        if (request.getPreferredChannel() != null) {
	            validateChannelAgainstAvailableContacts(request);
	        }
	    }
	    
	    /**
	     * Valide que le canal préféré est compatible avec les moyens de contact disponibles
	     */
	    private void validateChannelAgainstAvailableContacts(CreateParentRequest request) {
	        boolean hasEmail = request.getEmail() != null && !request.getEmail().trim().isEmpty();
	        boolean hasWhatsApp = request.getWhatsappNumber() != null && !request.getWhatsappNumber().trim().isEmpty();
	        
	        switch (request.getPreferredChannel()) {
	            case EMAIL:
	                if (!hasEmail) {
	                    throw new RuntimeException("Impossible de choisir EMAIL comme canal préféré sans adresse email");
	                }
	                break;
	            case WHATSAPP:
	                if (!hasWhatsApp) {
	                    throw new RuntimeException("Impossible de choisir WHATSAPP comme canal préféré sans numéro WhatsApp");
	                }
	                break;
	            case BOTH:
	                if (!hasEmail && !hasWhatsApp) {
	                    throw new RuntimeException("Impossible de choisir BOTH comme canal préféré sans moyen de contact");
	                }
	                break;
	        }
	    }
	    
	    /**
	     * Valide un format d'email
	     */
	    private boolean isValidEmail(String email) {
	        if (email == null || email.trim().isEmpty()) {
	            return false;
	        }
	        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
	        return email.matches(emailRegex);
	    }
	    
	    /**
	     * Valide un format de numéro WhatsApp
	     */
	    private boolean isValidWhatsAppNumber(String phoneNumber) {
	        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
	            return false;
	        }
	        String whatsappRegex = "^\\+[1-9]\\d{1,14}$";
	        return phoneNumber.matches(whatsappRegex);
	    }
	    
	    /**
	     * Obtient des statistiques sur les parents
	     */
	    public Map<String, Object> getParentStatistics() {
	        Map<String, Object> stats = new HashMap<>();
	        
	        long totalParents = parentRepository.count();
	        long activeParents = parentRepository.countByIsActiveTrue();
	        long inactiveParents = totalParents - activeParents;
	        
	        stats.put("total", totalParents);
	        stats.put("active", activeParents);
	        stats.put("inactive", inactiveParents);
	        
	        // Statistiques par canal préféré
	        List<Parent> allParents = parentRepository.findAll();
	        Map<String, Long> channelStats = allParents.stream()
	                .collect(Collectors.groupingBy(
	                    parent -> parent.getPreferredChannel().toString(),
	                    Collectors.counting()
	                ));
	        stats.put("byPreferredChannel", channelStats);
	        
	        return stats;
	    }
}
