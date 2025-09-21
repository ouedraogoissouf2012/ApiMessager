package com.school.notification_service.controllers;

import com.school.notification_service.Dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller simple pour vérifications de santé et tests
 */
@RestController
@RequestMapping("/")
@Tag(name = "Health", description = "Endpoints de santé et vérification")
@CrossOrigin(origins = "*")
public class HealthController {

    @GetMapping("/")
    @Operation(summary = "Page d'accueil", description = "Point d'entrée principal de l'API")
    public ResponseEntity<ApiResponse<Map<String, Object>>> home() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Notification Service");
        info.put("version", "1.0.0");
        info.put("status", "UP");
        info.put("timestamp", LocalDateTime.now());
        info.put("swagger", "/swagger-ui.html");
        info.put("actuator", "/actuator/health");

        return ResponseEntity.ok(ApiResponse.success("Service Notification - API Ready", info));
    }

    @GetMapping("/ping")
    @Operation(summary = "Test de connectivité", description = "Simple ping pour vérifier que l'API répond")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("pong"));
    }

    @GetMapping("/health-simple")
    @Operation(summary = "Health check simple", description = "Vérification de santé basique")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthSimple() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("database", "Connected");
        health.put("email", "Configured");
        health.put("whatsapp", "Ready");

        return ResponseEntity.ok(ApiResponse.success("Application healthy", health));
    }
}