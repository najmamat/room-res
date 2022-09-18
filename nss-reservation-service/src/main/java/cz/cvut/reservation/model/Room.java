package cz.cvut.reservation.model;

import cz.cvut.reservation.util.EquipmentType;
import cz.cvut.reservation.util.RoomType;

import javax.persistence.*;
import java.util.Set;

@Entity
@NamedQueries({
        @NamedQuery(name = "Room.findByRoomType", query = "SELECT r FROM Room r WHERE r.roomType = :roomType")
})
public class Room extends AbstractEntity {

    @Basic(optional = false)
    @Column(nullable = false)
    private String code;

    private Integer openingHour;

    private Integer closingHour;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private Set<TimeSlot> timeSlots;

    @Basic(optional = false)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    @Basic(optional = false)
    @Column(nullable = false)
    private Integer capacity;

    @ElementCollection(targetClass = EquipmentType.class)
    @Enumerated(EnumType.STRING)
    private Set<EquipmentType> equipment;

    public Room() {
    }

    public Room(String code, RoomType roomType, Integer capacity) {
        this.code = code;
        this.roomType = roomType;
        this.capacity = capacity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Set<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(Set<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Integer getOpeningHour() {
        return openingHour;
    }

    public void setOpeningHour(Integer openingHour) {
        this.openingHour = openingHour;
    }

    public Integer getClosingHour() {
        return closingHour;
    }

    public void setClosingHour(Integer closingHour) {
        this.closingHour = closingHour;
    }

    public Set<EquipmentType> getEquipment() {
        return equipment;
    }

    public void setEquipment(Set<EquipmentType> equipment) {
        this.equipment = equipment;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
