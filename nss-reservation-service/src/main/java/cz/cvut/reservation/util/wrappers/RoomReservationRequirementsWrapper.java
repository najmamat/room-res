package cz.cvut.reservation.util.wrappers;

import java.time.LocalDateTime;

public class RoomReservationRequirementsWrapper {
    private LocalDateTime start;
    private Integer duration;

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
