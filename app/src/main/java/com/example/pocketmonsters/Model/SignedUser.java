package com.example.pocketmonsters.Model;

import androidx.annotation.NonNull;

public class SignedUser extends User {

    private String sessionId;

    SignedUser() {
        super();
    }

    public SignedUser(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("sessionId: %s, username: %s; life points: %s; exp points: %s", sessionId, getUsername(), getLifePoints(), getExpPoints());
    }
}

