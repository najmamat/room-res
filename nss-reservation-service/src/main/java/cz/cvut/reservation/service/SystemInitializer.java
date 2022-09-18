package cz.cvut.reservation.service;

import cz.cvut.reservation.dao.EventDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class SystemInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInitializer.class);

    /**
     * Default admin username
     */
    private static final String ADMIN_USERNAME = "admin";

    private final PlatformTransactionManager txManager;

    private final PasswordEncoder encoder;

    private final EventDao eventDao;

    @Autowired
    public SystemInitializer(
                             PlatformTransactionManager txManager,
                             PasswordEncoder encoder,
                             EventDao eventDao) {
        this.txManager = txManager;
        this.encoder = encoder;
        this.eventDao = eventDao;
    }
/*
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
        txTemplate.execute((status) -> {
            deleteTestPostmanRooms();
            return null;
        });
        txTemplate.execute((status) -> {
            deleteTestPostmanEvents();
            return null;
        });
        txTemplate.execute((status) -> {
            generateTimeSlots();
            return null;
        });
    }

    private void deleteTestPostmanRooms() {
        List<Room> allRooms = roomDao.findAll();

        for (Room r : allRooms) {
            if (r.getCode().contains("postman_room"))
                roomDao.remove(r);
        }
        LOG.info("Cleared db of all virtual postman test rooms.");
    }*/


    /**
     * Generates an admin account if it does not already exist.
     */
    /*private void generateAdmin() {
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

    private void deleteTestPostmanEvents() {
        List<Event> allEvents = eventDao.findAll();

        for (Event e: allEvents) {
            if (e.getName().contains("postman_event"))
                eventDao.remove(e);
        }
        LOG.info("Cleared db of all virtual postman test events.");
    }

    private void generateTimeSlots() {
        List<Room> rooms = roomDao.getPrimary();
        for (Room r: rooms) {
            r.setTimeSlots(generateTimeSlotsWithList(r));
            roomDao.update(r);
        }
        LOG.info("Generated timeslots for primary rooms for this year");
    }

    private Set<TimeSlot> generateTimeSlotsWithList(Room r) {
        Set<TimeSlot> timeSlots = new HashSet<>();

        for (int m = 1; m < 6; m++) {
            LOG.info("month : " + m);
            for (int day = 1; day <= Month.of(m).length(false); day++) {
                LOG.info("day : " + day);
                for (int i = 0; i < ((DAY_END.getHour() - DAY_START.getHour()) * (60 / TIME_SLOT_LENGTH)); i++) {
                    timeSlots.add(new TimeSlot(r, LocalDateTime.of(LocalDate.of(2021, Month.of(m).getValue(), day), DAY_START.plusMinutes(i * 30L))));
                }
            }
        }
        return timeSlots;
    }*/

}
