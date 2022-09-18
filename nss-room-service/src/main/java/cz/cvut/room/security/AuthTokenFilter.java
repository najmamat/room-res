package cz.cvut.room.security;

import cz.cvut.room.util.Constants;
import cz.cvut.room.util.wrappers.UserWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthTokenFilter extends OncePerRequestFilter  {
    private static final Logger LOG = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
        } else {
            WebClient userServiceClient = WebClient.builder()
                .baseUrl("http://localhost:9999/user/authenticate")
                .defaultHeader(HttpHeaders.AUTHORIZATION, request.getHeader(HttpHeaders.AUTHORIZATION))
                .build();

            WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = userServiceClient.method(HttpMethod.GET);
            WebClient.RequestBodySpec requestBodySpec = uriSpec.uri("");

            WebClient.RequestHeadersSpec<?> headersSpec = requestBodySpec.bodyValue("");

            try {
                Mono<UserWrapper> userServiceResponse = headersSpec.retrieve().bodyToMono(UserWrapper.class);
                UserWrapper userWrapper = userServiceResponse.block();
                request.setAttribute(Constants.USER_WRAPPER_ATTR, userWrapper);

                filterChain.doFilter(request, response);

            } catch (WebClientException e) {
                //Happens when userServiceResponse sends 4xx or 5xx status code
                LOG.warn(e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
            }
        }
    }
}
