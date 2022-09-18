package cz.cvut.room.service;

import cz.cvut.room.NssRoomServiceApplication;
import cz.cvut.room.dto.AbstractDto;
import cz.cvut.room.dto.RoomDto;
import cz.cvut.room.environment.Generator;
import cz.cvut.room.exception.RoomNotFoundException;
import cz.cvut.room.model.AbstractEntity;
import cz.cvut.room.model.Room;
import cz.cvut.room.model.TimeSlot;
import cz.cvut.room.util.EquipmentType;
import cz.cvut.room.util.RoomRequirements;
import cz.cvut.room.util.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.cvut.room.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = NssRoomServiceApplication.class)
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class RoomServiceTest {
    private final LocalDateTime testTime = LocalDateTime.of(LocalDate.of(2021, TEST_MONTH, RANDOM.nextInt(TEST_MONTH.maxLength() - 1)), LocalTime.of(10,30));

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private RoomService sut;

    private boolean dataGenerated = false;

    /**
     * Seeds the test data to H2 database.
     */
    @BeforeEach
    public void setUp() {
        if (dataGenerated)
            return;
        Generator.generateRooms().forEach(em::persist);
        dataGenerated = true;
    }

    @ParameterizedTest(name = "Get primary room on date: {0}.{1}.{2} {3}:{4}, with duration: {5}")
    @CsvSource({"2021,11,10,8,30,6", "2021,11,24,8,0,4", "2021,11,29,6,30,10"})
    public void getFreeRoomReturnsFirstFreePrimaryRoomDto(int year, int month, int day, int hours, int minutes, int duration) {
        LocalDateTime start = LocalDateTime.of(year, month, day, hours, minutes);
        Room r = sut.getFreeRoomOnTimeSlot(start, duration);

        assertNotNull(r);

        List<RoomDto> roomsSortedById = sut.getRooms().stream().filter(room -> room.getRoomType() == RoomType.PRIMARY).sorted(Comparator.comparingInt(AbstractDto::getId)).collect(Collectors.toList());
        assertEquals(roomsSortedById.get(0).getId(), r.getId());
        assertEquals(RoomType.PRIMARY, r.getRoomType());
        List<TimeSlot> availableTimeSlots = r.getTimeSlots()
                .stream()
                .filter(ts -> ts.getStart().isEqual(start) || (ts.getStart().isAfter(start) && ts.getStart().isBefore(start.plusMinutes((long) duration *TIME_SLOT_LENGTH))))
                .collect(Collectors.toList());

        assertEquals(duration, availableTimeSlots.size());
    }

    @Test
    public void getFreeRoomReturnsFreeSecondaryWhenAllPrimaryAreNotAvailable() {
        List<Room> rooms = em.createQuery("SELECT r FROM Room r", Room.class).getResultList();
        rooms.stream().filter(r -> r.getRoomType() == RoomType.PRIMARY)
                .forEach(primary -> primary.getTimeSlots()
                        .stream()
                        .filter(ts -> ts.getStart().getYear() == testTime.getYear() &&
                                ts.getStart().getMonth() == testTime.getMonth() &&
                                ts.getStart().getDayOfMonth() == testTime.getDayOfMonth())
                        .forEach(timeSlot -> timeSlot.setReserved(true)));

        rooms.forEach(em::merge);

        int duration = RANDOM.nextInt(((DAY_END.getHour() - DAY_START.getHour()) * (60 / TIME_SLOT_LENGTH)) - testTime.getHour()) + 1;

        Room r = sut.getFreeRoomOnTimeSlot(testTime, duration);

        assertNotNull(r);
        assertEquals(RoomType.SECONDARY, r.getRoomType());

        List<TimeSlot> availableTimeSlots = r.getTimeSlots()
                .stream()
                .filter(ts -> ts.getStart().isEqual(testTime) || (ts.getStart().isAfter(testTime) && ts.getStart().isBefore(testTime.plusMinutes((long) duration *TIME_SLOT_LENGTH))))
                .collect(Collectors.toList());

        assertEquals(duration, availableTimeSlots.size());
    }

    @Test
    public void getFreeRoomWithRequirementsReturnsRoomWithGivenEquipmentAndCapacity() {
        final Integer capacityRequired = 8;
        final Set<EquipmentType> equipmentRequired = new HashSet<>();
        equipmentRequired.add(EquipmentType.TV);
        RoomRequirements rq = new RoomRequirements(capacityRequired, equipmentRequired);
        final int duration = 8;

        Room r = sut.getFreeRoomOnTimeSlot(testTime, duration, rq);

        assertNotNull(r);
        assertEquals(RoomType.PRIMARY, r.getRoomType());

        List<TimeSlot> availableTimeSlots = r.getTimeSlots()
                .stream()
                .filter(ts -> ts.getStart().isEqual(testTime) || (ts.getStart().isAfter(testTime) && ts.getStart().isBefore(testTime.plusMinutes((long) duration *TIME_SLOT_LENGTH))))
                .collect(Collectors.toList());

        assertEquals(duration, availableTimeSlots.size());
        assertEquals(equipmentRequired, r.getEquipment());
    }

    @Test
    public void getFreeRoomWithRequirementsThrowsRoomNotFoundExceptionWhenRequirementsCannotBeMet() {
        final Integer capacityRequired = 5;
        final Set<EquipmentType> equipmentRequired = new HashSet<>();
        equipmentRequired.add(EquipmentType.TV);
        equipmentRequired.add(EquipmentType.DATA_PROJECTOR);
        equipmentRequired.add(EquipmentType.TOUCH_SCREEN);
        RoomRequirements rq = new RoomRequirements(capacityRequired, equipmentRequired);
        final int duration = 8;

        assertThrows(RoomNotFoundException.class, () -> sut.getFreeRoomOnTimeSlot(testTime, duration, rq));
    }
}
