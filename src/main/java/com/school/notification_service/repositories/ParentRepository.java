package com.school.notification_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.school.notification_service.entities.Parent;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long>{


	// Trouver un parent par email
    Optional<Parent> findByEmail(String email);
    
    // Trouver un parent par numéro WhatsApp
    Optional<Parent> findByWhatsappNumber(String whatsappNumber);
    
    // Trouver tous les parents actifs
    List<Parent> findByIsActiveTrue();
    
    // Recherche par nom (insensible à la casse)
    @Query("SELECT p FROM Parent p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Parent> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Compter les parents actifs
    long countByIsActiveTrue();
    
    // Vérifier si un email existe déjà
    boolean existsByEmail(String email);
    
    // Vérifier si un numéro WhatsApp existe déjà
    boolean existsByWhatsappNumber(String whatsappNumber);
    

}
