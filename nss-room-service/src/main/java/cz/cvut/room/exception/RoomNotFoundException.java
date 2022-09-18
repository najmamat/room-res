package cz.cvut.room.exception;

import cz.cvut.room.util.Constants;

import java.time.LocalDateTime;

public class RoomNotFoundException extends EarException {
    public RoomNotFoundException(String message) {
        super(message);
    }

    public RoomNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static RoomNotFoundException create(LocalDateTime start, Integer duration) {
        return new RoomNotFoundException("Room not found for time block from " + start.toString() + " with duration " + duration + " timeslots (" + Constants.TIME_SLOT_LENGTH * duration + " minutes).");
    }
}
