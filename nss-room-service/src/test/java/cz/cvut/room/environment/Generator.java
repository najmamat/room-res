package cz.cvut.room.environment;

import cz.cvut.room.model.Room;
import cz.cvut.room.model.TimeSlot;
import cz.cvut.room.util.EquipmentType;
import cz.cvut.room.util.RoomType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static cz.cvut.room.util.Constants.*;

public class Generator {
    private static final Random RAND = new Random();

    public static int randomInt() {
        return RAND.nextInt();
    }

    public static boolean randomBoolean() {
        return RAND.nextBoolean();
    }

    public static Room getTestRoom() {
        final Room toCreate = new Room();
        toCreate.setCode("test room ");
        toCreate.setRoomType(RoomType.PRIMARY);
        toCreate.setCapacity(10);
        Set<EquipmentType> equipment = new HashSet<>();
        equipment.add(EquipmentType.TV);
        toCreate.setEquipment(equipment);
        return toCreate;
    }
    
    public static List<Room> generateRooms() {
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
        return rooms;
    }
    
    public static Set<TimeSlot> generateTimeSlots(Room r) {
        Set<TimeSlot> timeSlots = new HashSet<>();
        for (int day = 1; day <= TEST_MONTH.length(false); day++) {
            for (int i = 0; i < ((DAY_END.getHour() - DAY_START.getHour()) * (60 / TIME_SLOT_LENGTH)); i++) {
                timeSlots.add(new TimeSlot(r, LocalDateTime.of(LocalDate.of(2021, TEST_MONTH.getValue(), day), DAY_START.plusMinutes(i * 30L))));
            }
        }
        return timeSlots;
    }

    public static List<TimeSlot> generateTimeSlotsWithList(Room r) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        for (int day = 1; day <= TEST_MONTH.length(false); day++) {
            for (int i = 0; i < ((DAY_END.getHour() - DAY_START.getHour()) * (60 / TIME_SLOT_LENGTH)); i++) {
                timeSlots.add(new TimeSlot(r, LocalDateTime.of(LocalDate.of(2021, TEST_MONTH.getValue(), day), DAY_START.plusMinutes(i * 30L))));
            }
        }
        return timeSlots;
    }
}
