package cz.cvut.reservation.dto;

import cz.cvut.reservation.model.Event;
import cz.cvut.reservation.model.Reservation;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.stream.Collectors;

public class EventDto {
    @Getter @Setter
    private Integer id;

    private String name;

    private String description;

    private Set<UserDto> owners;

    private Set<Reservation> reservations;

    public EventDto() {
    }

    public EventDto(Event event) {
        this.name = event.getName();
        this.description = event.getDescription();
        this.owners = event.getOwners().stream().map(UserDto::new).collect(Collectors.toSet());
        this.reservations = event.getReservations();
        this.id = event.getId();
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

    public Set<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(Set<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Set<UserDto> getOwners() {
        return owners;
    }

    public void setOwners(Set<UserDto> owners) {
        this.owners = owners;
    }

    @Override
    public String toString() {
        return "EventDto{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", owners=" + owners +
                ", reservations=" + reservations +
                '}';
    }
}
