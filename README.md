# Draftly – AI-Powered Gmail Reply Assistant

An intelligent backend service that fetches emails from Gmail, generates AI-powered reply drafts, and lets users review/approve before sending.

## Architecture

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│  Gmail API   │◄─────►│   Draftly    │◄─────►│  OpenRouter  │
│  (OAuth2)    │       │  Spring Boot │       │  (GPT-3.5)   │
└──────────────┘       └──────┬───────┘       └──────────────┘
                              │
                       ┌──────▼───────┐
                       │    MySQL     │
                       │  (Drafts,   │
                       │   Emails)    │
                       └──────────────┘
```

## Tech Stack

| Layer         | Technology                     |
|---------------|--------------------------------|
| Framework     | Spring Boot 3.x (Java 17)     |
| Database      | MySQL 8.0                      |
| AI Model      | GPT-3.5 Turbo via OpenRouter   |
| Email         | Gmail API with OAuth2          |
| Build         | Maven                          |
| Containerization | Docker & Docker Compose     |

## Features

- **Gmail Integration** – OAuth2-secured fetch of recent emails with metadata
- **AI Draft Generation** – Automatic reply drafts using GPT-3.5
- **Draft Review Workflow** – Approve, reject, or view pending drafts
- **Send on Approval** – Only approved drafts are sent, maintaining thread integrity
- **Async Processing** – Draft generation runs asynchronously for better performance
- **Idempotency** – Duplicate emails/drafts are not re-processed
- **Persistent Storage** – All emails and drafts stored with status tracking

## Prerequisites

- Docker & Docker Compose
- Google Cloud project with Gmail API enabled
- OAuth2 credentials (`credentials.json`) from Google Cloud Console
- OpenRouter API key (or any OpenAI-compatible key)

## Quick Start

### 1. Clone the repository

```bash
git clone https://github.com/tejeswarim/draftly.git
cd draftly
```

### 2. Configure environment

```bash
cp .env.example .env
```

Edit `.env` with your values:
```
MYSQL_ROOT_PASSWORD=your_secure_password
OPENAI_API_KEY=your_openrouter_api_key
```

### 3. Add Gmail credentials

Place your Google OAuth2 `credentials.json` file at:
```
src/main/resources/credentials.json
```

### 4. Run with Docker Compose

```bash
docker-compose up --build
```

The app will be available at `http://localhost:8080`.

### 5. Authenticate with Gmail

On first run, visit the OAuth consent URL printed in the logs to authorize Gmail access. The token is persisted in the `tokens/` directory.

## Running Without Docker

```bash
# Ensure MySQL is running locally on port 3306 with database "Draftly"
export SPRING_DATASOURCE_PASSWORD=your_password
export OPENAI_API_KEY=your_key
./mvnw spring-boot:run
```

## API Documentation

### Emails

| Method | Endpoint       | Description                                      |
|--------|----------------|--------------------------------------------------|
| GET    | `/emails`      | Fetch latest emails and trigger draft generation |
| GET    | `/gmail-test`  | Test Gmail API connectivity                      |

### Drafts

| Method | Endpoint              | Description          |
|--------|-----------------------|----------------------|
| GET    | `/draft`              | List all drafts      |
| POST   | `/draft/{id}/approve` | Approve and send     |
| POST   | `/draft/{id}/reject`  | Reject a draft       |

### Health Check

| Method | Endpoint | Description       |
|--------|----------|-------------------|
| GET    | `/hello` | Basic health test |

### Example Workflow

```bash
# 1. Fetch emails (triggers async draft generation)
curl http://localhost:8080/emails

# 2. View generated drafts
curl http://localhost:8080/draft

# 3. Approve a draft (sends the reply)
curl -X POST http://localhost:8080/draft/1/approve

# 4. Reject a draft
curl -X POST http://localhost:8080/draft/2/reject
```

## Design Decisions

1. **Async Draft Generation** – Uses `@Async` to avoid blocking the email fetch endpoint while AI generates replies.

2. **Idempotent Processing** – Checks `existsById` / `existsByMessageId` before creating duplicates, ensuring safe retries.

3. **Environment-based Configuration** – All secrets are injected via environment variables, never hardcoded in source.

4. **Docker Multi-stage Build** – Separates build (Maven + JDK) from runtime (JRE only), producing a smaller final image.

5. **MySQL for Persistence** – Chosen over H2 for production-readiness; schema auto-managed by Hibernate `ddl-auto=update`.

6. **OpenRouter as AI Gateway** – Provides access to multiple models behind a single API, allowing easy model switching.

7. **Status-based Workflow** – Drafts follow `PENDING → APPROVED → SENT` or `PENDING → REJECTED`, giving users full control.

## Project Structure

```
src/main/java/com/draftly/draftly/
├── config/
│   ├── AppConfig.java          # RestTemplate bean
│   ├── GmailConfig.java        # Gmail OAuth2 setup
│   └── OpenAIConfig.java       # API key configuration
├── controller/
│   ├── DraftController.java    # Draft review endpoints
│   ├── GmailController.java    # Email fetch endpoints
│   └── TestController.java     # Health check
├── entity/
│   ├── Draft.java              # Draft JPA entity
│   ├── Email.java              # Email JPA entity
│   └── Status.java             # Draft status enum
├── repository/
│   ├── DraftRepository.java    # Draft data access
│   └── EmailRepository.java    # Email data access
├── service/
│   ├── AIService.java          # OpenRouter AI integration
│   ├── DraftService.java       # Draft business logic
│   ├── EmailWorkflowService.java # Orchestration layer
│   └── GmailService.java       # Gmail API operations
└── DraftlyApplication.java     # Entry point
```

## Security Considerations

- OAuth2 tokens stored locally in `tokens/` (excluded from git)
- API keys injected via environment variables
- `credentials.json` excluded from version control
- Only approved drafts can be sent (prevents accidental sends)

## License

MIT
