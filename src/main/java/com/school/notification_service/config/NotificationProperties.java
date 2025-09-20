package com.school.notification_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

	 private WhatsApp whatsapp = new WhatsApp();
	    private Email email = new Email();
	    private Scheduling scheduling = new Scheduling();
	    
	    // Getters et Setters
	    public WhatsApp getWhatsapp() { return whatsapp; }
	    public void setWhatsapp(WhatsApp whatsapp) { this.whatsapp = whatsapp; }
	    
	    public Email getEmail() { return email; }
	    public void setEmail(Email email) { this.email = email; }
	    
	    public Scheduling getScheduling() { return scheduling; }
	    public void setScheduling(Scheduling scheduling) { this.scheduling = scheduling; }
	    
	   // Classes internes pour les configurations
	    public static class WhatsApp {
	        private String apiUrl;
	        private String phoneNumberId;
	        private String accessToken;
	        private String verifyToken;
	        
	        // Getters et Setters
	        public String getApiUrl() { return apiUrl; }
	        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
	        
	        public String getPhoneNumberId() { return phoneNumberId; }
	        public void setPhoneNumberId(String phoneNumberId) { this.phoneNumberId = phoneNumberId; }
	        
	        public String getAccessToken() { return accessToken; }
	        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
	        
	        public String getVerifyToken() { return verifyToken; }
	        public void setVerifyToken(String verifyToken) { this.verifyToken = verifyToken; }
	    }
	    public static class Email {
	        private String from;
	        private String fromName;
	        
	        // Getters et Setters
	        public String getFrom() { return from; }
	        public void setFrom(String from) { this.from = from; }
	        
	        public String getFromName() { return fromName; }
	        public void setFromName(String fromName) { this.fromName = fromName; }
	    }
	    
	    public static class Scheduling {
	        private boolean enabled = true;
	        private int batchSize = 50;
	        private int retryAttempts = 3;
	        private long retryDelay = 5000;
	        
	        // Getters et Setters
	        public boolean isEnabled() { return enabled; }
	        public void setEnabled(boolean enabled) { this.enabled = enabled; }
	        
	        public int getBatchSize() { return batchSize; }
	        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
	        
	        public int getRetryAttempts() { return retryAttempts; }
	        public void setRetryAttempts(int retryAttempts) { this.retryAttempts = retryAttempts; }
	        
	        public long getRetryDelay() { return retryDelay; }
	        public void setRetryDelay(long retryDelay) { this.retryDelay = retryDelay; }
	    }
}
