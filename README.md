# Freenote

Hub des documents des élèves de l'ISFCE. Éclaire ta promo.

## Architecture

```text
be.freenote
├── config          # SecurityConfig, CorsConfig, RedisConfig, MinioConfig, MeilisearchConfig, SwaggerConfig
├── controller      # REST controllers (Auth, User, Document, Report)
├── dto
│   ├── request     # Objets de requête (DocumentUploadRequest, ReportRequest, UpdateProfileRequest)
│   └── response    # Objets de réponse (AuthResponse, UserResponse, DocumentResponse, ReportResponse)
├── entity          # Entités JPA : User, Document, Report
├── enums           # Category, ReportStatus, OAuthProvider
├── exception       # Exceptions custom + GlobalExceptionHandler (@RestControllerAdvice)
├── mapper          # Interfaces MapStruct (UserMapper, DocumentMapper, ReportMapper)
├── repository      # Interfaces Spring Data JPA
├── security        # JwtTokenProvider, JwtAuthFilter, CustomOAuth2UserService
├── service         # Interfaces des services métier
│   └── impl        # Implémentations des services
└── util            # HashUtil, FileUtil, Constants
```

**Stack technique :**

- Java 25 LTS, Spring Boot 4, Gradle Kotlin DSL
- PostgreSQL 17 + pgvector, Redis 7, MinIO, Meilisearch
- OAuth2 (Discord, Google), JWT (JJWT 0.13.x)
- MapStruct 1.6.x, Lombok, Flyway, SpringDoc OpenAPI 3.x
- Virtual Threads activés

## Setup local

### Prérequis

- Java 25+
- Docker & Docker Compose

### Lancer l'infrastructure

```bash
docker compose up -d
```

Cela démarre :

- **PostgreSQL 17** (pgvector) sur le port `5432`
- **Redis 7** sur le port `6379`
- **MinIO** sur le port `9000` (API) et `9001` (console)
- **Meilisearch** sur le port `7700`

### Configurer l'application

```bash
cp .env.example .env
# Éditer .env avec vos valeurs (OAuth2 client IDs, etc.)
```

### Lancer l'application

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

L'API est disponible sur `http://localhost:8080`.
Swagger UI : `http://localhost:8080/swagger-ui.html`.

### Console MinIO

Accédez à `http://localhost:9001` avec `minioadmin` / `minioadmin`.

## Guide de contribution

1. Forker le repository
2. Créer une branche feature : `git checkout -b feature/ma-feature`
3. Committer vos changements : `git commit -m "feat: description"`
4. Pousser la branche : `git push origin feature/ma-feature`
5. Ouvrir une Pull Request

### Conventions

- **Commits** : suivre [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `docs:`, `refactor:`, etc.)
- **Code** : suivre les conventions Java standard, Lombok pour le boilerplate, MapStruct pour le mapping
- **Tests** : écrire des tests unitaires et d'intégration pour toute nouvelle fonctionnalité
- **Branches** : `main` est la branche de production, développer sur des branches feature
