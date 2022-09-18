package cz.cvut.reservation.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.reservation.exception.NotFoundException;
import cz.cvut.reservation.exception.ValidationException;
import cz.cvut.reservation.model.Event;
import cz.cvut.reservation.model.Reservation;
import cz.cvut.reservation.model.Room;
import cz.cvut.reservation.rest.util.RestUtils;
import cz.cvut.reservation.service.EventService;
import cz.cvut.reservation.service.IEventService;
import cz.cvut.reservation.service.IReservationService;
import cz.cvut.reservation.service.ReservationService;
import cz.cvut.reservation.util.Constants;
import cz.cvut.reservation.util.wrappers.ReservationInfoWrapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

@RestController
public class ReservationController {

    private static final Logger LOG = LoggerFactory.getLogger(ReservationController.class);

    private final IReservationService reservationService;

    private final IEventService eventService;

    private final ReplyingKafkaTemplate<String, String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Autowired
    public ReservationController(ReservationService reservationService, EventService eventService, ReplyingKafkaTemplate<String, String, String> kafkaTemplate, ObjectMapper objectMapper){
        this.reservationService = reservationService;
        this.eventService = eventService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createReservation(@RequestBody ReservationInfoWrapper reservationInfo) throws InterruptedException, ExecutionException {
        final LocalDateTime start = reservationInfo.getStart();
        final Integer duration = reservationInfo.getDuration();
        final Integer roomId = reservationInfo.getRoomId();
        final Integer eventId = reservationInfo.getEventId();
        if (roomId == null || eventId == null) {
            reservationService.persist(new Reservation());
            final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("");
            return new ResponseEntity<>(headers, HttpStatus.CREATED);
        }

        Room r = requestRoom(roomId.toString());

        Event e = eventService.find(eventId);
        Reservation result;

        if (r == null || e == null) {
            reservationService.persist(new Reservation());
            final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("");
            return new ResponseEntity<>(headers, HttpStatus.CREATED);
        }
        else
            result = reservationService.createReservation(start, duration, r , e);
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", result.getId());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeReservation(@PathVariable Integer reservationId) {
        final Reservation reservation = reservationService.find(reservationId);
        reservationService.removeReservation(reservation);
        LOG.debug("reservation removed");
    }

    @GetMapping(value = "/{reservationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Reservation getReservation(@PathVariable Integer reservationId){
        final Reservation reservation = reservationService.find(reservationId);
        if(reservation == null){
            throw NotFoundException.create("Event", reservationId);
        }
        return reservation;
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateReservation(@PathVariable Integer id, @RequestBody Reservation reservation) {
        final Reservation original = reservationService.find(id);
        if (!original.getId().equals(reservation.getId())) {
            throw new ValidationException("Reservation identifier in the data does not match the one in the request URL.");
        }
        reservationService.updateReservation(reservation);
        LOG.debug("Updated reservation {}.", reservation);
    }

    @PostMapping(value = "/{reservationId}/event", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addReservationToEvent(@PathVariable Integer reservationId, @RequestBody Event event) {
        final Reservation reservation = reservationService.find(reservationId);
        if(reservation == null){
            throw NotFoundException.create("Reservation", reservationId);
        }
        eventService.addReservationUnderEvent(reservation, event);
        LOG.debug("Reservation {} added under event {}.", reservation, event);
    }

    private Room requestRoom(String roomId) throws ExecutionException, InterruptedException {
        // create producer record
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(Constants.MAIN_KAFKA_TOPIC, roomId);
        // set reply topic in header
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, Constants.REPLY_TOPIC.getBytes()));
        // post in kafka topic
        RequestReplyFuture<String, String, String> sendAndReceive = kafkaTemplate.sendAndReceive(record);

        // confirm if producer produced successfully
        SendResult<String, String> sendResult = sendAndReceive.getSendFuture().get();
        LOG.info("Sent ok: " + sendResult.getRecordMetadata().toString());

        // get consumer record
        ConsumerRecord<String, String> consumerRecord = sendAndReceive.get();
        LOG.info("Value received: " + consumerRecord.value());
        // return consumer value
        if (consumerRecord.value().equals("null"))
            return null;
        try {
            return objectMapper.readValue(consumerRecord.value(), Room.class);
        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }

        return null;
    }
}
