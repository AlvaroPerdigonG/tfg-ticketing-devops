# TFG – Plataforma de Ticketing amb Arquitectura DevOps

Aquest repositori conté el codi font del Treball de Final de Grau titulat:

**“Disseny i implementació d’una plataforma de ticketing amb una arquitectura DevOps i desplegament al núvol”**

El projecte té com a objectiu desenvolupar una aplicació web de ticketing com a cas d’estudi per aplicar pràctiques modernes d’enginyeria del programari basades en DevOps, integració contínua, desplegament continu, control de qualitat automatitzat i contenidorització.

---

## 🧭 Context acadèmic

Aquest projecte forma part del Treball de Final de Grau del Grau en Enginyeria Informàtica.  
El repositori és públic amb finalitats educatives i de recerca aplicada, i pretén servir com a exemple pràctic d’una arquitectura DevOps completa aplicada a una aplicació full-stack.

---

## 🛠️ Tecnologies principals

### Backend
- Java 17
- Spring Boot
- JPA / Hibernate
- PostgreSQL

### Frontend
- React
- TypeScript
- Vite

### DevOps i Qualitat
- GitHub Actions (CI/CD)
- Docker i Docker Compose
- SonarCloud
- ESLint / Checkstyle
- JUnit, REST Assured, Testcontainers
- React Testing Library

### Infraestructura
- Desplegament en entorn de núvol

- ## ⚖️ Llicència

Aquest projecte es distribueix sota llicència **MIT**.  
Consulta el fitxer [LICENSE](LICENSE) per a més informació.

---

## 👤 Autor

Treball de Final de Grau realitzat per:

**Álvaro Perdigón Gordillo**  
Grau en Enginyeria Informàtica de Gestió i Sistemes d'Informació - Tecnocampus

---

## 📬 Contacte

Per a consultes acadèmiques relacionades amb aquest projecte, es pot contactar amb l’autor mitjançant el repositori GitHub.

---

## 🧰 Prerequisits i execució local

Per executar el projecte en un entorn local és necessari disposar de les següents eines instal·lades:

### Requisits generals
- Git
- Docker (Docker Desktop o Docker Engine)

### Backend
- Java Development Kit (JDK) 17 (LTS)
- Maven (o Maven Wrapper inclòs al projecte)
- IntelliJ IDEA (recomanat)

### Frontend
- Node.js (versió LTS, 20.x recomanada)
- npm (inclòs amb Node.js)
- Visual Studio Code (recomanat)

---

## ▶ Execució del projecte en local

### 1. Clonar el repositori

```bash
git clone https://github.com/AlvaroPerdigonG/tfg-ticketing-devops.git
cd tfg-ticketing-devops
```

### 2. Iniciar la base de dades amb Docker
El projecte utilitza una base de dades PostgreSQL executada en un contenidor Docker.
```bash
docker-compose up -d
```

### 3. Executar el backend
```bash
cd backend
./mvnw spring-boot:run
```
El backend quedarà accessible a: http://localhost:8080

### 4. Executar el frontend
Des del directori frontend:
```bash
cd frontend
npm install
npm run dev
```
El frontend quedarà accessible a: http://localhost:5173

### ⚙ Configuració
Les variables de configuració s’estableixen mitjançant fitxers d’entorn i perfils d’execució:

Backend: application.yml / application-local.yml

Frontend: .env

No s’inclouen credencials ni secrets al repositori.
Es proporciona un fitxer d’exemple per a la configuració (.env.example).

### 🧪 Execució de proves
Backend:
```bash
cd backend
./mvnw test
```
Frontend:
```bash
cd frontend
npm run test
```

### 🐳 Execució completa amb Docker (opcional)
El projecte pot executar-se completament mitjançant contenidors:
```bash
docker-compose up --build
```
Aquesta opció permet desplegar el sistema sencer (backend, frontend i base de dades) sense instal·lar dependències locals addicionals.

## 🔎 Integració amb SonarCloud

El projecte inclou workflow de GitHub Actions a `.github/workflows/sonarcloud.yml` per executar anàlisi en `push` i `pull_request`.

Passos necessaris al repositori de GitHub:
1. Crear el secret **`SONAR_TOKEN`** a *Settings → Secrets and variables → Actions*.
2. Verificar que `sonar.organization` i `sonar.projectKey` de `sonar-project.properties` coincideixen exactament amb els del teu projecte de SonarCloud.

Nota: SonarCloud pot fer *Automatic Analysis* sense workflow, però per projectes multi-mòdul (backend + frontend) és recomanable l’anàlisi via CI perquè és més completa i reproduïble.
