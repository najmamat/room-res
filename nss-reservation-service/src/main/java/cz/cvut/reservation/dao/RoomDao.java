package cz.cvut.reservation.dao;

import cz.cvut.reservation.model.Room;
import cz.cvut.reservation.util.RoomType;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import java.util.List;

@Repository
public class RoomDao extends BaseDao<Room> {
    public RoomDao() {
        super(Room.class);
    }

    public List<Room> getPrimary() {
        try {
            return em.createNamedQuery("Room.findByRoomType", Room.class).setParameter("roomType", RoomType.PRIMARY)
                    .getResultList();
        } catch (NoResultException e) {

            return null;
        }
    }

    public List<Room> getSecondary() {
        try {
            return em.createNamedQuery("Room.findByRoomType", Room.class).setParameter("roomType", RoomType.SECONDARY)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}
