package cz.cvut.room.util;

import java.util.Set;

public class RoomRequirements {
    private final Integer capacityRequired;
    private final Set<EquipmentType> equipmentRequired;

    public RoomRequirements(Integer capacityRequired, Set<EquipmentType> equipmentRequired) {
        this.capacityRequired = capacityRequired;
        this.equipmentRequired = equipmentRequired;
    }

    public Integer getCapacityRequired() {
        return capacityRequired;
    }

    public Set<EquipmentType> getEquipmentRequired() {
        return equipmentRequired;
    }
}
