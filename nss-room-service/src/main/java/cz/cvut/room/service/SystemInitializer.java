package cz.cvut.room.service;

import cz.cvut.room.dao.RoomDao;
import cz.cvut.room.model.Room;
import cz.cvut.room.model.TimeSlot;
import cz.cvut.room.util.EquipmentType;
import cz.cvut.room.util.RoomType;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cz.cvut.room.util.Constants.*;

@Component
public class SystemInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInitializer.class);

    /**
     * Default admin username
     */
    private static final String ADMIN_USERNAME = "admin";

    private final RoomDao roomDao;

    private final PlatformTransactionManager txManager;

    private final PasswordEncoder encoder;

    @Autowired
    public SystemInitializer(RoomDao roomDao,
                             PlatformTransactionManager txManager,
                             PasswordEncoder encoder) {
        this.txManager = txManager;
        this.encoder = encoder;
        this.roomDao = roomDao;
    }

    @PostConstruct
    private void initSystem() {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.execute((status) -> {
            deleteTestRooms();
            roomDao.persist(generateRooms());
            return null;
        });/*
        txTemplate.execute((status) -> {
            deleteTestPostmanUsers();
            return null;
        });
        txTemplate.execute((status) -> {
            deleteTestPostmanRooms();
            return null;
        });
        txTemplate.execute((status) -> {
            deleteTestPostmanEvents();
            return null;
        });
       /* txTemplate.execute((status) -> {
            generateTimeSlots();
            return null;
        });*/
    }

    public List<Room> generateRooms() {
        final List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Room newPrimaryRoom = new Room("code-p" + i, RoomType.PRIMARY, i + 3);
            newPrimaryRoom.setTimeSlots(generateTimeSlots(newPrimaryRoom));
            Set<EquipmentType> roomEquipmentTypes = new HashSet<>();
            roomEquipmentTypes.add(EquipmentType.values()[i% (EquipmentType.values().length)]);
            newPrimaryRoom.setEquipment(roomEquipmentTypes);
            rooms.add(newPrimaryRoom);
        }
        for (int i = 0; i < 5; i++) {
            Room newSecondaryRoom = new Room("code-s" + i, RoomType.SECONDARY, i + 3);
            newSecondaryRoom.setTimeSlots(new HashSet<>());
            Set<EquipmentType> roomEquipmentTypes = new HashSet<>();
            roomEquipmentTypes.add(EquipmentType.values()[i% (EquipmentType.values().length)]);
            newSecondaryRoom.setEquipment(roomEquipmentTypes);
            rooms.add(newSecondaryRoom);
        }
        LOG.info("Importing default test rooms.");
        return rooms;
    }

    public Set<TimeSlot> generateTimeSlots(Room r) {
        Set<TimeSlot> timeSlots = new HashSet<>();
        for (int day = 1; day <= TEST_MONTH.length(false); day++) {
            for (int i = 0; i < ((DAY_END.getHour() - DAY_START.getHour()) * (60 / TIME_SLOT_LENGTH)); i++) {
                timeSlots.add(new TimeSlot(r, LocalDateTime.of(LocalDate.of(2021, TEST_MONTH.getValue(), day), DAY_START.plusMinutes(i * 30L))));
            }
        }
        return timeSlots;
    }

    public List<TimeSlot> generateTimeSlotsWithList(Room r) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        for (int day = 1; day <= TEST_MONTH.length(false); day++) {
            for (int i = 0; i < ((DAY_END.getHour() - DAY_START.getHour()) * (60 / TIME_SLOT_LENGTH)); i++) {
                timeSlots.add(new TimeSlot(r, LocalDateTime.of(LocalDate.of(2021, TEST_MONTH.getValue(), day), DAY_START.plusMinutes(i * 30L))));
            }
        }
        return timeSlots;
    }


    private void deleteTestRooms() {
        List<Room> allRooms = roomDao.findAll();

        for (Room r : allRooms) {
           roomDao.remove(r);
        }
        LOG.info("Cleared db of all rooms.");
    }
}
