package cz.cvut.reservation.service;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import cz.cvut.reservation.dao.EventDao;
import cz.cvut.reservation.dao.UserDao;
import cz.cvut.reservation.exception.ValidationException;
import cz.cvut.reservation.model.Event;
import cz.cvut.reservation.model.Reservation;
import cz.cvut.reservation.model.User;
import cz.cvut.reservation.util.Constants;
import cz.cvut.reservation.util.wrappers.UserWrapper;
import cz.cvut.room.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class EventService implements IEventService {
    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

    private final HazelcastInstance hazelcast;
    private final Map<Integer, Event> cache;
    private final EventDao dao;
    //Helper user dao for simpler user handling
    private final UserDao userDao;

    @Autowired
    public EventService(EventDao dao, UserDao userDao) {
        this.dao = dao;
        this.userDao = userDao;
        this.hazelcast = Hazelcast.newHazelcastInstance(createConfig());
        this.cache = hazelcast.getMap(Constants.EVENT_CACHE);
    }

    /**
     * Adds given Reservation under Given Event.
     */
    @Transactional
    public void addReservationUnderEvent(Reservation reservation, Event event){
        Set<Reservation> currReservations = event.getReservations();
        if (currReservations == null) {
            currReservations = new HashSet<>();
            currReservations.add(reservation);
        }

        event.setReservations(currReservations);
        Event updated = dao.update(event);
        cache.put(updated.getId(), updated);
    }

    @Transactional
    public void remove(Event event){
        event.getReservations().forEach(event::removeReservationFromEvent);
        dao.remove(event);
        cache.remove(event.getId());
    }

    /**
     * Adds Users as Event's owners. So they can change the Reservation and the Event.
     *
     * @param usernameToAdd Username of user that will be added as Event's owners.
     * @param event Event to which will be users added as co-owners.
     *
     * @return The new set of users -> owners.
     */
    @Transactional
    public Set<User> addCoOwner(Event event, String usernameToAdd, UserWrapper requestSender) {
        Set<User> currOwners = event.getOwners();

        if (currOwners.stream().noneMatch(u -> u.getUsername().equals(requestSender.getUsername())) && !requestSender.isAdmin())
            throw new ValidationException("Logged in user with username " + requestSender.getUsername() + " has no permission to add owners to event with name: "
                    + event.getName() + " and description: " + event.getDescription());

        currOwners.add(userDao.findByUsername(usernameToAdd));
        event.setOwners(currOwners);
        Event updated = dao.update(event);
        cache.put(updated.getId(), updated);
        return currOwners;
    }

    /**
     * Removes Users from Event's owners. So they can no longer change the Reservation and the Event.
     *
     * @param username Username of the User that will be removed from Event's owners.
     * @param event Event from which will be user removed as co-owners.
     *
     * @return The new set of users -> owners.
     */
    @Transactional
    public Set<User> removeCoOwner(Event event, String username, UserWrapper requestSender){
        Set<User> currOwners = event.getOwners();

        if (currOwners.stream().noneMatch(u -> u.getUsername().equals(username)) && !requestSender.isAdmin())
            throw new ValidationException("Logged in user with username " + username + " has no permission to remove owners from event with name: "
                    + event.getName() + " and description: " + event.getDescription());

        currOwners.remove(currOwners.stream().filter(u -> u.getUsername().equals(username)).findFirst().get());
        event.setOwners(currOwners);
        Event updated = dao.update(event);
        cache.put(updated.getId(), updated);
        return currOwners;
    }

    @Override
    @Transactional
    public Event createEvent(Event e, String usernameOfOwner) {
        Set<User> owners = new HashSet<>();
        owners.add(userDao.findByUsername(usernameOfOwner));
        e.setOwners(owners);
        persist(e);
        return e;
    }

    public List<Event> findAll() {
        return dao.findAll();
    }

    @Transactional
    public void persist(Event event) {
        dao.persist(event);
    }

    @Transactional(readOnly = true)
    public Event find(Integer id) throws NotFoundException {
        if (cache.containsKey(id))
            return cache.get(id);

        Event e = dao.find(id);
        if (e == null)
            throw NotFoundException.create("Event", id);

        cache.put(id, e);
        return e;
    }

    private Config createConfig() {
        Config config = new Config();
        config.addMapConfig(mapConfig());
        return config;
    }

    private MapConfig mapConfig() {
        MapConfig mapConfig = new MapConfig(Constants.EVENT_CACHE);
        mapConfig.setTimeToLiveSeconds(Constants.MAX_TTL_MAP_LIMIT);
        mapConfig.setMaxIdleSeconds(Constants.IDLE_MAP_LIMIT);
        return mapConfig;
    }
}
