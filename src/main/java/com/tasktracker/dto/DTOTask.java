package com.tasktracker.dto;

import java.sql.Timestamp;

public class DTOTask {

	private long id;
	private String description;
	private int statusID;
	private long userID;
	private Timestamp completedAt;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getStatusID() {
		return statusID;
	}

	public void setStatus_id(int status_id) {
		this.statusID = status_id;
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Timestamp getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Timestamp completedAt) {
		this.completedAt = completedAt;
	}

	@Override
	public String toString() {
		return "DTOTask [id=" + id + ", description=" + description + ", userID="
				+ userID + ", status_id=" + statusID + "]";
	}

}
