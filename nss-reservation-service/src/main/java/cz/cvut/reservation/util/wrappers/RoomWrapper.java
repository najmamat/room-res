package cz.cvut.reservation.util.wrappers;

import cz.cvut.reservation.model.Room;

import java.util.List;

public class RoomWrapper {

    private List<Room> rooms;

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}
