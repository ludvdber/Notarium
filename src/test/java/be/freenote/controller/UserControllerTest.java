package be.freenote.controller;

import be.freenote.dto.response.UserResponse;
import be.freenote.security.JwtAuthFilter;
import be.freenote.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(exclude = OAuth2ClientWebSecurityAutoConfiguration.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    @Test
    void getUserById_shouldReturnFilteredProfile() throws Exception {
        var response = new UserResponse(1L, "alice", 100, null, null, null, null, null,
                List.of("FIRST_UPLOAD"), 5, false, false, true);
        when(userService.getPublicProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.bio").doesNotExist());
    }

    @Test
    void featured_shouldReturnList() throws Exception {
        when(userService.getFeaturedProfiles()).thenReturn(List.of());

        mockMvc.perform(get("/api/users/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
