package cz.cvut.user.rest;

import cz.cvut.user.config.SecurityConfig;
import cz.cvut.user.environment.Environment;
import cz.cvut.user.environment.Generator;
import cz.cvut.user.environment.TestConfiguration;
import cz.cvut.user.environment.TestSecurityConfig;
import cz.cvut.user.model.User;
import cz.cvut.user.security.jwt.JwtUtils;
import cz.cvut.user.service.IUserService;
import cz.cvut.user.service.security.UserDetailsService;
import cz.cvut.user.util.UserType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(
        classes = {TestSecurityConfig.class,
                UserControllerSecurityTest.TestConfig.class,
                SecurityConfig.class})
public class UserControllerSecurityTest extends BaseControllerTestRunner {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IUserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        this.objectMapper = Environment.getObjectMapper();
        this.user = Generator.generateUser();
    }

    @AfterEach
    public void tearDown() {
        Environment.clearSecurityContext();
        Mockito.reset(userService);
    }

    @Configuration
    @TestConfiguration
    public static class TestConfig {

        @MockBean
        private IUserService userService;

        @MockBean
        private AuthenticationManager authenticationManager;

        @MockBean
        private JwtUtils jwtUtils;

        @MockBean
        private UserDetailsService userDetailsService;

        @Bean
        public UserController userController() {
            return new UserController(userService, authenticationManager, userDetailsService, jwtUtils);
        }
    }

    private final User testUser = Generator.generateUser();

    @WithAnonymousUser
    @Test
    public void registerSupportsAnonymousAccess() throws Exception {
        mockMvc.perform(
                        post("/register/").content(toJson(testUser)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());
        verify(userService).registerUser(any(User.class));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    public void registerAdminIsAllowedForAdminUser() throws Exception {
        user.setUserType(UserType.ADMIN);
        Environment.setCurrentUser(user);
        final User toRegister = Generator.generateUser();
        toRegister.setUserType(UserType.ADMIN);

        mockMvc.perform(
                        post("/register/").content(toJson(toRegister))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());
        verify(userService).registerUser(any(User.class));
    }


    @Test
    public void loginRegisteredUserReturnsJwt() throws Exception {
        mockMvc.perform(
                post("/register/").content(toJson(testUser)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());
        verify(userService).registerUser(any(User.class));
        MvcResult result = mockMvc.perform(
                    post("/login/").content(toJson(testUser)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("token"));
    }
}
