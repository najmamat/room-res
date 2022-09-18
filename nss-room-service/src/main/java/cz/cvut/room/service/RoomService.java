package cz.cvut.room.service;

import cz.cvut.room.dao.RoomDao;
import cz.cvut.room.dao.TimeSlotDao;
import cz.cvut.room.dto.RoomDto;
import cz.cvut.room.exception.NoPermissionException;
import cz.cvut.room.exception.NotFoundException;
import cz.cvut.room.exception.RoomNotFoundException;
import cz.cvut.room.model.Room;
import cz.cvut.room.model.TimeSlot;
import cz.cvut.room.util.RoomRequirements;
import cz.cvut.room.util.wrappers.UserWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.cvut.room.util.Constants.*;

@Service
public class RoomService implements IRoomService {
    private static final Logger LOG = LoggerFactory.getLogger(RoomService.class);

    private TimeSlotDao timeSlotDao;
    private final RoomDao dao;
    @Autowired
    public RoomService(TimeSlotDao timeSlotDao, RoomDao dao) {
        this.timeSlotDao = timeSlotDao;
        this.dao = dao;
    }

    @Transactional
    @Override
    public void addRoom(Room roomToAdd, UserWrapper userWrapper) {
        if (userWrapper.isAdmin()) {
            dao.persist(roomToAdd);
            LOG.info("Room with code " + roomToAdd.getCode() + " created by admin " + userWrapper.getUsername());
        } else
            throw new NoPermissionException("Room with code: " + roomToAdd.getCode() + " could not be added. Logged in user does not have permission to add room (is not ADMIN).");
    }

    @Transactional
    @Override
    public void removeRoom(Integer id, UserWrapper userWrapper) {
        Room toRemove = dao.find(id);
        if (toRemove == null) {
            throw NotFoundException.create("Room", id);
        }
        if (userWrapper.isAdmin())
            dao.remove(toRemove);
        else
            throw new NoPermissionException("Room with id: " + id + " could not be removed. Logged in user does not have permission to add room (is not ADMIN).");
    }

    /**
     * Finds and returns Room, if there is Room with free TimeSlots corresponding to given parameters.
     * @param date Start date of time slot (reservation)
     * @param duration Number of time slots that need to be reserved.
     * @throws RoomNotFoundException if room for given parameters is not found
     * @return Primary room / secondary room (when primary not available) / null (when primary and secondary not available)
     */
    @Transactional
    public Room getFreeRoomOnTimeSlot(LocalDateTime date, Integer duration, RoomRequirements requirements) throws RoomNotFoundException {
        Room r = getFreePrimaryRoomOnTimeSlot(date, duration, requirements);
        if (r != null)
            return r;
        return getFreeSecondaryRoomOnTimeSlot(date, duration, requirements);
    }

    /**
     * Iterates through Secondary rooms and either creates and reserves new TimeSlots, or tries to reserve free TimeSlots, that have already been created.
     * @param date Start date of time slot (reservation)
     * @param duration Number of time slots that need to be reserved.
     * @throws RoomNotFoundException if room for given parameters is not found
     * @return Secondary room / null (when secondary not available)
     */
    @Transactional
    public Room getFreeSecondaryRoomOnTimeSlot(LocalDateTime date, Integer duration, RoomRequirements requirements) throws RoomNotFoundException {
        roomLoop:
        for (Room r : dao.getSecondary()) {
            if (r.getTimeSlots() != null && r.getTimeSlots().stream().anyMatch(ts -> ts.getStart().isEqual(date)))
                continue;
            if (requirements != null) {
                if (r.getCapacity() < requirements.getCapacityRequired() || !r.getEquipment().equals(requirements.getEquipmentRequired()))
                    continue;
            }

            List<TimeSlot> timeSlotsForDay = new ArrayList<>(r.getTimeSlots());
            if (timeSlotsForDay.size() == 0) {
                for (int i = 0; i < ((DAY_END.getHour() - DAY_START.getHour()) * (60 / TIME_SLOT_LENGTH)); i++) {
                    timeSlotsForDay.add(new TimeSlot(r, LocalDateTime.of(date.toLocalDate(), DAY_START.plusMinutes(i * 30L))));
                }
                r.setTimeSlots(new HashSet<>(timeSlotsForDay));
                dao.update(r);
                timeSlotDao.persist(timeSlotsForDay);
                LOG.info("Timeslot room id " + timeSlotDao.findByStart(LocalDateTime.of(date.toLocalDate(), DAY_START), r).getRoom().getId());
            }

            r = dao.find(r.getId());
            List<TimeSlot> persistedSortedTimeSlots = getSortedTimeSlotsByHoursForSpecificDay(date, r.getTimeSlots());
            TimeSlot startSlot = persistedSortedTimeSlots.stream().filter(ts -> ts.getStart().isEqual(date) && !ts.isReserved()).findFirst().orElse(null);
            if (startSlot != null) {
                int indexOfStartSlot = persistedSortedTimeSlots.indexOf(startSlot);
                try {
                    for (int i = indexOfStartSlot; i < indexOfStartSlot + duration; i++) {
                        if (persistedSortedTimeSlots.get(i).isReserved()) {
                            continue roomLoop;
                        }
                    }

                    //persistedSortedTimeSlots.subList(indexOfStartSlot, indexOfStartSlot + duration).forEach(ts -> ts.setReserved(true));
                    //updateTimeSlotsForDay(r, persistedSortedTimeSlots);
                    return r;
                } catch (IndexOutOfBoundsException e) {
                    LOG.info("No more end TimeSlot to find for current Room with Id: " + r.getId() + " and code: " + r.getCode() +
                            ". Date is " + date);
                }
            } else
                LOG.info("No valid start TimeSlot found for current Room with Id: " + r.getId() + " and code: " + r.getCode() +
                    ". Trying another room.");
        }
        LOG.warn("No more secondary rooms to iterate through when finding TimeSlot for date: " + date + " and duration: " + duration);
        throw RoomNotFoundException.create(date, duration);
    }

