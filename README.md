# AI Resume Analyzer

An AI-assisted resume analysis web application. Users can upload a text-based PDF resume, compare it with a job description, receive a match score and improvement feedback, and revisit or delete previous analyses.

## Features

- Account registration and secure sign-in
- Password hashing with BCrypt and session-based Spring Security
- PDF resume upload and text extraction with Apache PDFBox
- Resume-to-job-description scoring using the OpenAI API
- Feedback and relevant skill-term extraction
- Keyword-based fallback estimate when no AI API key is configured
- Personal dashboard with total analyses, average score and latest score
- Analysis history with timestamps, feedback and job description
- Delete analysis (only the owner can delete their own record)
- Optional administrator dashboard with read-only platform overview
- MongoDB persistence

## Technology

- Java 17
- Spring Boot 3.4.5, Spring MVC and Thymeleaf
- Spring Security and BCrypt
- Spring Data MongoDB
- Apache PDFBox 3
- OpenAI Chat Completions API
- Maven
- HTML, CSS and a small amount of browser-native JavaScript (confirmation only)

## Requirements

- JDK 17+
- Maven 3.9+
- MongoDB 6/7 (local or hosted)
- OpenAI API key for AI-generated scoring (optional; local keyword fallback is used without it)

## Configuration

Set environment variables before starting the application:

| Variable | Required | Description |
|---|---|---|
| `MONGODB_URI` | Yes | MongoDB connection string |
| `OPENAI_API_KEY` | No | OpenAI API key; without it, a local keyword estimate is returned |
| `OPENAI_MODEL` | No | Defaults to `gpt-4.1-mini` |
| `ADMIN_EMAIL` | No | Email for optional first-run admin creation |
| `ADMIN_PASSWORD` | No | Password for optional first-run admin creation |

Never commit real API keys, database credentials or administrator passwords. `.env.example` contains placeholders only.

## Run locally

1. Start MongoDB.
2. Configure `MONGODB_URI` and optionally `OPENAI_API_KEY`.
3. From the project root run:

```bash
mvn spring-boot:run
```

4. Open [http://localhost:8080](http://localhost:8080).
5. Create an account, sign in, upload a PDF and paste a job description.

Build a jar:

```bash
mvn clean package
java -jar target/ai-resume-analyzer-1.0.0.jar
```

## Run with Docker Compose

Set `OPENAI_API_KEY` in your shell if you want AI analysis, then run:

```bash
docker compose up --build
```

The app is available at `http://localhost:8080`. Docker Compose starts MongoDB with a persistent volume.

## Admin account

To create an administrator automatically on first startup, set both `ADMIN_EMAIL` and `ADMIN_PASSWORD` before the application starts. The password is BCrypt-hashed. Do not use a weak or shared password. The admin dashboard is available at `/admin`.

## Important limitations

- PDFBox extracts selectable text. Scanned/image-only PDFs need OCR and are rejected with a clear message.
- The fallback score is a simple keyword-overlap estimate, not an AI assessment. Configure an OpenAI key for AI-generated scoring and feedback.
- The original source repository had incomplete backend controller sections and an admin page commented out. This Java implementation supplies working application flows for registration, analysis, history, deletion and a basic read-only admin overview rather than pretending those incomplete source sections were fully implemented.
- This is a portfolio-ready starting implementation, not a security or production certification. Before public deployment, configure HTTPS, secure secrets, MongoDB access controls/backups, rate limits, monitoring, privacy/retention policy and production error handling. Run the test suite and verify the deployment environment.

## Project structure

```text
src/main/java/com/apurv/resumeanalyzer/
  config/       Security and initial admin setup
  controller/   MVC controllers
  model/        MongoDB documents
  repository/   MongoDB repositories
  service/      Authentication, PDF extraction and AI analysis
src/main/resources/
  templates/    Thymeleaf pages
  static/css/   Application styling
```

## Author

Apurv Chaturvedi
