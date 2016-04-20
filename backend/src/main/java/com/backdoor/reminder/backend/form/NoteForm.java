package com.backdoor.reminder.backend.form;


public class NoteForm {

	private String text;
	private int color = -1;
	private String uuId;
	private byte[] image;
    private long reminderId;

	private NoteForm(){}

    public NoteForm(String text, int color, String uuId, byte[] image, long reminderId) {
        this.text = text;
        this.color = color;
        this.uuId = uuId;
        this.image = image;
        this.reminderId = reminderId;
    }

    public long getReminderId() {
        return reminderId;
    }

    public String getText() {
        return text;
    }

    public int getColor() {
        return color;
    }

    public String getUuId() {
        return uuId;
    }

    public byte[] getImage() {
        return image;
    }
}
