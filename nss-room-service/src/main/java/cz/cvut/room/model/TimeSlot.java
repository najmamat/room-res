package cz.cvut.room.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NamedQueries({
        @NamedQuery(name = "TimeSlot.findByStartAndRoom", query = "SELECT t FROM TimeSlot t WHERE t.start = :start AND t.room = :room")
})
@JsonIgnoreProperties(value = { "room" })
public class TimeSlot extends AbstractEntity implements Comparable<TimeSlot> {

    private LocalDateTime start;

    private boolean isReserved = false;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    public TimeSlot() {
    }

    public TimeSlot(Room room, LocalDateTime start){
        this.room = room;
        this.start = start;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public void setReserved(boolean reserved) {
        isReserved = reserved;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    @Override
    public int compareTo(TimeSlot comparedTimeSlot) {
        if (getStart().isEqual(comparedTimeSlot.getStart()))
            return 0;
        if (comparedTimeSlot.getStart().isAfter(getStart())) {
            return -1;
        }
        else
            return 1;
    }
}
