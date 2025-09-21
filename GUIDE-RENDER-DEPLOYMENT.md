# 🚀 GUIDE DÉPLOIEMENT RENDER.COM - SPRING BOOT
## Guide Clean et À Jour (Septembre 2024)

---

## 📋 **PRÉREQUIS**

✅ **Votre projet est prêt** : Dockerfile créé + JAR testé
✅ **Compte GitHub** : Code poussé sur repository
✅ **Compte Render** : Inscription gratuite (pas de CB nécessaire)

---

## 🎯 **ÉTAPE 1 : PRÉPARER LE REPOSITORY GITHUB**

### 1.1 Vérifier que votre code est sur GitHub
```bash
# Si pas encore fait :
git remote add origin https://github.com/VOTRE-USERNAME/notification-service.git
git push -u origin main
```

### 1.2 Vérifier la structure
```
votre-repo/
├── Dockerfile ✅
├── .dockerignore ✅
├── pom.xml ✅
├── src/ ✅
└── target/notification-service.jar (après build)
```

---

## 🔐 **ÉTAPE 2 : CRÉER COMPTE RENDER.COM**

### 2.1 Inscription
1. 🌐 **Aller sur** : https://render.com
2. 🔄 **Cliquer** : "Get Started for Free"
3. 🔗 **Choisir** : "Sign up with GitHub" (recommandé)
4. ✅ **Autoriser** : Render à accéder à vos repos

### 2.2 Connexion GitHub
- ✅ Authorize Render dans GitHub
- ✅ Sélectionner repositories (All ou specific)

---

## 🚀 **ÉTAPE 3 : CRÉER LE WEB SERVICE**

### 3.1 Depuis le Dashboard Render
1. 📊 **Dashboard** : https://dashboard.render.com
2. ➕ **Cliquer** : "New +" (bouton bleu en haut à droite)
3. 🌐 **Sélectionner** : "Web Service"

### 3.2 Choisir la source
1. 🔗 **Option 1** : "Build and deploy from a Git repository" ⭐ RECOMMANDÉ
2. 📦 **Option 2** : "Deploy an existing image from a registry"

**➡️ CHOISIR OPTION 1** pour votre cas

### 3.3 Connecter le Repository
1. 🔍 **Trouver votre repo** : `notification-service`
2. 🔗 **Cliquer** : "Connect" à côté du nom
3. ⏳ **Attendre** : Render analyse votre repo

---

## ⚙️ **ÉTAPE 4 : CONFIGURATION DU SERVICE**

### 4.1 Informations de Base
```yaml
Name: notification-service           # Votre choix
Environment: Web Service            # Automatique
Region: Frankfurt (EU Central)      # Ou Oregon (US West)
Branch: main                        # Ou votre branche principale
```

### 4.2 Build Settings (Render détecte automatiquement)
```yaml
Language: Docker                    ✅ Auto-détecté
Build Command: [Automatic]          ✅ Via Dockerfile
Start Command: [Automatic]          ✅ Via ENTRYPOINT
```

### 4.3 Plan de Service
```yaml
Instance Type: Free                 ✅ 0$/mois
                                    • 512 MB RAM
                                    • Partagé CPU
                                    • 750h/mois
```

---

## 🔧 **ÉTAPE 5 : VARIABLES D'ENVIRONNEMENT**

### 5.1 Ajouter les Variables
📍 **Section "Environment Variables"** (avant de déployer)

**OBLIGATOIRES** :
```bash
SPRING_PROFILES_ACTIVE=production
DATABASE_URL=postgresql://... # (sera généré automatiquement si vous ajoutez PostgreSQL)
```

**POUR EMAIL** :
```bash
EMAIL_USERNAME=ecampusnsia@gmail.com
EMAIL_PASSWORD=czhxnadyqeezfqni
EMAIL_FROM=ecampusnsia@gmail.com
```

**OPTIONNELLES** :
```bash
WHATSAPP_ENABLED=false
API_DOCS_ENABLED=true          # Pour garder Swagger accessible
SWAGGER_ENABLED=true
```

