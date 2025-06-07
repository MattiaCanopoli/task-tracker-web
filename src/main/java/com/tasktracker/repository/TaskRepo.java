package com.tasktracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasktracker.model.Status;
import com.tasktracker.model.Task;

public interface TaskRepo extends JpaRepository<Task, Long> {

	public List<Task> findByStatusId(int statusID);

	public List<Task> findByisDeletedFalse();

	public List<Task> findByUserId(Long userID);

	public List<Task> findByUserIdAndStatus(Long userID, Status status);
}
