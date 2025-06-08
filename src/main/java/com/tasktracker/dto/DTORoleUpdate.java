package com.tasktracker.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class DTORoleUpdate {
	
	@NotEmpty (message = "Role name cannot be empty")
	@NotNull(message = "Role name cannot be null")
	private String role;

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	
}
