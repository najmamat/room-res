package cz.cvut.user.service;

import cz.cvut.user.dao.UserDao;
import cz.cvut.user.exception.UserAlreadyExistsException;
import cz.cvut.user.model.User;
import cz.cvut.user.util.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
public class UserService implements IUserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private User loggedInUser;
    private final PasswordEncoder encoder;

    @Autowired
    public UserService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.encoder = passwordEncoder;
    }

    /**
     * Registers user. If user is registered, nothing happens.
     * @param u User to be registered
     * @return Registered user, if registering was successful, null otherwise.
     */
    @Transactional
    @Override
    public User registerUser(User u) throws UserAlreadyExistsException {
        if (findByUsername(u.getUsername()) != null) {
            LOG.warn("User with username " + u.getUsername() + " already exists.");
            throw new UserAlreadyExistsException("This username is taken");
        }

        User newUser = new User(u.getFirstName(), u.getLastName(), u.getUsername(), u.getPassword());
        newUser.setUserType(UserType.USER);
        newUser.encodePassword(encoder);
        userDao.persist(newUser);
        LOG.info("User with username " + newUser.getUsername() + " registered.");
        return newUser;
    }

    /**
     * Tries to login user.
     * @param username
     * @param password
     * @return True, if login was successful / False otherwise
     */
    @Transactional
    @Override
    public boolean login(String username, String password) {
        User potentialUser = userDao.findByUsername(username);
        if (potentialUser != null && encoder.matches(password, potentialUser.getPassword())) {
            loggedInUser = potentialUser;
            LOG.info("User with username " + username + " logged in.");
            return true;
        }
        LOG.warn("Invalid initials to login with username: " + username);
        return false;
    }


    /**
     * Logouts user.
     */
    @Override
    public void logout() {
        loggedInUser = null;
    }

    @Override
    public boolean isLoggedInUserAdmin() {
        return loggedInUser != null && loggedInUser.isAdmin();
    }

    @Override
    public boolean exists(String username) {
        return userDao.findByUsername(username) != null;
    }


    private boolean userIsLoggedIn(String username) {
        return this.loggedInUser != null && username.equals(this.loggedInUser.getUsername());
    }

    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public User find(Integer userId) {
        return userDao.find(userId);
    }
}
