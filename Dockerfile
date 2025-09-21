# Dockerfile optimisé pour Render.com
# Multi-stage build pour Spring Boot avec Maven

# Stage 1: Build avec Maven
FROM maven:3.9.5-eclipse-temurin-21 AS builder

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers de configuration Maven d'abord (pour cache layer)
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Télécharger les dépendances (cache layer séparé)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Build l'application (skip tests pour éviter problèmes de déploiement)
RUN mvn clean package -DskipTests -B

# Vérifier que le JAR a été créé
RUN ls -la target/

# Stage 2: Runtime avec JRE minimal
FROM eclipse-temurin:21-jre-alpine AS runtime

# Variables d'environnement pour Render
ENV SPRING_PROFILES_ACTIVE=production

# Créer utilisateur non-root pour sécurité
RUN addgroup -g 1001 -S spring && \
    adduser -u 1001 -S spring -G spring

# Répertoire de travail
WORKDIR /app

# Copier le JAR depuis le stage builder
COPY --from=builder /app/target/notification-service.jar app.jar

# Changer propriétaire
RUN chown spring:spring app.jar

# Utiliser l'utilisateur non-root
USER spring

# Exposer le port (Render détecte automatiquement)
EXPOSE 8080

# Health check pour Render
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Commande de démarrage optimisée pour conteneur
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dserver.port=${PORT:-8080}", \
    "-jar", \
    "app.jar"]