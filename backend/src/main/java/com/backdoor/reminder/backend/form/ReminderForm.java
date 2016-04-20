package com.backdoor.reminder.backend.form;

public class ReminderForm {

	private String summary;
	private String json;
	private String type;
	private String category;
	private String uuId;
	private long eventTime;

	private ReminderForm(){}
	
	public ReminderForm(String summary, String json, String type, String category, long eventTime,
						String uuId) {
		this.summary = summary;
		this.json = json;
		this.type = type;
		this.category = category;
		this.eventTime = eventTime;
		this.uuId = uuId;
	}

	public String getUuId() {
		return uuId;
	}

	public String getCategory() {
		return category;
	}
	
	public long getEventTime() {
		return eventTime;
	}
	
	public String getJson() {
		return json;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public String getType() {
		return type;
	}
}
