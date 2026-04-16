package com.pidev.utils;

import com.pidev.models.BaseUser;

public class SessionManager {
    private static SessionManager instance;
    private BaseUser currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public BaseUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(BaseUser user) {
        this.currentUser = user;
    }

    public void clearSession() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getUserRole() {
        if (currentUser == null) return null;
        return currentUser.getDiscriminator();
    }
}