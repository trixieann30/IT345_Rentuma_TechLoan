package edu.cit.rentuma.techloan.features.auth;

import edu.cit.rentuma.techloan.AbstractIntegrationTest;
import edu.cit.rentuma.techloan.features.auth.dto.LoginRequest;
import edu.cit.rentuma.techloan.features.auth.dto.RegisterRequest;
import edu.cit.rentuma.techloan.features.auth.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("TC-AUTH: Authentication Controller Tests")
class AuthControllerTest extends AbstractIntegrationTest {

    // TC-AUTH-001
    @Test
    @DisplayName("TC-AUTH-001: Register with valid student data returns 201 and token")
    void register_validStudent_returns201() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "Alice Smith", "alice@cit.edu", "2021-00001",
                "Password1", "Password1", User.Role.STUDENT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.success").value(true));
    }

    // TC-AUTH-002
    @Test
    @DisplayName("TC-AUTH-002: Register with duplicate email returns 4xx error")
    void register_duplicateEmail_returnsError() throws Exception {
        createStudent("bob@cit.edu", "STU-002");

        RegisterRequest req = new RegisterRequest(
                "Bob Duplicate", "bob@cit.edu", "STU-999",
                "Password1", "Password1", User.Role.STUDENT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    // TC-AUTH-003
    @Test
    @DisplayName("TC-AUTH-003: Register with missing required fields returns 400")
    void register_missingFields_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "", "", "",
                "Password1", "Password1", User.Role.STUDENT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // TC-AUTH-004
    @Test
    @DisplayName("TC-AUTH-004: Login with valid credentials returns 200 and token")
    void login_validCredentials_returns200() throws Exception {
        createStudent("carol@cit.edu", "STU-003");

        LoginRequest req = new LoginRequest("carol@cit.edu", "Password1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.success").value(true));
    }

    // TC-AUTH-005
    @Test
    @DisplayName("TC-AUTH-005: Login with wrong password returns 401")
    void login_wrongPassword_returns401() throws Exception {
        createStudent("dave@cit.edu", "STU-004");

        LoginRequest req = new LoginRequest("dave@cit.edu", "WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // TC-AUTH-006
    @Test
    @DisplayName("TC-AUTH-006: Login with non-existent email returns 401")
    void login_nonExistentEmail_returns401() throws Exception {
        LoginRequest req = new LoginRequest("ghost@cit.edu", "Password1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // TC-AUTH-007
    @Test
    @DisplayName("TC-AUTH-007: GET /auth/me with valid JWT returns 200 and user info")
    void getMe_validToken_returns200() throws Exception {
        User student = createStudent("eve@cit.edu", "STU-005");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", studentToken(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("eve@cit.edu"));
    }

    // TC-AUTH-008
    @Test
    @DisplayName("TC-AUTH-008: GET /auth/me without JWT returns 401/403")
    void getMe_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().is4xxClientError());
    }
}
