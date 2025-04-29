package com.tasktraker.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tasktraker.model.Status;
import com.tasktraker.model.Task;
import com.tasktraker.repository.TaskRepo;

@Service
public class TaskService {


	private final TaskRepo taskRepo;
	private final StatusService statusService;

	
	public TaskService (TaskRepo taskRepo, StatusService statusService) {
		this.taskRepo=taskRepo;
		this.statusService=statusService;
	}

	/**
	 * retrieve all tasks from the task table in task_tracker_web DB
	 * 
	 * @return a List of every task
	 */
	public List<Task> getTasks() {
		return taskRepo.findAll();
	}

//	public List<Task> getByStatus(Status status) {
//		return taskRepo.findByStatusContains(status);
//	}
	
	public List<Task> getByStatusName(String statusName){
		Status status=statusService.findStatusByName(statusName);
		
		List<Task> tasks=new ArrayList<>();
		if (status!=null) {
			tasks=taskRepo.findByStatusId(status.getId());
		}
		
		return tasks;
		
	}

	public Task getByID(long id) {
		Optional<Task> t = taskRepo.findById(id);

		if (t.isPresent()) {
			return t.get();
		}
		return null;
	}
	
	public void save (Task task) {
		taskRepo.save(task);
	}
}
