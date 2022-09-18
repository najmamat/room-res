package cz.cvut.room.service;


import cz.cvut.room.dto.RoomDto;
import cz.cvut.room.model.Room;
import cz.cvut.room.util.RoomRequirements;
import cz.cvut.room.util.wrappers.UserWrapper;

import java.time.LocalDateTime;
import java.util.List;

public interface IRoomService {
    Room getFreeRoomOnTimeSlot(LocalDateTime date, Integer duration, RoomRequirements requirements);
    default Room getFreeRoomOnTimeSlot(LocalDateTime date, Integer duration) {
        return getFreeRoomOnTimeSlot(date, duration, null);
    };
    void addRoom(Room roomToAdd, UserWrapper userWrapper);
    void removeRoom(Integer id, UserWrapper userWrapper);
    List<RoomDto> getRooms();
    Room find(Integer id);
}
