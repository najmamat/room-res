package cz.cvut.reservation.dao;

import cz.cvut.reservation.model.Event;
import org.springframework.stereotype.Repository;

@Repository
public class EventDao extends BaseDao<Event>{
    protected EventDao() {
        super(Event.class);
    }

    //public Reservation findReservation
}