package com.todo.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TodoItem {
	private int id;
	private int isCompleted;
	private int essential;
    private String category;
    private String title;
    private String description;
    private String dueDate;
    private String state;
    private String currentDate;

    public TodoItem() {}
    
	public TodoItem(String category, String title, String description, String dueDate) {
        this.category = category;
    	this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        
        try { 
        	Date today = new Date();
        	Date due = dateFormat.parse(this.dueDate); 
        	
        	if (today.before(due)) this.state = "expired";
        	if (today.equals(due)) this.state = "D-day";
        } 
        catch (ParseException e) { e.printStackTrace(); }
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
        this.currentDate = timeFormat.format(new Date());
    }
    
    public TodoItem(
		int id, int isCompleted, int essential,
		String category, String title, String description, String dueDate, String state, String currentDate 
    ) {
        this.id = id;
        this.isCompleted = isCompleted;
        this.essential = essential;
    	this.category = category;
    	this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.state = state;
        this.currentDate = currentDate;
    }

    public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public int getIsCompleted() { return isCompleted; }
	public void setIsCompleted(int isCompleted) { this.isCompleted = isCompleted; }

	public int getEssential() { return essential; }
	public void setEssential(int essential) { this.essential = essential; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }

	public String getDueDate() { return dueDate; }
	public void setDueDate(String dueDate) { this.dueDate = dueDate; }

	public String getState() { return state; }
	public void setState(String state) { this.state = state; }

    public String getCurrentDate() { return currentDate; }
	public void setCurrentDate(String currentDate) { this.currentDate = currentDate; }

	public String getField(int index) {
		if (index == 0) return category;
		else if (index == 1) return title;
		else if (index == 2) return description;
		else if (index == 3) return dueDate;
		
		return null;
	}
	
	public void updateState() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        
    	String today = dateFormat.format(new Date());
    	
    	if (today.equals(this.dueDate)) this.state = "D-day";
    	else if (today.compareTo(this.dueDate) > 0) this.state = "expired";
    	else this.state = "";
	}
    
	@Override
	public String toString() {
		return String.format("%s %s %s %s %s %s", id, isCompleted, category, title, description, dueDate);
	}
	
	public String toString(int[] width) {
		return String.format(" %2d | %-3s | %-3"
			 + "s | %-" + width[0]
			 + "s | %-" + width[1]
			 + "s | %-" + width[2]
			 + "s | %-10s | %-7s | %s", 
			id, isCompleted == 1 ? " V": " ", essential == 1 ? " V": " ", category, title, description, dueDate, state, currentDate
		);
	}
	
	// Useless for now
    public String toSaveString() {
    	return String.format("%s##%s##%s##%s##%s\n", category, title, description, dueDate, currentDate);
    }
}
