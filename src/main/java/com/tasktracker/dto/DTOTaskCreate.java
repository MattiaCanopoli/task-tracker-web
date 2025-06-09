package com.tasktracker.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class DTOTaskCreate {

	@NotNull(message= "description cannot be null")
	@NotEmpty(message= "description cannot be empty")
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	

}
