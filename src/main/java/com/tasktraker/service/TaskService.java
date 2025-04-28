package com.tasktraker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tasktraker.model.Task;
import com.tasktraker.repository.TaskRepo;

@Service
public class TaskService {


	private final TaskRepo taskRepo;
	
	public TaskService (TaskRepo taskRepo) {
		this.taskRepo=taskRepo;
	}

	/**
	 * retrieve all tasks from the task table in task_tracker_web DB
	 * 
	 * @return a List of every task
	 */
	public List<Task> getTasks() {
		return taskRepo.findAll();
	}

	public List<Task> getByStatus(String status) {
		return taskRepo.findByStatusContains(status);
	}

	public Task getByID(long id) {
		Optional<Task> t = taskRepo.findById(id);

		if (t.isPresent()) {
			return t.get();
		}
		return null;
	}
}
