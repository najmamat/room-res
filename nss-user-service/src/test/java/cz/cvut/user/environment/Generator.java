package cz.cvut.user.environment;

import cz.cvut.user.model.User;

import java.util.*;


public class Generator {
    private static final Random RAND = new Random();

    public static int randomInt() {
        return RAND.nextInt();
    }

    public static boolean randomBoolean() {
        return RAND.nextBoolean();
    }

    public static User generateUser() {
        final User user = new User("FirstName" + randomInt(),
                "LastName" + randomInt(),
                "username" + randomInt() + "@kbss.felk.cvut.cz",
                Integer.toString(randomInt()));
        return user;
    }
}
