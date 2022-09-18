package cz.cvut.reservation.util;

public enum RoomType {
    PRIMARY("PRIMARY"), SECONDARY("SECONDARY");

    private final String name;

    RoomType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
