package cz.cvut.user.environment;

import cz.cvut.user.security.jwt.AuthEntryPointJwt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import static org.mockito.Mockito.mock;

@cz.cvut.user.environment.TestConfiguration
@Configuration
public class TestSecurityConfig {

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return mock(AuthenticationFailureHandler.class);
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return mock(AuthenticationSuccessHandler.class);
    }


    @Bean
    public AuthEntryPointJwt authEntryPointJwt() {
        return mock(AuthEntryPointJwt.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return mock(LogoutSuccessHandler.class);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return mock(AuthenticationProvider.class);
    }
}
