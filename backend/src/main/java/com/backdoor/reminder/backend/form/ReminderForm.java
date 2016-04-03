package com.backdoor.reminder.backend.form;

public class ReminderForm {
	
	public enum ReminderType{
		TYPE_REMINDER ("reminder"),
	    TYPE_MONTHDAY ("day_of_month"),
	    TYPE_MONTHDAY_LAST ("day_of_month_last"),
	    TYPE_MONTHDAY_CALL ("day_of_month_call"),
	    TYPE_MONTHDAY_CALL_LAST ("day_of_month_call_last"),
	    TYPE_MONTHDAY_MESSAGE ("day_of_month_message"),
	    TYPE_MONTHDAY_MESSAGE_LAST ("day_of_month_message_last"),
	    TYPE_TIME ("time"),
	    TYPE_CALL ("call"),
	    TYPE_MAIL ("e_mail"),
	    TYPE_SHOPPING_LIST ("shopping_list"),
	    TYPE_MESSAGE ("message"),
	    TYPE_PLACES ("places_location"),
	    TYPE_LOCATION ("location"),
	    TYPE_LOCATION_OUT ("out_location"),
	    TYPE_LOCATION_OUT_CALL ("out_location_call"),
	    TYPE_LOCATION_OUT_MESSAGE ("out_location_message"),
	    TYPE_LOCATION_CALL ("location_call"),
	    TYPE_LOCATION_MESSAGE ("location_message"),
	    TYPE_WEEKDAY ("weekday"),
	    TYPE_WEEKDAY_CALL ("weekday_call"),
	    TYPE_WEEKDAY_MESSAGE ("weekday_message"),
	    TYPE_APPLICATION ("application"),
	    TYPE_APPLICATION_BROWSER ("application_browser"),
	    TYPE_SKYPE ("skype"),
	    TYPE_SKYPE_CHAT ("skype_chat"),
	    TYPE_SKYPE_VIDEO ("skype_video");
	    
	    private final String name;       

	    ReminderType(String s) {
	        name = s;
	    }

	    public boolean equalsName(String otherName) {
	        return (otherName == null) ? false : name.equals(otherName);
	    }

	    @Override
	    public String toString() {
	       return this.name;
	    }
	}

	private String summary;
	private String json;
	private ReminderType type;
	private String category;
	private long eventTime;

	private ReminderForm(){}
	
	public ReminderForm(String summary, String json, ReminderType type, String category, long eventTime) {
		this.summary = summary;
		this.json = json;
		this.type = type;
		this.category = category;
		this.eventTime = eventTime;
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
	
	public ReminderType getType() {
		return type;
	}
}
