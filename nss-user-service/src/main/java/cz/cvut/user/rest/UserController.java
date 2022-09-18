package cz.cvut.user.rest;

import cz.cvut.user.exception.NoPermissionException;
import cz.cvut.user.exception.UserAlreadyExistsException;
import cz.cvut.user.model.Jwt;
import cz.cvut.user.rest.util.RestUtils;
import cz.cvut.user.security.jwt.JwtUtils;
import cz.cvut.user.security.model.AuthenticationToken;
import cz.cvut.user.security.model.UserDetails;
import cz.cvut.user.service.IUserService;
import cz.cvut.user.model.User;
import cz.cvut.user.service.security.UserDetailsService;
import cz.cvut.user.util.UserWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.standard.Media;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

@RestController
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final IUserService userService;
    private final JwtUtils jwtUtils;

    @Autowired
    public UserController(IUserService userService, AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Registers a new user.
     *
     * @param user User data
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            userService.registerUser(user);
        } catch (UserAlreadyExistsException error) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error.getMessage());
        }

        LOG.debug("User {} successfully registered.", user);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtils.generateJwtToken(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Jwt(token));
    }

    /**
     * User login using Spring Security.
     *
     * @param user User data
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Jwt login(@RequestBody User user) throws Exception {
        authenticate(user);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtils.generateJwtToken(userDetails);
        return new Jwt(token);
    }

    @GetMapping(value = "/authenticate", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserWrapper authenticate(HttpServletRequest request) {
        if (request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
            throw new NoPermissionException("Authorization header is missing completely.");
        }

        Jwt jwt = new Jwt(request.getHeader(HttpHeaders.AUTHORIZATION).substring(7));
        if (jwtUtils.validateJwtToken(jwt.getToken())) {
            return new UserWrapper(jwtUtils.getUserNameFromJwtToken(jwt.getToken()), jwtUtils.getIsAdminClaim(jwt.getToken()));
        }

        throw new NoPermissionException("Invalid token!");
    }

    /**
     * Get info about logged in user.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER', 'ROLE_GUEST')")
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public User getCurrent(Principal principal) {
        AuthenticationToken auth = (AuthenticationToken) principal;
        return auth.getPrincipal().getUser();
    }

    /**
     * Logouts user.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null)
            new SecurityContextLogoutHandler().logout(request, response, auth);
    }

    private void authenticate(User u) throws Exception {
        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(u.getUsername(), u.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    @ExceptionHandler(NoPermissionException.class)
    public ResponseEntity<String> handleNoPermissionException(NoPermissionException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(exception.getMessage());
    }

}
