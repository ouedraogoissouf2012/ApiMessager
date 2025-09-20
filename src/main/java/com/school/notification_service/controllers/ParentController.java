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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school.notification_service.Dto.ApiResponse;
import com.school.notification_service.Dto.CreateParentRequest;
import com.school.notification_service.entities.Parent;
import com.school.notification_service.services.ParentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/parents")
@Tag(name = "Parents", description = "API de gestion des parents")
@CrossOrigin(origins = "*")
public class ParentController {

private static final Logger logger = LoggerFactory.getLogger(ParentController.class);
    
    @Autowired
    private ParentService parentService;
    
   
    @PostMapping
    @Operation(summary = "Créer un nouveau parent", 
               description = "Ajoute un nouveau parent dans le système")
    public ResponseEntity<ApiResponse<Parent>> createParent(
            @Valid @RequestBody CreateParentRequest request) {
        
        try {
            Parent parent = parentService.createParent(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Parent créé avec succès", parent));
                    
        } catch (Exception e) {
            logger.error("Erreur lors de la création du parent: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de la création: " + e.getMessage()));
        }
    }
    
    /**
     * Met à jour un parent existant
     */
    @PutMapping("/{parentId}")
    @Operation(summary = "Mettre à jour un parent", 
               description = "Modifie les informations d'un parent existant")
    public ResponseEntity<ApiResponse<Parent>> updateParent(
            @Parameter(description = "ID du parent") @PathVariable Long parentId,
            @Valid @RequestBody CreateParentRequest request) {
        
        try {
            Parent parent = parentService.updateParent(parentId, request);
            
            return ResponseEntity.ok(ApiResponse.success("Parent mis à jour avec succès", parent));
            
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du parent: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }
    
    /**
     * Récupère un parent par ID
     */
    @GetMapping("/{parentId}")
    @Operation(summary = "Récupérer un parent par ID", 
               description = "Obtient les détails d'un parent spécifique")
    public ResponseEntity<ApiResponse<Parent>> getParentById(
            @Parameter(description = "ID du parent") @PathVariable Long parentId) {
        
        try {
            Parent parent = parentService.getParentById(parentId);
            
            return ResponseEntity.ok(ApiResponse.success(parent));
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du parent: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Parent non trouvé: " + e.getMessage()));
        }
    }
    
    /**
     * Récupère tous les parents actifs
     */
    @GetMapping
    @Operation(summary = "Lister tous les parents actifs", 
               description = "Récupère la liste de tous les parents actifs")
    public ResponseEntity<ApiResponse<List<Parent>>> getAllActiveParents() {
        
        try {
            List<Parent> parents = parentService.getAllActiveParents();
            
            return ResponseEntity.ok(ApiResponse.success(
                String.format("%d parents actifs trouvés", parents.size()), parents));
                
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des parents: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la récupération: " + e.getMessage()));
        }
    }
    
    /**
     * Recherche des parents par nom
     */
    @GetMapping("/search")
    @Operation(summary = "Rechercher des parents par nom", 
               description = "Recherche des parents par prénom ou nom de famille")
    public ResponseEntity<ApiResponse<List<Parent>>> searchParentsByName(
            @Parameter(description = "Terme de recherche") @RequestParam String name) {
        
        try {
            List<Parent> parents = parentService.searchParentsByName(name);
            
            return ResponseEntity.ok(ApiResponse.success(
                String.format("%d parents trouvés pour '%s'", parents.size(), name), parents));
                
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de parents: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la recherche: " + e.getMessage()));
        }
    }
    
    /**
     * Active ou désactive un parent
     */
    @PatchMapping("/{parentId}/toggle-status")
    @Operation(summary = "Activer/désactiver un parent", 
               description = "Change le statut actif/inactif d'un parent")
    public ResponseEntity<ApiResponse<Parent>> toggleParentStatus(
            @Parameter(description = "ID du parent") @PathVariable Long parentId) {
        
        try {
            Parent parent = parentService.toggleParentStatus(parentId);
            
            String status = parent.getIsActive() ? "activé" : "désactivé";
            return ResponseEntity.ok(ApiResponse.success(
                String.format("Parent %s avec succès", status), parent));
                
        } catch (Exception e) {
            logger.error("Erreur lors du changement de statut: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors du changement de statut: " + e.getMessage()));
        }
    }
    
    /**
     * Obtient les statistiques des parents
     */
    @GetMapping("/statistics")
    @Operation(summary = "Statistiques des parents", 
               description = "Récupère les statistiques générales des parents")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getParentStatistics() {
        
        try {
            Map<String, Object> stats = parentService.getParentStatistics();
            
            return ResponseEntity.ok(ApiResponse.success("Statistiques récupérées", stats));
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la récupération des statistiques: " + e.getMessage()));
        }
    }
}
