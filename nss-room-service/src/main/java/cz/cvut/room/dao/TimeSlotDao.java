package cz.cvut.room.dao;

import cz.cvut.room.model.Room;
import cz.cvut.room.model.TimeSlot;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import java.time.LocalDateTime;

@Repository
public class TimeSlotDao extends BaseDao<TimeSlot> {
    public TimeSlotDao() {
        super(TimeSlot.class);
    }

    public TimeSlot findByStart(LocalDateTime start, Room room) {
        try {
            return em.createNamedQuery("TimeSlot.findByStartAndRoom", TimeSlot.class).setParameter("start", start)
                    .setParameter("room", room)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
