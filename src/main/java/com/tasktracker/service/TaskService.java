package com.tasktracker.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tasktracker.dto.DTOTaskCreate;
import com.tasktracker.dto.DTOTaskUpdate;
import com.tasktracker.exception.StatusNotFoundException;
import com.tasktracker.model.Status;
import com.tasktracker.model.Task;
import com.tasktracker.repository.TaskRepo;
import com.tasktracker.security.model.User;

/**
 * Service layer responsible for business logic and data operations
 * related to {@link Task} entities.
 * <p>
 * Provides methods to retrieve, create, update, and logically delete tasks,
 * interacting with {@link TaskRepo} for database operations and
 * {@link StatusService} for status validation and retrieval.
 * </p>
 * <p>
 * This service is managed by Spring and instantiated via constructor-based
 * dependency injection.
 * </p>
 */
@Service
public class TaskService {

	private final TaskRepo taskRepo;
	private final StatusService statusService;

	/**
	 * Constructs a new {@code TaskService} with required dependencies.
	 * <p>
	 * This constructor is invoked by Spring's dependency injection container,
	 * providing the repository and status service.
	 * </p>
	 * 
	 * @param taskRepo      the repository for {@link Task} entities; must not be null
	 * @param statusService the service to manage {@link Status} entities; must not be null
	 */
	public TaskService(TaskRepo taskRepo, StatusService statusService) {
		this.taskRepo = taskRepo;
		this.statusService = statusService;
	}

	//READ
	
	/**
	 * Retrieves all {@link Task} entities from the database that are not marked
	 * as deleted.
	 * 
	 * @return a list of all active (not deleted) tasks; never {@code null}, but
	 *         can be empty if no tasks are found
	 */
	public List<Task> getTasks() {
		return taskRepo.findByisDeletedFalse();
	}

	/**
	 * Retrieves all {@link Task} entities created by the user with the specified
	 * ID.
	 * 
	 * @param userId the ID of the user whose tasks to retrieve
	 * @return a list of tasks belonging to the user; may be empty if none found
	 */
	public List<Task> getByUserID(long userId) {
		return taskRepo.findByUserId(userId);
	}

	/**
	 * Retrieves all {@link Task} entities with the specified status name.
	 * 
	 * @param statusName the name of the status to filter tasks by
	 * @return a list of tasks matching the specified status; may be empty
	 * @throws IllegalArgumentException if the status name is invalid or does not
	 *                                  exist
	 */
	public List<Task> getByStatusName(String statusName)
			throws IllegalArgumentException {
		// specified statusName is validated. if it's not valid, exception is
		// thrown
		if (!statusService.isStatusValid(statusName.toUpperCase())) {

			throw new IllegalArgumentException(
					"Status \"" + statusName + "\" is not valid");

		}
		// a new Status is instantiated by retrieving it from DB
		Status status = statusService.findStatusByName(statusName);
		// a new list of task with the specified status in instantiated
		List<Task> tasks = taskRepo.findByStatusId(status.getId());
		// return the list
		return tasks;

	}

	/**
	 * Retrieves all {@link Task} entities created by a specific user and
	 * filtered by status name.
	 * 
	 * @param userId     the ID of the user who created the tasks
	 * @param statusName the status name to filter tasks by
	 * @return a list of tasks for the user with the given status; may be empty
	 * @throws IllegalArgumentException if the status name is invalid or does not
	 *                                  exist
	 */
	public List<Task> getByUserAndStatus(long userId, String statusName)
			throws IllegalArgumentException {

		// specified statusName is validated. if it's not valid, exception is
		// thrown
		if (!statusService.isStatusValid(statusName.toUpperCase())) {

			throw new IllegalArgumentException(
					"Status \"" + statusName + "\" is not valid");

		}
		// a new Status is instantiated by retrieving it from DB
		Status status = statusService.findStatusByName(statusName);
		// a new list of task with the specified status in instantiated
		List<Task> tasks = taskRepo.findByUserIdAndStatus(userId, status);
		// return the list
		return tasks;

	}

	/**
	 * Retrieves a {@link Task} entity by its ID.
	 * 
	 * @param id the ID of the task to retrieve
	 * @return the task with the specified ID
	 * @throws NoSuchElementException if no task with the given ID exists
	 */

	public Task getByID(long id) throws NoSuchElementException {
		// instantiate a new Optional<Task> by retrieving the task with the
		// specified ID from DB
		Optional<Task> t = taskRepo.findById(id);
		// if there is no task with the specified ID an exception is thrown
		if (!t.isPresent()) {

			throw new NoSuchElementException(
					"Task with ID \"" + id + "\" does not exist");
		}
		// return the task with the specified ID
		return t.get();

	}

