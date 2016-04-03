package com.backdoor.reminder.backend.form;


public class GroupForm {
	
	private String name;
	private int color = -1;

	private GroupForm(){}
	
	public GroupForm(String name, int color) {
		this.name = name;
		this.color = color;
	}
	
	public int getColor() {
		return color;
	}
	
	public String getName() {
		return name;
	}
}
