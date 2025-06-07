package com.tasktracker.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.tasktracker.security.model.User;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotEmpty
	@Column(nullable = false)
	private String description;

	@CreationTimestamp
	@Column(nullable = false)
	private Timestamp createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Timestamp updatedAt;

	@Nullable
	@Column(nullable = true)
	private Timestamp completedAt;

	@Nullable
	@Column(nullable=true)
	private Timestamp deletedAt;

	private boolean isDeleted;

	@Transient
	private SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy hh:mm:ss");

	@NotNull
	@ManyToOne
	@JoinColumn(nullable = false, name="user_id")
	@JsonManagedReference
	private User user;

	@ManyToOne
	@JsonManagedReference
	@JoinColumn(name = "status_id")
	private Status status;

//	public Task() {
//
//	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String Description) {
		this.description = Description;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Timestamp getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Timestamp completedAt) {
		this.completedAt = completedAt;
	}

	public Timestamp getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(Timestamp deletedAt) {
		this.deletedAt = deletedAt;
	}


	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	private String getCreatedAtF() {
		return format.format(this.createdAt);
	}

	private String getUpdatedAtF() {
		return format.format(this.updatedAt);
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	@Override
	public String toString() {
		return "id=" + id + ", user=" + user +", description=" + description + ", status: " + this.getStatus().getStatusName()
				+ ", createdAt=" + this.getCreatedAtF() + ", last update: " + this.getUpdatedAtF();
	}


}
