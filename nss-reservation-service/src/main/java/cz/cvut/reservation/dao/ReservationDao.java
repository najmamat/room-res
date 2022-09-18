package cz.cvut.reservation.dao;

import cz.cvut.reservation.model.Reservation;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationDao extends BaseDao<Reservation>{
    protected ReservationDao() {
        super(Reservation.class);
    }

    //public Reservation findReservation
}
