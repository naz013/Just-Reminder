package com.backdoor.reminder.backend.domain;

import com.backdoor.reminder.backend.form.GroupForm;
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
public class Group {
	
	@Id
    private long id;
	
	@Parent
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<Profile> profileKey;
	
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private String organizerUserId;
	
    @Index
	String name;

	@Index
	int color;
	
	@Index
	String uuId;
	
	@Index
	long dateTime;

    private Group() {}
	
	public Group(final long id, final String organizerUserId, GroupForm groupForm) {
		this.id = id;
		this.profileKey = Key.create(Profile.class, organizerUserId);
        this.organizerUserId = organizerUserId;
		this.uuId = UUID.randomUUID().toString();
		this.color = groupForm.getColor();
		this.name = groupForm.getName();
		this.dateTime = System.currentTimeMillis();
	}
	
	public void update(GroupForm groupForm) {
		if (groupForm == null) {
			return;
		}
		if (groupForm.getColor() != -1)
            this.color = groupForm.getColor();
        if (groupForm.getName() != null)
            this.name = groupForm.getName();
		this.dateTime = System.currentTimeMillis();
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Key<Profile> getProfileKey() {
		return profileKey;
	}

	// Get a String version of the key
	public String getWebsafeKey() {
		return Key.create(profileKey, Group.class, id).getString();
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public String getOrganizerUserId() {
		return organizerUserId;
	}
	
	public int getColor() {
		return color;
	}
	
	public long getDateTime() {
		return dateTime;
	}
	
	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUuId() {
		return uuId;
	}
}
