package cz.cvut.room.dao;

import cz.cvut.room.NssRoomServiceApplication;
import cz.cvut.room.environment.Generator;
import cz.cvut.room.environment.TestConfiguration;
import cz.cvut.room.model.Room;
import cz.cvut.room.util.RoomType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ComponentScan(basePackageClasses = NssRoomServiceApplication.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = TestConfiguration.class)})
@ContextConfiguration(classes = NssRoomServiceApplication.class)
public class RoomDaoTest {
    @PersistenceContext
    private EntityManager em;

    @Autowired
    private RoomDao sut;

    @Test
    public void getPrimaryReturnsAllPrimaryRooms() {
        final List<Room> rooms = Generator.generateRooms();
        final long primaryRoomsCount = rooms.stream().filter(r -> r.getRoomType() == RoomType.PRIMARY).count();
        final long predefinedPrimaryRoomsCount = sut.getPrimary().size();
        rooms.forEach(em::persist);

        final List<Room> result = sut.getPrimary();
        assertEquals(primaryRoomsCount, result.size() - predefinedPrimaryRoomsCount);
        assertTrue(result.stream().allMatch(r -> r.getRoomType() == RoomType.PRIMARY));
    }

    @Test
    public void getSecondaryReturnsAllSecondaryRooms() {
        final List<Room> rooms = Generator.generateRooms();
        final long secondaryRoomsCount = rooms.stream().filter(r -> r.getRoomType() == RoomType.SECONDARY).count();
        final long predefinedSecondaryRoomsCount = sut.getSecondary().size();
        rooms.forEach(em::persist);

        final List<Room> result = sut.getSecondary();
        assertEquals(secondaryRoomsCount, result.size() - predefinedSecondaryRoomsCount);
        assertTrue(result.stream().allMatch(r -> r.getRoomType() == RoomType.SECONDARY));
    }
}
