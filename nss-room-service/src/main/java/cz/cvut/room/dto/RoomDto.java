
package cz.cvut.room.dto;

import cz.cvut.room.model.Room;
import cz.cvut.room.util.EquipmentType;
import cz.cvut.room.util.RoomType;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

public class RoomDto extends AbstractDto {

    @Getter @Setter
    private Integer id;

    private String code;

    private Integer openingHour;

    private Integer closingHour;

    private RoomType roomType;

    private Integer capacity;

    private Set<EquipmentType> equipment;

    public RoomDto() {
    }

    public RoomDto(Room r) {
        super(r.getId());
        this.code = r.getCode();
        this.roomType = r.getRoomType();
        this.capacity = r.getCapacity();
        this.openingHour = r.getOpeningHour();
        this.closingHour = r.getClosingHour();
        this.equipment = r.getEquipment();
        this.id = r.getId();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
