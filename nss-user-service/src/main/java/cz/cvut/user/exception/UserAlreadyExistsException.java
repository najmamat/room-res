package cz.cvut.user.exception;

public class UserAlreadyExistsException extends EarException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
