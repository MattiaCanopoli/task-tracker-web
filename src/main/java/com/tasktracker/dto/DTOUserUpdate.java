package com.tasktracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DTOUserUpdate {
	
	@Email(message="Invalid mail format")
	private String email;
	
	@Size(min=6,max=24,message = "Password must be between 6 and 24 characters")
	private String newPassword;
	
	@NotBlank(message = "Current password is required")
	private String currentPassword;

	public String getEmail() {
		return email;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public String getCurrentPassword() {
		return currentPassword;
	}
	
	

}
