package cz.cvut.reservation.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Reservation extends AbstractEntity{

    private LocalDateTime start;

    @OneToMany
    private List<TimeSlot> reservedTimeSlots = new ArrayList<>();

    @ManyToOne
    private Event event;

    public Reservation() {
    }

    public Reservation(LocalDateTime start, Event event){
        this.event = event;
        this.start = start;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<TimeSlot> getReservedTimeSlots() {
        return reservedTimeSlots;
    }

    public void setReservedTimeSlots(List<TimeSlot> reservedTimeSlots) {
        this.reservedTimeSlots = reservedTimeSlots;
    }

    public void addTimeSlotToReservedTimeSlots(TimeSlot timeSlot){
        reservedTimeSlots.add(timeSlot);
    }
}