	//CREATE
	
	/**
	 * Persists the given {@link Task} entity to the database.
	 * 
	 * @param task the task to save; must not be null
	 */
	public void save(Task task) {
		taskRepo.save(task);
	}

	/**
	 * Validates and creates a new {@link Task} entity based on the provided DTO
	 * and authenticated user.
	 * <p>
	 * The method verifies that description is not null/empty and status ID exists.
	 * Upon validation, it creates and persists the new task.
	 * </p>
	 * 
	 * @param dto  the DTO containing the task data to create
	 * @param user the {@link User} who owns the task
	 * @return the newly created {@link Task} entity
	 * @throws IllegalArgumentException if description is null/empty
	 */
	public Task createFromDTO(DTOTaskCreate dto, User user)
			throws IllegalArgumentException {

		// validate DTO's description field. if it's not valid, exception is
		// thrown
		if (dto.getDescription() == null || dto.getDescription().isEmpty()) {
			throw new IllegalArgumentException(
					"Description cannot be null. Creation request failed.");
		}

		// instantiate a new task using DTO's values
		Task task = new Task();
		task.setUser(user);
		task.setDescription(dto.getDescription());
		Status toDoStatus = statusService.findStatusByName("to-do");
		task.setStatus(toDoStatus);
		// task.setUser(dto.getUser());
		// saves the task in the DB and return it
		taskRepo.save(task);
		return task;
	}

	//UPDATE
	
	/**
	 * Updates the status of an existing {@link Task} based on the provided DTO.
	 * <p>
	 * If the new status name is "DONE" ({@code 3}), the task's completed timestamp
	 * is set to the current time.
	 * </p>
	 * 
	 * @param task the task to update
	 * @param dto  the DTO containing the new status name
	 * @return the updated {@link Task} entity
	 * @throws StatusNotFoundException if the status name does not exist
	 */
	public Task updateStatus(Task task, DTOTaskUpdate dto)
			throws StatusNotFoundException {

		Status status = statusService.findStatusByName(dto.getStatusName());

		// status field of the provided task is updated with the DTO's value
		task.setStatus(status);
		// if the new status is "COMPLETED", completedAt field is set to current
		// timestamp
		if (task.getStatus().getId() == 3) {
			task.setCompletedAt(Timestamp.valueOf(LocalDateTime.now()));
		}
		// saves the task in the DB and return it
		taskRepo.save(task);

		return task;

	}

	/**
	 * Updates the description of an existing {@link Task} based on the provided
	 * DTO.
	 * 
	 * @param task the task to update
	 * @param dto  the DTO containing the new description
	 * @return the updated {@link Task} entity
	 * @throws IllegalArgumentException if the description is null or empty
	 */
	public Task updateDescription(Task task, DTOTaskUpdate dto)
			throws IllegalArgumentException {
		// validate DTO's description field. if it's not valid, exception is
		// thrown
		if (dto.getDescription() == null || dto.getDescription().isEmpty()) {
			throw new IllegalArgumentException(
					"Desciption is empty. Description update request failed");
		}
		// description field of the provided task is updated with the DTO's
		// value
		task.setDescription(dto.getDescription());
		// saves the task in the DB and return it
		taskRepo.save(task);
		return task;
	}
	
	
	//DELETE

	/**
	 * Marks the given {@link Task} as deleted by setting its status to {@code 4}
	 * ("DELETED"), recording the current deletion timestamp, and flagging it as
	 * deleted.
	 * 
	 * @param task the task to mark as deleted
	 */
	public void markAsDeleted(Task task) {
		// sets status of the provided task to DELETED
		task.setStatus(statusService.findStatusById(4));
		// sets deletedAt field to current timestamp
		task.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));
		// sets isDeleted field to true
		task.setDeleted(true);
		// saves the task in the DB
		taskRepo.save(task);
	}

	//VALIDATION
	
	/**
	 * Checks whether the given {@link Task} belongs to the specified {@link User}.
	 * 
	 * @param task the task to check
	 * @param user the user to compare against
	 * @return {@code true} if the task belongs to the user; {@code false}
	 *         otherwise
	 */
	public boolean isUserTask(Task task, User user) {

		long userId= user.getId();
		long taskUserId = task.getUser().getId();

		if (userId == taskUserId) {
			return true;
		}

		return false;
	}

}
