package cz.cvut.reservation.service;

import cz.cvut.reservation.dao.EventDao;
import cz.cvut.reservation.dao.ReservationDao;
import cz.cvut.reservation.dao.TimeSlotDao;
import cz.cvut.reservation.exception.EarException;
import cz.cvut.reservation.exception.ValidationException;
import cz.cvut.reservation.model.Event;
import cz.cvut.reservation.model.Reservation;
import cz.cvut.reservation.model.Room;
import cz.cvut.reservation.model.TimeSlot;
import cz.cvut.reservation.util.RoomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cz.cvut.reservation.util.Constants.TIME_SLOT_LENGTH;

@Service
public class ReservationService implements IReservationService{
    private static final Logger LOG = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationDao dao;
    private final ITimeSlotService timeSlotService;

    @Autowired
    public ReservationService(ReservationDao dao, TimeSlotService timeSlotService) {
        this.dao = dao;
        this.timeSlotService = timeSlotService;
    }

    /**
     * Creates new Reservation.
     *
     * @param start LocalDateTime start of Reservation
     * @param duration LocalDateTime duration of Reservation
     * @param room Room under which is Reservation created
     * @param event Event under which Reservation is created
     *
     * @return The new reservation
     */
    @Override
    @Transactional
    public Reservation createReservation(LocalDateTime start, Integer duration, Room room, Event event) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(duration);
        Objects.requireNonNull(room);
        Objects.requireNonNull(event);
        try {
            timeSlotService.checkInputDuration(duration);
        } catch (ValidationException e) {
            throw new ValidationException("ValidationException -> Duration has to be in multiples of 30");
        }
        if (room.getRoomType() == RoomType.SECONDARY && room.getTimeSlots() == null)
            throw new EarException("Cannot create reservation for given room with id: " + room.getId());

        List<TimeSlot> reservationTS = timeSlotService.separateTimeIntoTimeSlots(start, duration, room);
        if (reservationTS.size() < duration)
            throw new EarException("Room is closed for part of given duration");
        boolean timeSlotsAvailable = timeSlotService.checkAvailability(reservationTS);
        if (!timeSlotsAvailable){
            throw new ValidationException("ValidationException -> Room is not available for the given duration. It is already reserved");
        }
        final Reservation reservation = new Reservation(start, event);
        for (TimeSlot ts : reservationTS) {
            ts.setReserved(true);
            reservation.addTimeSlotToReservedTimeSlots(ts);
            timeSlotService.update(ts);
        }
        dao.persist(reservation);
        return reservation;
    }

    @Transactional
    public Reservation updateReservation(Reservation reservation){
        dao.update(reservation);
        return reservation;
    }

    @Transactional
    public void removeReservation(Reservation reservation){
        reservation.getEvent().removeReservationFromEvent(reservation);
        for (TimeSlot ts : reservation.getReservedTimeSlots()){
            ts.setReserved(false);
            timeSlotService.update(ts);
        }
        dao.remove(reservation);
    }

    @Transactional(readOnly = true)
    public Reservation find(Integer id) {
        return dao.find(id);
    }

    public Set<Reservation> getReservations(Event event){
        return event.getReservations();
    }

    @Transactional
    public void persist(Reservation reservation) {
        dao.persist(reservation);
    }
}

