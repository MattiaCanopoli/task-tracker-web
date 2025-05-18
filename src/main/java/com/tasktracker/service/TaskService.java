package com.tasktracker.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tasktracker.dto.DTOTask;
import com.tasktracker.model.Status;
import com.tasktracker.model.Task;
import com.tasktracker.repository.TaskRepo;
import com.tasktracker.security.model.User;

@Service
public class TaskService {

	private final TaskRepo taskRepo;
	private final StatusService statusService;

	public TaskService(TaskRepo taskRepo, StatusService statusService) {
		this.taskRepo = taskRepo;
		this.statusService = statusService;
	}

	/**
	 * Retrieves all {@link Task} entities from the task_tracker_web database,
	 * excluding those marked as deleted.
	 *
	 * @return a list of all active {@link Task} entities; the list may be empty
	 *         if no tasks are found
	 */
	public List<Task> getTasks() {
		return taskRepo.findByisDeletedFalse();
	}
	
	public List<Task> getByUserID(long userId){
		return taskRepo.findByUserId(userId);
	}

	/**
	 * Retrieves all {@link Task} entities matching the specified status name.
	 * <p>
	 * If the provided status name does not correspond to an existing status, an
	 * {@link IllegalArgumentException} is thrown.
	 * </p>
	 *
	 * @param statusName
	 *            the name of the status used to filter tasks
	 * @return a list of {@link Task} entities with the given status; the list
	 *         may be empty if no tasks match
	 * @throws IllegalArgumentException
	 *             if the provided status name is not valid
	 */
	public List<Task> getByStatusName(String statusName)
			throws IllegalArgumentException {
		//specified statusName is validated. if it's not valid, exception is thrown
		if (!statusService.isStatusValid(statusName.toUpperCase())) {

			throw new IllegalArgumentException(
					"Status \"" + statusName + "\" is not valid");

		}
		//a new Status is instantiated by retrieving it from DB
		Status status = statusService.findStatusByName(statusName);
		//a new list of task with the specified status in instantiated
		List<Task> tasks = taskRepo.findByStatusId(status.getId());
		//return the list
		return tasks;

	}

	/**
	 * Retrieves the {@link Task} entity with the specified ID.
	 * <p>
	 * If no {@link Task} with the given ID exists, a
	 * {@link NoSuchElementException} is thrown.
	 * </p>
	 *
	 * @param id
	 *            the ID of the {@link Task} to retrieve
	 * @return the {@link Task} entity with the specified ID
	 * @throws NoSuchElementException
	 *             if no {@link Task} with the provided ID exists
	 */
	public Task getByID(long id) throws NoSuchElementException {
		//instantiate a new Optional<Task> by retrieving the task with the specified ID from DB
		Optional<Task> t = taskRepo.findById(id);
		//if there is no task with the specified ID an exception is thrown
		if (!t.isPresent()) {

			throw new NoSuchElementException(
					"Task with ID \"" + id + "\" does not exist");
		}
		//return the task with the specified ID
		return t.get();

	}

	/**
	 * Saves the specified {@link Task} entity to the task_tracker_web database.
	 *
	 * @param task
	 *            the {@link Task} to be saved
	 */
	public void save(Task task) {
		taskRepo.save(task);
	}

	/**
	 * Validates the fields of the provided {@link DTOTask} and creates a new
	 * {@link Task} entity.
	 * <p>
	 * If any required field is null, empty, or invalid (e.g., invalid status
	 * ID), an {@link IllegalArgumentException} is thrown.
	 * </p>
	 * <p>
	 * Upon successful validation, a new {@link Task} is instantiated using the
	 * DTO's values and persisted to the task_tracker_web database.
	 * </p>
	 *
	 * @param dto
	 *            the {@link DTOTask} containing task data to be validated and
	 *            persisted
	 * @return the newly created {@link Task} entity
	 * @throws IllegalArgumentException
	 *             if the description, user, or status ID is invalid
	 */
	public Task createFromDTO(DTOTask dto, User user) throws IllegalArgumentException {

		//validate DTO's description field. if it's not valid, exception is thrown
		if (dto.getDescription() == null || dto.getDescription().isEmpty()) {
			throw new IllegalArgumentException(
					"Description cannot be null. Creation request failed.");
		}
		
		//validate DTO's status_id field. if it's not valid, exception is thrown
		if (statusService.findStatusById(dto.getStatusID()) == null) {
			throw new IllegalArgumentException(
					"Status with ID " + dto.getStatusID()
							+ " does not exists. Creation request failed.");
		}
		//instantiate a new task using DTO's values
		Task task = new Task();
		task.setUser(user);
		task.setDescription(dto.getDescription());
		task.setStatus(statusService.findStatusById(dto.getStatusID()));
//		task.setUser(dto.getUser());
		//saves the task in the DB and return it
		taskRepo.save(task);
		return task;
	}

