@echo off
echo =====================================
echo   CONFIGURATION ENVOI WHATSAPP REEL
echo =====================================
echo.
echo Etape 1: Configurez vos tokens Facebook
echo.
echo Remplacez les valeurs ci-dessous par vos vrais tokens:
echo.
set WHATSAPP_API_URL=https://graph.facebook.com/v18.0
set WHATSAPP_PHONE_ID=VOTRE_PHONE_NUMBER_ID
set WHATSAPP_ACCESS_TOKEN=VOTRE_ACCESS_TOKEN
echo.
echo Etape 2: Test direct avec curl
echo.
echo curl -X POST https://graph.facebook.com/v18.0/VOTRE_PHONE_ID/messages ^
  -H "Authorization: Bearer VOTRE_ACCESS_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"messaging_product\":\"whatsapp\",\"to\":\"33123456789\",\"type\":\"text\",\"text\":{\"body\":\"Message de test depuis votre API\"}}"
echo.
echo Etape 3: Ou demarrez Spring Boot avec les nouveaux tokens
echo mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"
echo.
echo =====================================
echo     TOKENS FACEBOOK REQUIS:
echo =====================================
echo 1. Phone Number ID (depuis Facebook Developer Console)
echo 2. Access Token (Token permanent de votre app Facebook)
echo 3. Verify Token (Token de verification pour webhooks)
echo.
echo Lien: https://developers.facebook.com/apps/
echo =====================================
pause