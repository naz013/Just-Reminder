package com.backdoor.reminder.backend.form;


public class GroupForm {
	
	private String name;
	private int color = -1;
	private String uuId;

	private GroupForm(){}
	
	public GroupForm(String name, int color, String uuId) {
		this.name = name;
		this.color = color;
		this.uuId = uuId;
	}

	public String getUuId() {
		return uuId;
	}

	public int getColor() {
		return color;
	}
	
	public String getName() {
		return name;
	}
}
