package cz.cvut.reservation.service;

import cz.cvut.reservation.dao.TimeSlotDao;
import cz.cvut.reservation.exception.EarException;
import cz.cvut.reservation.exception.ValidationException;
import cz.cvut.reservation.model.Room;
import cz.cvut.reservation.model.TimeSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cz.cvut.reservation.util.Constants.*;

@Service
public class TimeSlotService implements ITimeSlotService{
    private static final Logger LOG = LoggerFactory.getLogger(TimeSlotService.class);

    private final TimeSlotDao dao;
    private List<TimeSlot> allTimeSlots;

    @Autowired
    public TimeSlotService(TimeSlotDao dao) {
        this.dao = dao;
    }

    /**
     * Separates time into TimeSlots for given room.
     *
     * @param start LocalDateTime start of Reservation
     * @param duration Integer duration of Reservation (How many timeslots to find)
     * @param room Room under which is Reservation created
     */
   public List<TimeSlot> separateTimeIntoTimeSlots(LocalDateTime start, Integer duration, Room room){
       Objects.requireNonNull(start);
       Objects.requireNonNull(duration);
       Objects.requireNonNull(room);
       if (!checkInputDuration(duration))
            throw new ValidationException("ValidationException -> Duration has to be in multiples of 30");

       allTimeSlots = dao.findAll();
       LocalDateTime end = start.plusMinutes(duration);
       List<TimeSlot> resultTSWithoutFirstOne = allTimeSlots
               .stream()
               .filter(ts -> ts.getStart().isBefore(end))
               .filter(ts -> ts.getStart().isAfter(start))
               .collect(Collectors.toList());
       List<TimeSlot> resultStartingTS = allTimeSlots
               .stream()
               .filter(ts -> ts.getStart().isBefore(end))
               .filter(ts -> ts.getStart().isEqual(start))
               .collect(Collectors.toList());

       List<TimeSlot> result = resultStartingTS;
       result.addAll(resultTSWithoutFirstOne);
       Collections.sort(result);
       if(result.isEmpty())
           throw new EarException("No existing TimeSlots for given time");
       return result;
   }


   public boolean checkInputDuration(Integer duration){
       if (duration != null && duration < DAY_END.getHour() - DAY_START.getHour())
           return true;
       throw new ValidationException("ValidationException -> Duration has to be in multiples of 30");
   }

    @Override
    public void update(TimeSlot timeSlot) {
        dao.update(timeSlot);
    }

    /**
    * Checks if all TimeSlots are free to be reserved.
    * @return boolean -> if all timeSlots are free
    */
    public boolean checkAvailability(List<TimeSlot> timeSlots){
        Objects.requireNonNull(timeSlots);
        for (TimeSlot ts : timeSlots){
            if (ts.isReserved()){
                return false;
            }
        }
        return true;
    }

    /**
     * Generates TimeSlots in Database. Existing TimeSlots in DB that are in between given time range
     * will be skipped to avoid duplicities in DB.
     *
     * @param start LocalDateTime start of Reservation
     * @param end Integer duration of Reservation
     * @param rooms list of Rooms under which are TimeSlots created
     */
    @Transactional
    public List<TimeSlot> generateTimeSlots(LocalDateTime start, LocalDateTime end, List<Room> rooms){
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        Objects.requireNonNull(rooms);
        long minutes = ChronoUnit.MINUTES.between(start, end);
        try {
            checkInputDuration((int) minutes);
        } catch (ValidationException e){
            LOG.warn(e.getMessage());
            throw e;
        }
        List<TimeSlot> result = new ArrayList<>();
        LocalDateTime tmpDateTime = start;
        for (Room room : rooms) {
            for (int i = 0; i < minutes / TIME_SLOT_LENGTH; i++) {
                if (dao.findByStart(tmpDateTime, room) != null){
                    tmpDateTime = tmpDateTime.plusMinutes(TIME_SLOT_LENGTH);
                    continue;
                }
                TimeSlot tmpTimeSlot = new TimeSlot(room, tmpDateTime);
                tmpDateTime = tmpDateTime.plusMinutes(TIME_SLOT_LENGTH);
                result.add(tmpTimeSlot);
            }
            tmpDateTime = start;
        }
        removeTimeSlotsOutsideOpeningHours(result);
        dao.persist(result);
        return result;
    }

    /**
     * Checks if all TimeSlots are free to be reserved.
     */
    public void removeTimeSlotsOutsideOpeningHours(List<TimeSlot> timeSlots){
        timeSlots.removeIf(ts -> (ts.getStart().getHour() < ts.getRoom().getOpeningHour()) || (ts.getStart().getHour() >= ts.getRoom().getClosingHour()));
    }
}
