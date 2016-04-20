package com.backdoor.reminder.backend.domain;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Entity
@Cache
public class Profile {
	String displayName;

	String mainEmail;

	String password;

	List<Long> connectedUsers = new ArrayList<>(0);

	@Id
	String userId;

	/**
	 * Just making the default constructor private.
	 */
	private Profile() {}

    public Profile (String userId, String displayName, String mainEmail, String password) {
    	this.userId = userId;
    	this.displayName = displayName;
    	this.mainEmail = mainEmail;
    	this.password = password;
    }

    public Profile (String userId, String displayName, String mainEmail) {
        this.userId = userId;
        this.displayName = displayName;
        this.mainEmail = mainEmail;
    }
    
	public String getDisplayName() {
		return displayName;
	}

	public String getMainEmail() {
		return mainEmail;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}

	public List<Long> getConnectedUsers() {
		return connectedUsers;
	}

	public void addToConferenceKeysToAttend(long userId) {
		connectedUsers.add(userId);
    }

    public void unregisterFromConference(long userId) {
        if (connectedUsers.contains(userId)) {
			connectedUsers.remove(userId);
        } else {
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }
    }

	public void update(String displayName, String password) {
		if (displayName != null) {
			this.displayName = displayName;
		}
		if (password != null && password.length() > 8) {
			this.password = password;
		}
	}

}
