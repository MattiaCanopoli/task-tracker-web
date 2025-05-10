package com.tasktracker.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
	 * Retrieves all tasks that are not marked as deleted from the task table in
	 * task_tracker_web DB
	 * 
	 * @return a List of every task or an empty List if there are no tasks
	 */
	public List<Task> getTasks() {
		return taskRepo.findByisDeletedFalse();
	}

	/**
	 * Retrieves all tasks matching the specified status name.
	 * <p>
	 * The provided status name is validated. If it does not correspond to an existing
	 * status, an {@link IllegalArgumentException} is thrown.
	 * The matching {@link Status} is then retrieved from the database, and all tasks
	 * with that status are returned.
	 * </p>
	 *
	 * @param statusName the name of the status used to filter tasks
	 * @return a list of tasks matching the status; the list may be empty if no tasks match
	 * @throws IllegalArgumentException if the status name is not valid
	 */
	public List<Task> getByStatusName(String statusName)
			throws IllegalArgumentException {

		if (!statusService.isStatusValid(statusName.toUpperCase())) {

			throw new IllegalArgumentException(
					"Status \"" + statusName + "\" is not valid");

		}

		Status status = statusService.findStatusByName(statusName);

		List<Task> tasks = taskRepo.findByStatusId(status.getId());

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

		Optional<Task> t = taskRepo.findById(id);

		if (!t.isPresent()) {

			throw new NoSuchElementException(
					"Task with ID \"" + id + "\" does not exists");
		}

		return t.get();

	}

	/**
	 * Persists the provided Task into task_tracker_web DB
	 * 
	 * @param task
	 *            object to persist
	 */
	public void save(Task task) {
		taskRepo.save(task);
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
	public Task createFromDTO(DTOTask dto) {

		if (dto.getDescription() == null || dto.getDescription().isEmpty()) {
			throw new IllegalArgumentException(
					"Description cannot be null. Creation request failed.");
		}

		if (dto.getUser() == null || dto.getUser().isEmpty()) {
			throw new IllegalArgumentException(
					"User cannot be null. Creation request failed.");
		}

		if (statusService.findStatusById(dto.getStatus_id()) == null) {
			throw new IllegalArgumentException(
					"Status with ID " + dto.getStatus_id()
							+ " does not exists. Creation request failed.");
		}

		Task task = new Task();
		task.setDescription(dto.getDescription());
		task.setStatus(statusService.findStatusById(dto.getStatus_id()));
		task.setUser(dto.getUser());
		taskRepo.save(task);
		return task;
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
	public Task updateStatus(Task task, DTOTask dto)
			throws IllegalArgumentException {

		Status status = statusService.findStatusById(dto.getStatus_id());

		if (status == null) {
			throw new IllegalArgumentException("Status with ID "
					+ dto.getStatus_id()
					+ " does not exists. Status update request failed.");
		}

		task.setStatus(status);

		if (task.getStatus().getId() == 3) {
			task.setCompletedAt(Timestamp.valueOf(LocalDateTime.now()));
		}

		taskRepo.save(task);

		return task;

	}

	public Task updateDescription(Task task, DTOTask dto) {

		if (dto.getDescription() == null || dto.getDescription().isEmpty()) {
			throw new IllegalArgumentException(
					"Desciption is empty. Description update request failed");
		}

		task.setDescription(dto.getDescription());
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
	public void markAsDeleted(Task task) {
		task.setStatus(statusService.findStatusById(4));
		task.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));
		task.setDeleted(true);
		taskRepo.save(task);
	}

}
