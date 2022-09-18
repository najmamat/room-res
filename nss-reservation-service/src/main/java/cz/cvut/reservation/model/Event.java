package cz.cvut.reservation.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
public class Event extends AbstractEntity{
    private String name;

    private String description;

    @ManyToMany(fetch=FetchType.EAGER)
    private Set<User> owners;

    @OneToMany(orphanRemoval = true, mappedBy ="event", fetch= FetchType.EAGER)
    private Set<Reservation> reservations;

    public Event() {
    }

    public Event(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<User> getOwners() {
        return owners;
    }

    public void setOwners(Set<User> owners) {
        this.owners = owners;
    }

    public Set<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(Set<Reservation> reservations) {
        this.reservations = reservations;
    }

    public void removeReservationFromEvent(Reservation reservationToRemove) {
        reservations.removeIf(r -> r == reservationToRemove);
    }

    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", owners=" + owners +
                ", reservations=" + reservations +
                '}';
    }
}