### 5.2 Comment ajouter une variable
1. 🔽 **Section** : "Environment Variables"
2. ➕ **Cliquer** : "Add Environment Variable"
3. 📝 **Saisir** : Key = `SPRING_PROFILES_ACTIVE`, Value = `production`
4. 🔄 **Répéter** pour chaque variable

---

## 🐘 **ÉTAPE 6 : AJOUTER BASE DE DONNÉES (OPTIONNEL)**

### 6.1 PostgreSQL Gratuit
1. ➕ **Cliquer** : "New +" dans Dashboard
2. 🗄️ **Sélectionner** : "PostgreSQL"
3. 📝 **Nommer** : `notification-db`
4. 💰 **Plan** : Free (1 GB)

### 6.2 Connexion Automatique
- ✅ Render va générer `DATABASE_URL` automatiquement
- ✅ L'injecter dans votre Web Service
- ✅ Format : `postgresql://user:pass@host:port/dbname`

---

## 🎯 **ÉTAPE 7 : LANCER LE DÉPLOIEMENT**

### 7.1 Vérification Finale
```yaml
✅ Repository connecté : notification-service
✅ Language détecté : Docker
✅ Branch : main
✅ Variables ajoutées : SPRING_PROFILES_ACTIVE, etc.
✅ Instance Type : Free
```

### 7.2 Déployer
1. 🚀 **Cliquer** : "Create Web Service" (bouton vert)
2. ⏳ **Attendre** : Le build commence automatiquement
3. 📊 **Suivre** : Les logs en temps réel

---

## 📊 **ÉTAPE 8 : SUIVRE LE DÉPLOIEMENT**

### 8.1 Logs de Build
```bash
==> Starting build
==> Installing dependencies
==> Building application
==> Creating docker image
==> Starting docker container
==> Health check passed
==> Deploy succeeded ✅
```

### 8.2 Temps de Déploiement
- ⏱️ **Premier deploy** : 5-10 minutes
- ⚡ **Deploys suivants** : 2-5 minutes

---

## ✅ **ÉTAPE 9 : TESTER VOTRE APPLICATION**

### 9.1 URL de votre app
```
Format : https://notification-service-xxxx.onrender.com
Example: https://notification-service-abc123.onrender.com
```

### 9.2 Endpoints à tester
```bash
# Health Check
GET https://votre-app.onrender.com/actuator/health

# API Documentation
GET https://votre-app.onrender.com/swagger-ui.html

# API Endpoints
GET https://votre-app.onrender.com/api/notifications/health
GET https://votre-app.onrender.com/api/parents
```

---

## 🔧 **ÉTAPE 10 : MAINTENANCE & UPDATES**

### 10.1 Déploiements Automatiques
- ✅ **Push sur GitHub** = **Déploiement automatique**
- ✅ **Logs visibles** dans Dashboard Render
- ✅ **Rollback possible** en 1 clic

### 10.2 Monitoring
```bash
# Dashboard Render
- CPU/Memory usage
- Response times
- Error rates
- Request counts
```

---

## 🚨 **TROUBLESHOOTING**

### Problème : Build Failed
```bash
❌ Error: No such file or directory
✅ Solution: Vérifier Dockerfile (déjà corrigé chez vous)
```

### Problème : Application Timeout
```bash
❌ Error: Health check failed
✅ Solution: Vérifier PORT variable (déjà configuré)
```

### Problème : Database Connection
```bash
❌ Error: Connection refused
✅ Solution: Ajouter DATABASE_URL dans env variables
```

---

## 🎯 **RÉSUMÉ RAPIDE**

1. 🔗 **GitHub** : Push your code
2. 🌐 **Render** : Sign up + Connect repo
3. ➕ **New** : Web Service
4. ⚙️ **Config** : Variables + Database
5. 🚀 **Deploy** : Click & Wait
6. ✅ **Test** : Your app is LIVE!

---

## 📞 **SUPPORT**

- 📚 **Docs** : https://render.com/docs
- 💬 **Community** : https://community.render.com
- 🎯 **Status** : https://status.render.com

---

**🎉 VOTRE SPRING BOOT EST MAINTENANT EN LIGNE ! 🎉**