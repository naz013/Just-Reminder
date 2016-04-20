package com.backdoor.reminder.backend.domain;

import com.backdoor.reminder.backend.form.ReminderForm;
import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

import java.util.UUID;

@Entity
@Cache
public class Reminder {
	
	@Id
    private long id;
	
	@Parent
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<Profile> profileKey;
	
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private String organizerUserId;
	
    @Index
	String summary;
    
	String json;
	
	@Index
	String type;
	
	@Index
	String uuId;
	
	@Index
	String category;
	
	@Index
	long eventTime;
	
	@Index
	int dbStatus = 0;
	
	@Index
	int dbList = 0;
	
	int delay = 0;
	
	int notificationStatus = 0;
	
	int reminderStatus = 0;
	
	int locationStatus = 0;

    private Reminder() {}
	
	public Reminder(final long id, final String organizerUserId, ReminderForm reminderForm) {
		this.id = id;
		this.profileKey = Key.create(Profile.class, organizerUserId);
        this.organizerUserId = organizerUserId;
		this.summary = reminderForm.getSummary();
		this.json = reminderForm.getJson();
		this.type = reminderForm.getType();
        this.uuId = reminderForm.getUuId();
		if (uuId == null || uuId.matches("")) {
			this.uuId = UUID.randomUUID().toString();
		}
		this.category = reminderForm.getCategory();
		this.eventTime = reminderForm.getEventTime();
		this.dbStatus = 0;
		this.dbList = 0;
		this.delay = 0;
		this.notificationStatus = 0;
		this.reminderStatus = 0;
		this.locationStatus = 0;
	}
	
	public void update(ReminderForm reminderForm) {
		if (reminderForm == null) {
			return;
		}
		
		this.summary = reminderForm.getSummary();
		this.json = reminderForm.getJson();
		this.type = reminderForm.getType();
		this.category = reminderForm.getCategory();
		this.eventTime = reminderForm.getEventTime();
		this.dbStatus = 0;
		this.dbList = 0;
		this.delay = 0;
		this.notificationStatus = 0;
		this.reminderStatus = 0;
		this.locationStatus = 0;
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Key<Profile> getProfileKey() {
		return profileKey;
	}

	// Get a String version of the key
	public String getWebsafeKey() {
		return Key.create(profileKey, Reminder.class, id).getString();
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public String getOrganizerUserId() {
		return organizerUserId;
	}

    public long getId() {
        return id;
    }

    public String getCategory() {
		return category;
	}
	
	public int getDbList() {
		return dbList;
	}
	
	public int getDbStatus() {
		return dbStatus;
	}
	
	public int getDelay() {
		return delay;
	}
	
	public long getEventTime() {
		return eventTime;
	}
	
	public String getJson() {
		return json;
	}
	
	public int getLocationStatus() {
		return locationStatus;
	}
	
	public int getNotificationStatus() {
		return notificationStatus;
	}
	
	public int getReminderStatus() {
		return reminderStatus;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public String getType() {
		return type;
	}
	
	public String getUuId() {
		return uuId;
	}
}
