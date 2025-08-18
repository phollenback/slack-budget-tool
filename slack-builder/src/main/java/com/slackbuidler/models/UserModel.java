package com.slackbuidler.models;

public abstract class UserModel {
    private String id;
    private String firstName;
    private String lastName;
    private String state;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    @Override
    public String toString() {
        return "UserModel [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", state=" + state + "]";
    }
    public UserModel(String id, String firstName, String lastName, String state) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.state = state;
    }
}
