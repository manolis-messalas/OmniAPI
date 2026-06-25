package com.messalas.omniapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messalas.omniapi.model.dto.AuthorDTO;
import com.messalas.omniapi.model.dto.BookDTO;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
public class IdempotencyRestIT {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyRestIT.class);

    @TestConfiguration
    static class JwtMockMvcConfig {
        @Bean
        MockMvcBuilderCustomizer jwtDefaultRequestCustomizer() {
            return builder -> builder.defaultRequest(get("/").with(jwt()));
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthorDTO buildAuthor(String name) {
        return AuthorDTO.builder()
                .authorName(name)
                .dateOfBirth("1 Jan 1990")
                .countryOfOrigin("TestLand")
                .build();
    }

    @Test
    public void newKey_firstRequest_succeeds() throws Exception {
        String key = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/rest/createAuthor")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildAuthor("Idempotency Author A"))))
                .andExpect(status().isOk());

        logger.info("newKey_firstRequest_succeeds passed");
    }

    @Test
    public void sameKey_secondRequest_returnsConflict() throws Exception {
        String key = UUID.randomUUID().toString();
        String body = objectMapper.writeValueAsString(buildAuthor("Idempotency Author B"));

        mockMvc.perform(post("/api/rest/createAuthor")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/rest/createAuthor")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Duplicate request")));

        logger.info("sameKey_secondRequest_returnsConflict passed");
    }

    @Test
    public void missingKey_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/rest/createAuthor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildAuthor("Idempotency Author C"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Idempotency-Key")));

        logger.info("missingKey_returnsBadRequest passed");
    }

    @Test
    public void differentKeys_samePayload_bothSucceed() throws Exception {
        String body = objectMapper.writeValueAsString(buildAuthor("Idempotency Author D"));

        mockMvc.perform(post("/api/rest/createAuthor")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/rest/createAuthor")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        logger.info("differentKeys_samePayload_bothSucceed passed");
    }

    @Test
    public void businessLogicFails_keyIsReleased_retryWithSameKeySucceeds() throws Exception {
        String key = UUID.randomUUID().toString();

        // Use addBook with a non-existent author to trigger a business-logic failure
        AuthorDTO missingAuthor = AuthorDTO.builder().authorName("Non-existent Author 99").build();
        BookDTO bookWithBadAuthor = BookDTO.builder()
                .bookName("Book With Missing Author")
                .publicationYear("2024")
                .authorDTO(missingAuthor)
                .build();

        // First request: business logic fails → key should be released
        mockMvc.perform(post("/api/rest/addBook")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookWithBadAuthor)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Author not found")));

        // Seed a real author so the retry can succeed
        mockMvc.perform(post("/api/rest/createAuthor")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildAuthor("Retry Test Author"))))
                .andExpect(status().isOk());

        // Retry with the same key but a valid payload → should succeed (key was released)
        AuthorDTO validAuthor = AuthorDTO.builder().authorName("Retry Test Author").build();
        BookDTO goodBook = BookDTO.builder()
                .bookName("Retry Test Book")
                .publicationYear("2024")
                .authorDTO(validAuthor)
                .build();

        mockMvc.perform(post("/api/rest/addBook")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goodBook)))
                .andExpect(status().isOk());

        logger.info("businessLogicFails_keyIsReleased_retryWithSameKeySucceeds passed");
    }
}
