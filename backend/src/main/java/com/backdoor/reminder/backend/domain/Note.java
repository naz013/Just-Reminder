package com.backdoor.reminder.backend.domain;

import com.backdoor.reminder.backend.form.NoteForm;
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
public class Note {

	@Id
    private long id;

	@Parent
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<Profile> profileKey;

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private String organizerUserId;

    @Index
	String text;

	@Index
	int color;

	@Index
	String uuId;

	@Index
	long dateTime;

    long reminderId;

	byte[] image;

    private Note() {}

	public Note(final long id, final String organizerUserId, NoteForm noteForm) {
		this.id = id;
		this.profileKey = Key.create(Profile.class, organizerUserId);
        this.organizerUserId = organizerUserId;
		this.uuId = noteForm.getUuId();
		if (uuId == null || uuId.matches("")) {
			this.uuId = UUID.randomUUID().toString();
		}
		this.color = noteForm.getColor();
		this.text = noteForm.getText();
        this.image = noteForm.getImage();
		this.dateTime = System.currentTimeMillis();
        this.reminderId = noteForm.getReminderId();
	}
	
	public void update(NoteForm noteForm) {
		if (noteForm == null) {
			return;
		}
		if (noteForm.getColor() != -1)
            this.color = noteForm.getColor();
        if (noteForm.getText() != null)
            this.text = noteForm.getText();
        this.image = noteForm.getImage();
		this.dateTime = System.currentTimeMillis();
        this.reminderId = noteForm.getReminderId();
	}

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Key<Profile> getProfileKey() {
		return profileKey;
	}

	// Get a String version of the key
	public String getWebsafeKey() {
		return Key.create(profileKey, Note.class, id).getString();
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
	
	public String getUuId() {
		return uuId;
	}

    public byte[] getImage() {
        return image;
    }

    public long getReminderId() {
        return reminderId;
    }

    public String getText() {
        return text;
    }
}
