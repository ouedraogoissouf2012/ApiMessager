package com.school.notification_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.school.notification_service.entities.Parent;
import com.school.notification_service.enumr.MessageChannel;
import com.school.notification_service.repositories.ParentRepository;

@Component
@Profile({"default", "dev", "test"})
public class DataInitializer implements CommandLineRunner{

	 private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
	    
	    @Autowired
	    private ParentRepository parentRepository;
	    
	    @Override
	    public void run(String... args) throws Exception {
	        logger.info("Initialisation des données de test...");
	        
	        // Vérifier si des données existent déjà
	        if (parentRepository.count() > 0) {
	            logger.info("Des données existent déjà, initialisation ignorée");
	            return;
	        }
	        
	        // Créer des parents de test
	        createTestParents();
	        
	        logger.info("Données de test initialisées avec succès");
	    }
	    
	    private void createTestParents() {
	        // Parent 1 - Email uniquement
	        Parent parent1 = new Parent();
	        parent1.setFirstName("Issouf");
	        parent1.setLastName("Ouedraogo");
	        parent1.setEmail("issouf.ouedraogo@email.com");
	        parent1.setPreferredChannel(MessageChannel.EMAIL);
	        parent1.setIsActive(true);
	        parentRepository.save(parent1);
	        
	        // Parent 2 - WhatsApp uniquement
	        Parent parent2 = new Parent();
	        parent2.setFirstName("Jean");
	        parent2.setLastName("Martin");
	        parent2.setWhatsappNumber("+33123456789");
	        parent2.setPreferredChannel(MessageChannel.WHATSAPP);
	        parent2.setIsActive(true);
	        parentRepository.save(parent2);
	        
	        // Parent 3 - Les deux canaux
	        Parent parent3 = new Parent();
	        parent3.setFirstName("Sophie");
	        parent3.setLastName("Bernard");
	        parent3.setEmail("sophie.bernard@email.com");
	        parent3.setWhatsappNumber("+33987654321");
	        parent3.setPreferredChannel(MessageChannel.BOTH);
	        parent3.setIsActive(true);
	        parentRepository.save(parent3);
	        
	        // Parent 4 - Parent inactif
	        Parent parent4 = new Parent();
	        parent4.setFirstName("Pierre");
	        parent4.setLastName("Moreau");
	        parent4.setEmail("pierre.moreau@email.com");
	        parent4.setWhatsappNumber("+33555666777");
	        parent4.setPreferredChannel(MessageChannel.BOTH);
	        parent4.setIsActive(false);
	        parentRepository.save(parent4);
	        
	        logger.info("4 parents de test créés");
	    }
	    
}
