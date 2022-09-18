package cz.cvut.reservation.rest;

import cz.cvut.reservation.dto.EventDto;
import cz.cvut.reservation.dto.UserDto;
import cz.cvut.reservation.dto.UsernameDto;
import cz.cvut.reservation.model.Event;
import cz.cvut.reservation.rest.util.RestUtils;
import cz.cvut.reservation.service.EventService;
import cz.cvut.reservation.service.IEventService;
import cz.cvut.reservation.util.Constants;
import cz.cvut.reservation.util.wrappers.UserWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events")
public class EventController {

    private static final Logger LOG = LoggerFactory.getLogger(EventController.class);

    private final IEventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> createEvent(@RequestBody Event event, HttpServletRequest request){
        UserWrapper userWrapper = (UserWrapper) request.getAttribute(Constants.USER_WRAPPER_ATTR);
        Event createdEvent = eventService.createEvent(event, userWrapper.getUsername());
        LOG.debug("created event {}.", event);
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", event.getId());
        return new ResponseEntity<>(createdEvent.getId(), headers, HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEvent(@PathVariable Integer eventId){
        final Event toRemove = eventService.find(eventId);
        if (toRemove == null) {
            LOG.warn("No event to remove because event with ID: " + eventId + " does not exist.");
            return;
        }
        eventService.remove(toRemove);
        LOG.debug("event removed");
    }

    @GetMapping(value = "/{eventId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EventDto getEvent(@PathVariable Integer eventId){
        return new EventDto(eventService.find(eventId));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EventDto> getEvents(){
        return eventService.findAll().stream().map(EventDto::new).collect(Collectors.toList());
    }

    @PostMapping(value = "/{id}/owners", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<UserDto> addCoOwnerToEvent(@PathVariable Integer id, @RequestBody UsernameDto payload, HttpServletRequest request) {
        final Event event = eventService.find(id);
        UserWrapper userWrapper = (UserWrapper) request.getAttribute(Constants.USER_WRAPPER_ATTR);
        LOG.debug("User {} added under Event {}.", payload.getUsername(), event);
        return eventService.addCoOwner(event, payload.getUsername(), userWrapper).stream().map(UserDto::new).collect(Collectors.toList());
    }


    /**
     * Method for admin to remove coowner by username from event with specific id.
     * @param eventId
     * @param request
     */
    @DeleteMapping(value = "/{eventId}/owners")
    public List<UserDto> removeCoOwnerFromEvent(@PathVariable Integer eventId, @RequestBody UsernameDto payload, HttpServletRequest request) {
        final Event event = eventService.find(eventId);
        UserWrapper userWrapper = (UserWrapper) request.getAttribute(Constants.USER_WRAPPER_ATTR);
        return eventService.removeCoOwner(event, payload.getUsername(), userWrapper).stream().map(UserDto::new).collect(Collectors.toList());
    }

//    @DeleteMapping(value = "/{eventId}/owners")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void removeCoOwnerFromEvent(@PathVariable Integer eventId, HttpServletRequest request) {
//        final Event event = eventService.find(eventId);
//
//        UserWrapper userWrapper = (UserWrapper) request.getAttribute(Constants.USER_WRAPPER_ATTR);
//
//        eventService.removeCoOwner(event, userWrapper.getUsername());
//        LOG.debug("User {} removed from Event {}.", userWrapper, event);
//    }
}