    /**
     * Iterates through Primary rooms and tries to reserve free TimeSlots.
     * @param date Start date of time slot (reservation)
     * @param duration Number of time slots that need to be reserved.
     * @return Primary room / null (when Primary not available)
     */
    @Transactional
    public Room getFreePrimaryRoomOnTimeSlot(LocalDateTime date, Integer duration, RoomRequirements requirements) {
        for (Room r : dao.getPrimary()) {
            if (requirements != null) {
                if (r.getCapacity() < requirements.getCapacityRequired() || !r.getEquipment().equals(requirements.getEquipmentRequired()))
                    continue;
            }

            List<TimeSlot> sortedTimeSlots = getSortedTimeSlotsByHoursForSpecificDay(date, r.getTimeSlots());
            TimeSlot startSlot = sortedTimeSlots.stream().filter(ts -> ts.getStart().isEqual(date) && !ts.isReserved()).findFirst().orElse(null);
            if (startSlot != null) {
                int indexOfStartSlot = sortedTimeSlots.indexOf(startSlot);
                try {
                    TimeSlot potentialEndSlot = sortedTimeSlots.get(indexOfStartSlot + duration);
                    if (potentialEndSlot != null && potentialEndSlot.getStart().isEqual(date.plus((long) TIME_SLOT_LENGTH * duration, ChronoUnit.MINUTES))) {
                        //sortedTimeSlots.subList(indexOfStartSlot, indexOfStartSlot + duration).forEach(ts -> ts.setReserved(true));
                        //updateTimeSlotsForDay(r, sortedTimeSlots);
                        return r;
                    }
                    else
                        LOG.info("No valid end TimeSlot found for current Room with Id: " + r.getId() + " and code: " + r.getCode() +
                                ". Date is " + date);
                } catch (IndexOutOfBoundsException e) {
                    LOG.info("No more end TimeSlot to find for current Room with Id: " + r.getId() + " and code: " + r.getCode() +
                            ". Date is " + date);
                    return null;
                }
            } else
                LOG.info("No valid start TimeSlot found for current Room with Id: " + r.getId() + " and code: " + r.getCode() +
                    ". Trying another room.");
        }
        LOG.warn("No valid primary Room found with start TimeSlot date: " + date + " and duration: " + duration);
        return null;
    }

    @Transactional
    public List<RoomDto> getRooms() {
        return dao.findAll().stream().map(RoomDto::new).collect(Collectors.toList());
    }

    @Transactional
    public Room find(Integer id) {
        Room temp = dao.find(id);
        if (temp == null) {
            throw NotFoundException.create("Room", id);
        }
        return temp;
    }

    /**
     * Sorts TimeSlots with specific Date by hour from the earliest to the latest TimeSlot.
     * @param date Date of TimeSlots that are needed to be sorted
     * @param timeSlots TimeSlots to sort
     * @return List of sorted TimeSlots
     */
    private List<TimeSlot> getSortedTimeSlotsByHoursForSpecificDay(LocalDateTime date, Set<TimeSlot> timeSlots) {
        List<TimeSlot> timeSlotList = new ArrayList<>(timeSlots)
                .stream()
                .parallel()
                .filter(ts -> ts.getStart().getDayOfMonth() == date.getDayOfMonth()
                        && ts.getStart().getMonth() == date.getMonth()
                        && ts.getStart().getYear() == date.getYear())
                .sorted()
                .collect(Collectors.toList());

        return timeSlotList;
    }
}
