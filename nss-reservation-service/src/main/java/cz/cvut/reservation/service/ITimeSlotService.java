package cz.cvut.reservation.service;

import cz.cvut.reservation.model.Room;
import cz.cvut.reservation.model.TimeSlot;

import java.time.LocalDateTime;
import java.util.List;

public interface ITimeSlotService {
    List<TimeSlot> separateTimeIntoTimeSlots(LocalDateTime start, Integer duration, Room room);
    boolean checkAvailability(List<TimeSlot> timeSlots);
    boolean checkInputDuration(Integer duration);
    void update(TimeSlot timeSlot);
    List<TimeSlot> generateTimeSlots(LocalDateTime start, LocalDateTime end, List<Room> rooms);
}
