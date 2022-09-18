package cz.cvut.reservation.service;

import cz.cvut.reservation.model.Event;
import cz.cvut.reservation.model.Reservation;
import cz.cvut.reservation.model.Room;
import cz.cvut.reservation.model.User;
import cz.cvut.reservation.util.wrappers.UserWrapper;
import cz.cvut.room.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface IEventService {
    void addReservationUnderEvent(Reservation reservation, Event event);
    Set<User> addCoOwner(Event event, String usernameToAdd, UserWrapper requestSender);
    Set<User> removeCoOwner(Event event, String username, UserWrapper requestSender);
    Event createEvent(Event e, String usernameOfOwner);
    void remove(Event event);
    void persist(Event event);
    Event find(Integer id) throws NotFoundException;
    List<Event> findAll();
}
