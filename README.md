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

---

## 🧰 Prerequisits i execució local

Per executar el projecte en un entorn local és necessari disposar de les següents eines instal·lades:

### Requisits generals
- Git
- Docker (Docker Desktop o Docker Engine)

### Backend
- Java Development Kit (JDK) 17 (LTS)
- Maven (o Maven Wrapper inclòs al projecte)

### Frontend
- Node.js (versió LTS, 20.x recomanada)
- npm (inclòs amb Node.js)

---

## ▶ Execució del projecte en local

### 1. Clonar el repositori

```bash
git clone https://github.com/AlvaroPerdigonG/tfg-ticketing-devops.git
cd tfg-ticketing-devops
```

### 2. Iniciar la base de dades amb Docker
El projecte utilitza una base de dades PostgreSQL dins `ticketing-backend/docker-compose.yml`.

```bash
cd ticketing-backend
docker compose up -d
```

### 3. Executar el backend
Des del directori `ticketing-backend`:

```bash
./mvnw spring-boot:run
```

El backend quedarà accessible a: http://localhost:8080

### 4. Executar el frontend
En una altra terminal, des de la ruta arrel del repositori:

```bash
cd ticketing-frontend
npm install
npm run dev
```

El frontend quedarà accessible a: http://localhost:5173

### ⚙ Configuració
Les variables de configuració s’estableixen mitjançant fitxers d’entorn i perfils d’execució:

- Backend: `ticketing-backend/src/main/resources/application.yml` i perfils (`application-local.yml`, `application-cloud.yml`).
- Frontend: variables d’entorn amb Vite (p. ex. `.env.local`).

No s’inclouen credencials ni secrets productius al repositori.

### 🧪 Execució de proves
Backend:
```bash
cd ticketing-backend
./mvnw test
```

Frontend:
```bash
cd ticketing-frontend
npm run test:run
```

E2E (Playwright):
```bash
cd ticketing-frontend
npm run test:e2e
```

---

## 📚 Documentació funcional i tècnica

- Endpoints i contractes API actuals: `docs/tickets-endpoints-proposal.md`
- Estratègia de testing: `docs/testing/testing-strategy.md`
- Matriu de traçabilitat: `docs/testing/traceability-matrix.md`
- Estratègia CI/CD: `docs/devops/cicd-strategy.md`

---

## ⚖️ Llicència

Aquest projecte es distribueix sota llicència **MIT**.  
Consulta el fitxer [LICENSE](LICENSE) per a més informació.

---

## 👤 Autor

Treball de Final de Grau realitzat per:

**Álvaro Perdigón Gordillo**  
Grau en Enginyeria Informàtica de Gestió i Sistemes d'Informació - Tecnocampus
