# Compliance-Oriented Engineering Checklist

> Engineering controls implemented or planned for OmniAPI. This document
> describes compliance-oriented features and does not claim legal
> compliance.

## Section A --- Already Implemented

### Authentication & Authorization

-   OAuth2 Authorization Server
-   PKCE support
-   JWT-based authentication
-   Spring Security
-   HTTPS support

### Application Quality

-   Backend unit tests
-   Integration tests
-   Frontend build validation
-   JaCoCo code coverage
-   Optimistic locking

### CI/CD

-   GitHub Actions pipeline
-   Docker image build
-   GHCR image publishing
-   Backend container smoke tests
-   Frontend container smoke tests
-   PostgreSQL integration testing
-   Spring Actuator health check
-   Database readiness verification
-   Failure log collection
-   CI validation gate

### DevSecOps

-   Trivy filesystem vulnerability scanning
-   Trivy container image scanning
-   Gitleaks secret scanning
-   SARIF upload to GitHub Code Scanning

------------------------------------------------------------------------

## Section B --- To Be Implemented

### GDPR

-   [ ] User data export endpoint
-   [ ] User deletion request workflow
-   [ ] Soft delete → anonymization → scheduled purge
-   [ ] Configurable retention policies
-   [ ] Personal data inventory
-   [ ] Consent / Privacy notice UI
-   [ ] Audit log for personal-data access
-   [ ] Encryption for sensitive database fields
-   [ ] Admin page for data-subject requests

### NIS2

-   [ ] RBAC permission matrix
-   [ ] MFA-ready authentication
-   [ ] Failed login rate limiting
-   [ ] Account lockout policy
-   [ ] Security event logging
-   [ ] Security event dashboard
-   [ ] Incident register
-   [ ] Security HTTP headers
-   [ ] Dependency update policy

### DORA

-   [ ] Metrics dashboard
-   [ ] Structured application logs
-   [ ] Backup script
-   [ ] Restore test
-   [ ] Disaster recovery runbook
-   [ ] Incident response runbook
-   [ ] Deployment changelog
-   [ ] Third-party dependency/vendor register
-   [ ] Operational risk register
-   [ ] RTO documentation
-   [ ] RPO documentation
-   [ ] Uptime and error-rate monitoring

### EU AI Act (only if AI features are added)

-   [ ] AI-use disclosure
-   [ ] Model/provider registry
-   [ ] Prompt & response audit logging
-   [ ] Source citations
-   [ ] Human review workflow
-   [ ] AI output disclaimer
-   [ ] User feedback on AI responses
-   [ ] AI risk classification page
-   [ ] AI usage monitoring

### CI/CD Enhancements

-   [ ] SonarQube quality gate
-   [ ] SBOM generation
-   [ ] License compliance scan
-   [ ] Database migration validation
-   [ ] Backup/restore smoke test
-   [ ] Deployment environment approvals

### Admin UI

-   [ ] Audit Trail
-   [ ] Roles & Permissions
-   [ ] Retention Policies
-   [ ] Data Subject Requests
-   [ ] Security Events
-   [ ] Incident Management
-   [ ] System Health
-   [ ] Compliance Dashboard
-   [ ] AI Governance (if applicable)
