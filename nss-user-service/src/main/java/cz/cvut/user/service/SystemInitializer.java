package cz.cvut.user.service;

import cz.cvut.user.dao.UserDao;
import cz.cvut.user.model.User;
import cz.cvut.user.util.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cz.cvut.user.util.Constants.*;

@Component
public class SystemInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInitializer.class);

    /**
     * Default admin username
     */
    private static final String ADMIN_USERNAME = "admin";

    private final UserDao userDao;

    private final PlatformTransactionManager txManager;

    private final PasswordEncoder encoder;


    @Autowired
    public SystemInitializer(UserDao userDao,
                             PlatformTransactionManager txManager,
                             PasswordEncoder encoder) {
        this.userDao = userDao;
        this.txManager = txManager;
        this.encoder = encoder;
    }

    @PostConstruct
    private void initSystem() {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.execute((status) -> {
            generateAdmin();
            return null;
        });
        txTemplate.execute((status) -> {
            deleteTestPostmanUsers();
            return null;
        });
    }


    /**
     * Generates an admin account if it does not already exist.
     */
    private void generateAdmin() {
        if (userDao.findByUsername(ADMIN_USERNAME) != null) {
            return;
        }
        final User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setPassword("admin1");
        admin.encodePassword(encoder);
        admin.setUserType(UserType.ADMIN);
        LOG.info("Generated admin user with credentials " + admin.getUsername() + "/" + admin.getPassword());
        userDao.persist(admin);
    }

    private void deleteTestPostmanUsers() {
        List<User> allUsers = userDao.findAll();

        for (User u: allUsers) {
            if (u.getUsername().contains("postman_user"))
                userDao.remove(u);
        }
        LOG.info("Cleared db of all virtual postman test users.");
    }
}
