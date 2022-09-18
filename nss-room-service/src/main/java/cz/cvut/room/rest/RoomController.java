package cz.cvut.room.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.room.dto.RoomDto;
import cz.cvut.room.exception.NotFoundException;
import cz.cvut.room.exception.RoomNotFoundException;
import cz.cvut.room.model.Room;
import cz.cvut.room.rest.util.RestUtils;
import cz.cvut.room.service.IRoomService;
import cz.cvut.room.util.Constants;
import cz.cvut.room.util.wrappers.RoomReservationRequirementsWrapper;
import cz.cvut.room.util.wrappers.UserWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.List;

@RestController
public class RoomController {
    private static final Logger LOG = LoggerFactory.getLogger(RoomController.class);

    private final IRoomService roomService;
    private final ObjectMapper objectMapper;

    @Autowired
    public RoomController(IRoomService roomService, ObjectMapper objectMapper) {
        this.roomService = roomService;
        this.objectMapper = objectMapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RoomDto> getRooms() {
        return roomService.getRooms();
    }

    @GetMapping(value = "/{roomId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Room getRoom(@PathVariable Integer roomId) {
        return roomService.find(roomId);
    }

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Room addRoom(@RequestBody Room roomToAdd, HttpServletRequest request) {
        UserWrapper u = (UserWrapper) request.getAttribute(Constants.USER_WRAPPER_ATTR);
        roomService.addRoom(roomToAdd, u);

        return roomToAdd;
    }

    @DeleteMapping(value = "/{roomId}")
    public ResponseEntity<Void> removeRoom(@PathVariable Integer roomId, HttpServletRequest request) {
        UserWrapper u = (UserWrapper) request.getAttribute(Constants.USER_WRAPPER_ATTR);
        roomService.removeRoom(roomId, u);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/free", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Room> getRoomOnTimeSlot(@RequestBody RoomReservationRequirementsWrapper wrapper) {
        Room r = roomService.getFreeRoomOnTimeSlot(wrapper.getStart(), wrapper.getDuration());

        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", r.getId());
        return new ResponseEntity<>(r, headers, HttpStatus.OK);
    }

    @KafkaListener(topics = Constants.MAIN_KAFKA_TOPIC, groupId = Constants.KAFKA_GROUP_ID)
    @SendTo
    public String listerForRoomRequest(String roomId) throws JsonProcessingException {
        LOG.info("Received room request in group " + Constants.KAFKA_GROUP_ID + ": Wanting room with id: " + roomId);
        try {
            Room r = roomService.find(Integer.parseInt(roomId));
            return objectMapper.writeValueAsString(r);
        } catch (NotFoundException ex) {
            LOG.warn(ex.getMessage());
            return "null";
        }
    }

    @ExceptionHandler(RoomNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleRoomNotFoundException(RoomNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }
}
