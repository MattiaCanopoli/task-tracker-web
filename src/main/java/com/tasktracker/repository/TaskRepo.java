package com.tasktracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasktracker.model.Task;

public interface TaskRepo extends JpaRepository<Task, Long> {

	public List<Task> findByStatusId(int statusID);
}
