package com.pidev.utils;

import com.pidev.models.BaseUser;

/**
 * Singleton Session - stores the currently logged-in user.
 */
public class UserSession {
    private static UserSession instance;
    private BaseUser user;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public BaseUser getUser() { return user; }
    public void setUser(BaseUser user) { this.user = user; }

    public void cleanUserSession() {
        user = null;
    }
}