	/**
	 * Updates the status of the provided {@link Task} based on the data from the given {@link DTOTask}.
	 * <p>
	 * If the provided status ID does not correspond to an existing {@link Status},
	 * an {@link IllegalArgumentException} is thrown.
	 * </p>
	 * <p>
	 * If the new status ID is {@code 3} (representing "DONE"), the {@code completedAt}
	 * field is also set to the current {@link Timestamp}.
	 * </p>
	 * <p>
	 * The updated {@link Task} is then persisted to the task_tracker_web database.
	 * </p>
	 *
	 * @param task the {@link Task} entity to be updated
	 * @param dto  the {@link DTOTask} containing the new status ID
	 * @return the updated {@link Task} entity
	 * @throws IllegalArgumentException if the provided status ID is invalid
	 */
	public Task updateStatus(Task task, DTOTask dto)
			throws IllegalArgumentException {
		//validate DTO's status_id field. if it's not valid, exception is thrown
		Status status = statusService.findStatusById(dto.getStatusID());

		if (status == null) {
			throw new IllegalArgumentException("Status with ID "
					+ dto.getStatusID()
					+ " does not exists. Status update request failed.");
		}
		//status field of the provided task is updated with the DTO's value 
		task.setStatus(status);
		//if the new status is "COMPLETED", completedAt field is set to current timestamp
		if (task.getStatus().getId() == 3) {
			task.setCompletedAt(Timestamp.valueOf(LocalDateTime.now()));
		}
		//saves the task in the DB and return it
		taskRepo.save(task);

		return task;

	}

	/**
	 * Updates the description of the provided {@link Task} based on the data from the given {@link DTOTask}.
	 * <p>
	 * If the provided {@code description} is null or empty, an {@link IllegalArgumentException} is thrown.
	 * </p>
	 * <p>
	 * After validation, the description of the provided {@link Task} is updated, and the task is then persisted
	 * to the task_tracker_web database.
	 * </p>
	 *
	 * @param task the {@link Task} entity to be updated
	 * @param dto  the {@link DTOTask} containing the new {@code description}
	 * @return the updated {@link Task} entity
	 * @throws IllegalArgumentException if the provided {@code description} is null or empty
	 */
	public Task updateDescription(Task task, DTOTask dto) throws IllegalArgumentException {
		//validate DTO's description field. if it's not valid, exception is thrown
		if (dto.getDescription() == null || dto.getDescription().isEmpty()) {
			throw new IllegalArgumentException(
					"Desciption is empty. Description update request failed");
		}
		//description field of the provided task is updated with the DTO's value 
		task.setDescription(dto.getDescription());
		//saves the task in the DB and return it
		taskRepo.save(task);
		return task;
	}

	/**
	 * Marks the given {@link Task} as {@code DELETED} by updating its {@link Status} to {@code 4} ("DELETED"),
	 * setting the {@code deletedAt} field to the current {@link Timestamp}, and the {@code isDeleted} field to {@code true}.
	 * <p>
	 * After performing these updates, the {@link Task} is persisted to the task_tracker_web database.
	 * </p>
	 * 
	 * @param task the {@link Task} to be marked as {@code DELETED}
	 */
	public void markAsDeleted(Task task) {
		//sets status of the provided task to DELETED
		task.setStatus(statusService.findStatusById(4));
		//sets deletedAt field to current timestamp
		task.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));
		//sets isDeleted field to true
		task.setDeleted(true);
		//saves the task in the DB
		taskRepo.save(task);
	}

}
