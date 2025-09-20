# Dockerfile pour Railway déploiement
FROM openjdk:21-jdk-slim

# Variables d'environnement
ENV SPRING_PROFILES_ACTIVE=railway

# Créer répertoire app
WORKDIR /app

# Copier le JAR
COPY target/notification-service-*.jar app.jar

# Exposer le port
EXPOSE 8080

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]