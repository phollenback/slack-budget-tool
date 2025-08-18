package com.slackbuidler.models;

import java.util.Arrays;

public class SuperUserModel extends UserModel {
    private String[] permissions;
    public SuperUserModel(String id, String firstName, String lastName, String state, String[] permissions) {
        super(id, firstName, lastName, state);
        this.permissions = permissions;
    }
    public String[] getPermissions() {
        return permissions;
    }
    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "SuperUserModel [permissions=" + Arrays.toString(permissions) + "]";
    }
}
