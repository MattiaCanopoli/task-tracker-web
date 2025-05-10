package com.tasktracker.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tasktracker.dto.DTOTask;
import com.tasktracker.model.Status;
import com.tasktracker.model.Task;
import com.tasktracker.repository.TaskRepo;

@Service
public class TaskService {

	private static final Logger logger = LoggerFactory
			.getLogger(TaskService.class);
	private final TaskRepo taskRepo;
	private final StatusService statusService;

	public TaskService(TaskRepo taskRepo, StatusService statusService) {
		this.taskRepo = taskRepo;
		this.statusService = statusService;
	}

	/**
	 * Retrieves all tasks from the task table in task_tracker_web DB
	 * 
	 * @return a List of every task or an empty List if there are no tasks
	 */
	public List<Task> getTasks() {
		logger.info("Attempting to retrieve all tasks");
		return taskRepo.findAll();
	}

	/**
	 * statusName argument is used to retrieve the corresponding Status object
	 * and then the corresponding id. A new ArrayList of Task is created and
	 * filled with tasks having the corresponding statusId. If there are no task
	 * with such statusId the list remains empty. Returns the list
	 * 
	 * @param statusName
	 *            a String corresponding to the status to filter
	 * @return a list of tasks. an empty list can be returned.
	 */
	public List<Task> getByStatusName(String statusName)
			throws IllegalArgumentException {
		logger.info("Attempting to retrieve tasks with status \"{}\"", statusName);
		if (!statusService.isStatusValid(statusName.toUpperCase())) {
			logger.error("Status \"{}\" is not valid",statusName);
			throw new IllegalArgumentException ("Status \"" + statusName + "\" is not valid");
			
		}

		Status status = statusService.findStatusByName(statusName);

		List<Task> tasks = new ArrayList<>();
		if (status != null) {
			tasks = taskRepo.findByStatusId(status.getId());
		}
		logger.info("Retrieved {} tasks with status \"{}\"",tasks.size(),statusName);
		return tasks;

	}

	/**
	 * Retrieve the task with the provided ID. Returns the task if exists, else
	 * return null
	 * 
	 * @param id
	 *            long of the task to find
	 * @return found task or null if nothing found
	 */
	public Task getByID(long id) {
		logger.info("Attempting to retrieve task with id {}", id);
		Optional<Task> t = taskRepo.findById(id);

		if (t.isPresent()) {
			logger.info("Task with id {} found. Task description: {}", id,
					t.get().getDescription());
			return t.get();
		}
		logger.warn("Task with id {} not found", id);
		return null;
	}

	/**
	 * Persists the provided Task into task_tracker_web DB
	 * 
	 * @param task
	 *            object to persist
	 */
	public void save(Task task) {
		taskRepo.save(task);
		logger.info("Task with ID: {} has been saved", task.getId());
	}

	// TODO: implement status validation
	// TODO: implement DTO validation
	/**
	 * Checks if DTO fields (description, statusID, user) are present. If any of
	 * the fields is missing, returns null. Creates a new Task. Task fields are
	 * filled with the corresponding ones in the DTO. ID and date fields
	 * (createdAt, updatedAt) are automatically generated. Persists newly
	 * created Task instance to the DB
	 * 
	 * @param dtoTask
	 *            an object representing a simplified version of a Task Object.
	 *            Stores the values to be passed to the Task object.
	 * @return newly created task or null if any required field in the DTO is
	 *         missing
	 */
	public Task createFromDTO(DTOTask dtoTask) {
		logger.info("Creating Task from DTO: {}", dtoTask);
		if ((dtoTask.getDescription() != null
				&& !dtoTask.getDescription().isEmpty())
				&& (dtoTask.getStatus_id() != 0) && (dtoTask.getUser() != null)
				&& !dtoTask.getUser().isEmpty()) {
			Task task = new Task();
			task.setDescription(dtoTask.getDescription());
			task.setStatus(
					statusService.findStatusById(dtoTask.getStatus_id()));
			task.setUser(dtoTask.getUser());
			taskRepo.save(task);
			logger.info("Task successfully created with ID: {}", task.getId());
			return task;
		}
		logger.error(
				"Failed to create Task - missing required fields in DTO: {}",
				dtoTask);
		return null;
	}

	/**
	 * Gets description and Status from the DTO. If any of the field is present,
	 * updates the provided task with the new values. If DTO's statusID is 3
	 * ("done"), completedAt value is updated to the current timestamp.
	 * UpdatedAt value if automatically updated to the current timestamp.
	 * Persists the new values in the DB.
	 * 
	 * @param task
	 *            the task object to be updated
	 * @param dto
	 *            an object representing a simplified version of a Task Object.
	 *            Stores the values to be passed to the Task object.
	 * @return updated task or same task if there are no updates.
	 */
	public Task update(Task task, DTOTask dto) {

		String newDescr = dto.getDescription();
		int newStat = dto.getStatus_id();

		boolean hasUpdated = false;

		if (newDescr != null) {
			task.setDescription(newDescr);
			logger.info("Description of task {} has been updated to \"{}\"",
					task.getId(), task.getDescription());
			hasUpdated = true;
		} else {
			logger.warn("Description is empty");
		}

		if (newStat > 0 && newStat < 4) {
			task.setStatus(statusService.findStatusById(newStat));
			logger.info("Status of task {} has been marked as \"{}\"",
					task.getId(), task.getStatus().getStatusName());
			if (newStat == 3) {
				task.setCompletedAt(Timestamp.valueOf(LocalDateTime.now()));
			}
		} else {
			logger.warn("Status with id {} is not valid", dto.getStatus_id());
		}
		taskRepo.save(task);
		return task;

	}

	/**
	 * Sets Status of the provided task to DELETED (id: 4) and deletedAt value
	 * is updated to current timestamp. New values are persisted to DB.
	 * 
	 * @param task
	 *            task instance to be deleted
	 * @return a confirmation message
	 */
	public String markAsDeleted(Task task) {
		task.setStatus(statusService.findStatusById(4));
		task.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));
		taskRepo.save(task);
		logger.info("Task with id {} has beed marked as \"DELETED\"");
		return "Task with ID " + task.getId()
				+ " has been marked as \"DELETED\"";
	}

}
