package cz.cvut.reservation.dto;

import cz.cvut.reservation.model.User;
import cz.cvut.reservation.util.UserType;

public class UserDto {
    private String firstName;

    private String lastName;

    private String username;

    private UserType userType;

    public UserDto() {
    }

    public UserDto(User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.username = user.getUsername();
        this.userType = user.getUserType();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                firstName + " " + lastName +
                "(" + username + ")}";
    }
}
