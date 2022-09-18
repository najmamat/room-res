package cz.cvut.user.service;

import cz.cvut.user.exception.UserAlreadyExistsException;
import cz.cvut.user.model.User;

public interface IUserService {
    User registerUser(User user) throws UserAlreadyExistsException;
    boolean login(String username, String password);
    void logout();
    boolean isLoggedInUserAdmin();
    boolean exists(String username);
    User getLoggedInUser();
}
