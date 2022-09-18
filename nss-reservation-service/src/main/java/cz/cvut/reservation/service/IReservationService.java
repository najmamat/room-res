package cz.cvut.reservation.service;

import cz.cvut.reservation.model.Event;
import cz.cvut.reservation.model.Reservation;
import cz.cvut.reservation.model.Room;

import java.time.LocalDateTime;
import java.util.Set;

public interface IReservationService {
    Reservation createReservation(LocalDateTime start, Integer durationMinutes, Room room, Event event);
    Reservation updateReservation(Reservation reservation);
    void removeReservation(Reservation reservation);
    Reservation find(Integer id);
    Set<Reservation> getReservations(Event event);
    void persist(Reservation reservation);
}
