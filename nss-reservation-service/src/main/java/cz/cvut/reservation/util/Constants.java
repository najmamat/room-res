package cz.cvut.reservation.util;


import java.time.LocalTime;
import java.time.Month;
import java.util.Random;

public final class Constants {
    private Constants() {
        throw new AssertionError();
    }

    public static final Random RANDOM = new Random();
    public static final Integer TIME_SLOT_LENGTH = 30;
    public static final LocalTime DAY_START = LocalTime.of(6, 0);
    public static final LocalTime DAY_END = LocalTime.of(22, 0);

    public static final Month TEST_MONTH = Month.NOVEMBER;

    public static final String MAIN_KAFKA_TOPIC = "NSS_TEST_TOPIC";
    public static final String REPLY_TOPIC = "NSS_REPLY_TOPIC";
    public static final String KAFKA_BOOTSTRAP_ADDRESS = "localhost:9092";
    public static final String KAFKA_GROUP_ID = "test-group";

    public static final String EVENT_CACHE = "event";
    public static final Integer IDLE_MAP_LIMIT = 180;
    public static final Integer MAX_TTL_MAP_LIMIT = 360;

    public static final String USER_WRAPPER_ATTR = "UserWrapper";
}
