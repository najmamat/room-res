package cz.cvut.reservation.util;

public enum EquipmentType {
    DATA_PROJECTOR("Data projector"),
    TV("Television"),
    BOARD("Board"),
    TOUCH_SCREEN("Touch screen"),
    GOOGLE_MEET_HW("Google meet hw");

    private final String type;

    EquipmentType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
