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
	 * Retrieves all tasks from the task table in task_tracker_web DB
	 * 
	 * @return a List of every task
	 * @return an empty List if there are no tasks
	 */
	public List<Task> getTasks() {
		return taskRepo.findAll();
	}

	/**
	 * statusName argument is used to retrieve the corresponding Status object and then the corresponding id.
	 * A new ArrayList of Task is created and filled with tasks having the corresponding statusId.
	 * If there are no task with such statusId the list remains empty.
	 * Returns the list
	 * @param statusName a String corresponding to the status to filter
	 * @return a list of tasks. an empty list can be returned.
	 */
	public List<Task> getByStatusName(String statusName) {
		Status status = statusService.findStatusByName(statusName);

		List<Task> tasks = new ArrayList<>();
		if (status != null) {
			tasks = taskRepo.findByStatusId(status.getId());
		}

		return tasks;

	}

	/**
	 * Retrieve the task with the provided ID.
	 * Return such task if exists, else return null
	 * @param id long of the task to find
	 * @return found task
	 * @return null if nothing found
	 */
	public Task getByID(long id) {
		Optional<Task> t = taskRepo.findById(id);

		if (t.isPresent()) {
			return t.get();
		}
		return null;
	}

	/**
	 * Persists the provided Task into task_tracker_web DB
	 * @param task object to persist
	 */
	public void save(Task task) {
		taskRepo.save(task);
	}

	/**
	 * Checks if DTO fields (description, statusID, user) are present.
	 * If any of the fields is missing, returns null.
	 * Creates a new Task.
	 * Task fields  are filled with the corresponding ones in the DTO.
	 * ID and date fields (createdAt, updatedAt) are automatically generated.
	 * Persists newly created Task instance to the DB
	 * @param dtoTask an object representing a simplified version of a Task Object. Stores the values to be passed to the Task object.
	 * @return newly created task
	 * @return null if any of the field in the DTO is missing
	 */
	public Task createFromDTO(DTOTask dtoTask) {
		if ((dtoTask.getDescription()!=null && 
				!dtoTask.getDescription().isEmpty()) &&
				(dtoTask.getStatus_id()!=0) &&
				(dtoTask.getUser()!=null) && 
				!dtoTask.getUser().isEmpty()){
			Task task = new Task();
			task.setDescription(dtoTask.getDescription());
			task.setStatus(statusService.findStatusById(dtoTask.getStatus_id()));
			task.setUser(dtoTask.getUser());
			taskRepo.save(task);
			return task;
		} 
		return null;
	}
	
	/**
	 * Gets description and Status from the DTO.
	 * If any of the field is present, updates the provided task with the new values.
	 * If DTO's statusID is 3 ("done"), completedAt value is updated to the current timestamp. 
	 * UpdatedAt value if automatically updated to the current timestamp.
	 * Persists the new values in the DB. 
	 * @param task the task object to be updated
	 * @param dto an object representing a simplified version of a Task Object. Stores the values to be passed to the Task object.
	 * @return updated task.
	 * @return same task if there are no updates.
	 */
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
