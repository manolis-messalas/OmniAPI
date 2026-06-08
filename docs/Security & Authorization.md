# Application Security Overview
This application follows a security-first design, using Spring Security as the core framework for authentication, authorization, and request protection. All endpoints are secured following modern best practices, with support for OAuth 2.0–based authentication flows and optional Multi-Factor Authentication (MFA) for enhanced identity assurance. The system enforces strong access control, secure session handling, CSRF protection, and hardened security headers to reduce exposure to common vulnerabilities. As the project evolves, additional safeguards—such as rate limiting, improved monitoring, and extended identity providers—may be introduced to further strengthen the application’s security posture.

The OWASP Top 10 is a regularly-updated report outlining security concerns for web application security, focusing on the 10 most critical risks. OmniAPI's security is based on securing the application from these threats.
The Open Web Application Security Project (OWASP) is a non-profit organization founded in 2001, with the goal of helping website owners and security experts protect web applications from cyber attacks. OWASP has 32,000 volunteers around the world who perform security assessments and research.

### A01 Broken Access Control
### A02 Cryptographic Failures
### A03 Injection
### A04 Insecure Design
### A05 Security Misconfiguration
### A06 Vulnerable and Outdated Components
### A07 Identification and Authentication Failures
### A08 Software and Data Integrity Failures
### A09 Security Logging and Monitoring Failures
### A10 Server Side Request Forgery (SSRF)

````mermaid
---
title:  The Dispatcher Servlet handles incoming requests. Spring Security adds a layer of filters prior to it
---
flowchart LR
    A[Spring Security] --> B[Dispatcher Servlet];
    B --> C[Rest Controller];
````
Spring Security filters will be utilized to target OWASP Top 10 issues. In the diagram below is displayed which Spring Security filters correspond to which OWASP Top 10 issues:
````mermaid
---
title:  OWASP Issue to Filter
---
flowchart LR
    A[A01 Broken Access Control] --> B[AuthorizationFilter
                                    UsernamePasswordAuthenticationFilter
                                    CsrfFilter
                                    SecurityContextHolderFilter
                                    SecurityContextHolderAwareRequestFilter
                                    ExceptionTranslationFilter
                                    AnonymousAuthenticationFilter];
        
    C[A03 Injection] -->            D[HeaderWriterFilter];
    E[A04 Insecure Design] -->      F[CsrfFilter
                                    HeaderWriterFilter];
    G[A05 Security Misconfiguration] -->    H[HeaderWriterFilter
                                            ExceptionTranslationFilter
                                            DefaultResourcesFilter
                                            DefaultLoginPageGeneratingFilter
                                            DefaultLogoutPageGeneratingFilter];
    J[A07 Identification and Authentication Failures] --> K[UsernamePasswordAuthenticationFilter
                                                            BasicAuthenticationFilter
                                                            RequestCacheAwareFilter];
````
For the other important security issues that are not addressed via Spring Security different approaches are in place:  
 #### 'A02 Cryptographic Failure'. HTTPS everywhere, password hashing, no hardcoded secrets.
 #### 'A06 Vulnerable and Outdated Components' issue we will keep our dependencies up to date and utilize dependency scanning.
 #### 'A08 – Software and Data Integrity Failures'. Artifacts are built exclusively from source code within the GitHub Actions CI environment and dependencies are pulled from trusted repos.
 #### 'A09 – Security Logging and Monitoring Failures'. Log the right things without leaking secrets. Ships logs to a central system like ELK & add alerts for many 401/403.
 #### 'A10 – Server-Side Request Forgery (SSRF)'. Both REST & SOAP controllers do not make outbound HTTP calls and the attacker cannot control URL, host or path that the server calls.