package com.school.notification_service.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.school.notification_service.enumr.MessageChannel;

public class CreateParentRequest {
    
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;
    
    @Email(message = "Email invalide")
    private String email;
    
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Numéro WhatsApp invalide")
    private String whatsappNumber;
    
    private MessageChannel preferredChannel = MessageChannel.BOTH;
    
    // Constructeur par défaut OBLIGATOIRE
    public CreateParentRequest() {}
    
    // Constructeur avec paramètres
    public CreateParentRequest(String firstName, String lastName, String email, String whatsappNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.whatsappNumber = whatsappNumber;
    }
    
    // GETTERS ET SETTERS OBLIGATOIRES
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getWhatsappNumber() {
        return whatsappNumber;
    }
    
    public void setWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
    }
    
    public MessageChannel getPreferredChannel() {
        return preferredChannel;
    }
    
    public void setPreferredChannel(MessageChannel preferredChannel) {
        this.preferredChannel = preferredChannel;
    }
    
    @Override
    public String toString() {
        return "CreateParentRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", whatsappNumber='" + whatsappNumber + '\'' +
                ", preferredChannel=" + preferredChannel +
                '}';
    }
}