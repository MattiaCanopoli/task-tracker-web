package com.tasktraker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tasktraker.model.Task;
import com.tasktraker.repository.TaskRepo;

@Service
public class TaskService {
	
	@Autowired
	private TaskRepo taskRepo;
	
	/**
	 * retrieve all tasks from the task table in task_tracker_web DB
	 * @return a List of every task
	 */
	public List<Task> getTasks() {
		return taskRepo.findAll();
	}

}
