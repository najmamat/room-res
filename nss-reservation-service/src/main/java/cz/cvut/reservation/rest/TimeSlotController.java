package cz.cvut.reservation.rest;

import cz.cvut.reservation.model.Room;
import cz.cvut.reservation.service.ITimeSlotService;
import cz.cvut.reservation.service.TimeSlotService;
import cz.cvut.reservation.util.wrappers.RoomWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/timeslots")
public class TimeSlotController {
    private static final Logger LOG = LoggerFactory.getLogger(TimeSlotController.class);

    private final ITimeSlotService timeSlotService;

    @Autowired
    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @PostMapping(value = "/startandendwithmapvariable/{startDateTime}/{endDateTime}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void generateTimeSlots(@PathVariable Map<String, LocalDateTime> pathVariables, @RequestBody RoomWrapper rooms){
        LocalDateTime start = pathVariables.get("start");
        LocalDateTime end = pathVariables.get("end");
        List<Room> roomsList = rooms.getRooms();
        timeSlotService.generateTimeSlots(start, end, roomsList);
        LOG.debug("Updated timeslots from {},", start);
        LOG.debug("until {}.", end);
    }
}
