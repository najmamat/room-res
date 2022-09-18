package cz.cvut.user.service;

import cz.cvut.user.dao.UserDao;
import cz.cvut.user.model.User;
import cz.cvut.user.util.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static cz.cvut.user.util.Constants.RANDOM;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
public class UserServiceTest {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UserDao daoMock;

    private UserService sut;

    @BeforeEach
    public void setUp() {
        this.sut = new UserService(daoMock, passwordEncoder);
    }

    @Test
    public void registerUserEncodesUserPassword() {
        User generatedUser = getTestUser();
        String rawPassword = generatedUser.getPassword();
        sut.registerUser(generatedUser);

        final ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(daoMock).persist(captor.capture());
        assertTrue(passwordEncoder.matches(rawPassword, captor.getValue().getPassword()));
    }

    @Test
    public void registeredUserHasUserRoleAsDefault() {
        User generatedUser = getTestUser();

        sut.registerUser(generatedUser);

        final ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(daoMock).persist(captor.capture());
        assertEquals(generatedUser.getUserType(), UserType.USER);
    }

    @Test
    public void loginRegisteredUserWithValidDataIsSuccessful() {
        User generatedUser = getTestUser();
        User registeredUser = sut.registerUser(generatedUser);

        assertNotNull(registeredUser);

        Mockito.when(daoMock.findByUsername(generatedUser.getUsername())).thenReturn(registeredUser);
        sut.login(generatedUser.getUsername(), generatedUser.getPassword());

        assertNotNull(sut.getLoggedInUser());
        assertEquals(generatedUser.getUsername(),sut.getLoggedInUser().getUsername());
        assertEquals(registeredUser.getUsername(), sut.getLoggedInUser().getUsername());
    }

    private User getTestUser() {
        return new User("TestFirstName", "TestLastName", "TestUsername" + RANDOM.nextInt(), "Heslo" + RANDOM.nextInt());
    }
}
