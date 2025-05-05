package com.tasktracker.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tasktracker.dto.DTOTask;
import com.tasktracker.model.Status;
import com.tasktracker.model.Task;
import com.tasktracker.repository.TaskRepo;

@Service
public class TaskService {

	private final TaskRepo taskRepo;
	private final StatusService statusService;

	public TaskService(TaskRepo taskRepo, StatusService statusService) {
		this.taskRepo = taskRepo;
		this.statusService = statusService;
	}

	/**
	 * retrieve all tasks from the task table in task_tracker_web DB
	 * 
	 * @return a List of every task
	 */
	public List<Task> getTasks() {
		return taskRepo.findAll();
	}

	// public List<Task> getByStatus(Status status) {
	// return taskRepo.findByStatusContains(status);
	// }

	public List<Task> getByStatusName(String statusName) {
		Status status = statusService.findStatusByName(statusName);

		List<Task> tasks = new ArrayList<>();
		if (status != null) {
			tasks = taskRepo.findByStatusId(status.getId());
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

	public void save(Task task) {
		taskRepo.save(task);
	}

	public Task createFromDTO(DTOTask dtoTask) {
		Task task = new Task();
		if ((dtoTask.getDescription()!=null && 
				!dtoTask.getDescription().isEmpty()) &&
				(dtoTask.getStatus_id()!=0) &&
				(dtoTask.getUser()!=null) && 
				!dtoTask.getUser().isEmpty()){
			task.setDescription(dtoTask.getDescription());
			task.setStatus(statusService.findStatusById(dtoTask.getStatus_id()));
			task.setUser(dtoTask.getUser());
			return task;
		} 
		return null;
	}
	
	public Task update(Task task, DTOTask dto) {
		
		String newDescr = dto.getDescription();
		int newStat=dto.getStatus_id();
		
		if (newDescr!=null) {
			task.setDescription(newDescr);
		}
		
		if (newStat>0 && newStat<4) {
			task.setStatus(statusService.findStatusById(newStat));
			
			if (newStat ==3) {
				task.setCompletedAt(Timestamp.valueOf(LocalDateTime.now()));
			}
		}
		taskRepo.save(task);
		return task;
		
	}


}
