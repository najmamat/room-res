package cz.cvut.user.dao;

import cz.cvut.user.NssUserServiceApplication;
import cz.cvut.user.environment.Generator;
import cz.cvut.user.environment.TestConfiguration;
import cz.cvut.user.exception.PersistenceException;
import cz.cvut.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ComponentScan(basePackageClasses = NssUserServiceApplication.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = TestConfiguration.class)})
@ContextConfiguration(classes = NssUserServiceApplication.class)
public class BaseDaoTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserDao sut;

    @Test
    public void persistSavesSpecifiedInstance() {
        final User user = Generator.generateUser();
        sut.persist(user);
        assertNotNull(user.getId());

        final User result = em.find(User.class, user.getId());
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getLastName(), result.getLastName());
        assertEquals(user.getUsername(), result.getUsername());
    }

    @Test
    public void findRetrievesInstanceByIdentifier() {
        final User user = Generator.generateUser();
        em.persistAndFlush(user);
        assertNotNull(user.getId());

        final User result = sut.find(user.getId());
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getLastName(), result.getLastName());
        assertEquals(user.getUsername(), result.getUsername());
    }

    @Test
    public void findAllRetrievesAllInstancesOfType() {
        final User firstUser = Generator.generateUser();
        em.persistAndFlush(firstUser);
        final User secondUser = Generator.generateUser();
        em.persistAndFlush(secondUser);

        final List<User> result = sut.findAll();
        //need to add admin
        assertEquals(2 + 1, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getId().equals(firstUser.getId())));
        assertTrue(result.stream().anyMatch(c -> c.getId().equals(secondUser.getId())));
    }

    @Test
    public void updateUpdatesExistingInstance() {
        final User user = Generator.generateUser();
        em.persistAndFlush(user);

        final User updatedUser = new User();
        updatedUser.setId(user.getId());
        final String newUsername = "New Username";
        updatedUser.setUsername(newUsername);
        sut.update(updatedUser);

        final User result = sut.find(user.getId());
        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
    }

    @Test
    public void removeRemovesSpecifiedInstance() {
        final User user = Generator.generateUser();
        em.persistAndFlush(user);
        assertNotNull(em.find(User.class, user.getId()));
        em.detach(user);

        sut.remove(user);
        assertNull(em.find(User.class, user.getId()));
    }

    @Test
    public void removeDoesNothingWhenInstanceDoesNotExist() {
        final User user = Generator.generateUser();
        user.setId(123);
        assertNull(em.find(User.class, user.getId()));

        sut.remove(user);
        assertNull(em.find(User.class, user.getId()));
    }

    @Test
    public void exceptionOnPersistInWrappedInPersistenceException() {
        final User user = Generator.generateUser();
        em.persistAndFlush(user);
        em.remove(user);
        assertThrows(PersistenceException.class, () -> sut.update(user));
    }

    @Test
    public void existsReturnsTrueForExistingIdentifier() {
        final User user = Generator.generateUser();
        em.persistAndFlush(user);
        assertTrue(sut.exists(user.getId()));
        assertFalse(sut.exists(-1));
    }
}
