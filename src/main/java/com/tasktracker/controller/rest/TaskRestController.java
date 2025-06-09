package com.tasktracker.controller.rest;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasktracker.dto.DTOTask;
import com.tasktracker.dto.DTOTaskCreate;
import com.tasktracker.model.Task;
import com.tasktracker.security.model.User;
import com.tasktracker.security.service.UserService;
import com.tasktracker.service.TaskService;

/**
 * REST controller for managing {@link Task} entities.
 * <p>
 * Provides endpoints for CRUD operations on tasks, including filtering tasks by status,
 * retrieving task details, creating new tasks, updating existing tasks, and marking tasks as deleted.
 * <p>
 * All endpoints require the user to be authenticated. Ownership checks are performed
 * to ensure users can only access and modify their own tasks.
 * </p>
 * 
 * Base URL for all endpoints is <code>/rest</code>.
 * 
 * @author 
 * @version 1.0
 */
//@CrossOrigin
@RestController
@RequestMapping("/rest")
public class TaskRestController {

	private static final Logger logger = LoggerFactory
			.getLogger(TaskRestController.class);
	private final TaskService tService;
	private final UserService uService;

	/**
	 * Constructs a new {@code TaskRestController} with the specified services.
	 * 
	 * @param tService the task service used to perform operations on tasks
	 * @param uService the user service used to retrieve user information
	 */
	public TaskRestController(TaskService tService, UserService uService) {
		this.tService = tService;
		this.uService = uService;
	}

	// READ (multiple)

	/**
	 * Retrieves a list of tasks for the currently authenticated user.
	 * <p>
	 * If a status query parameter is provided, filters the tasks by that status.
	 * If no status is provided, returns all tasks for the user.
	 * </p>
	 * <p>
	 * Returns HTTP 204 (No Content) if no tasks are found.
	 * Returns HTTP 400 (Bad Request) if the provided status is invalid.
	 * </p>
	 * 
	 * @param status optional task status filter
	 * @param auth the authentication object of the current user
	 * @return a {@link ResponseEntity} with the list of tasks or an error status
	 */
	@GetMapping("tasks")
	public ResponseEntity<?> list(
			@RequestParam(name = "status", required = false) String status,
			Authentication auth) {
		List<Task> tasks;

		long id = uService.getIdByUsername(auth.getName());

		if (status != null && !status.isEmpty()) {
			logger.info("Attempting to retrieve tasks with \"{}\" status",
					status);

			// status is validated. if it is not valid, the exception is caught
			// and a ResponseEntity with HttpStatus 400 (BAD_REQUEST) and an
			// error message is returned
			try {
				tasks = tService.getByUserAndStatus(id,status);

			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage());

				return new ResponseEntity<>(e.getMessage(),
						HttpStatus.BAD_REQUEST);
			}
			// if status is not specified, all tasks are retrieved
		} else {

			tasks = tService.getByUserID(id);
			logger.info("Attempting to retrieve all tasks");
		}

		// if there are no tasks with the specified status (or no tasks at all),
		// a ResponseEntity with HttpStatus 204 (NO_CONTENT) is returned
		if (tasks.size() == 0) {
			logger.warn("No tasks found");
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		// A ResponseEntity with HttpStatus 200 (OK) and a list of all retrieved
		// tasks is returned
		logger.info("Found " + tasks.size() + " tasks");
		return new ResponseEntity<>(tasks, HttpStatus.OK);

	}

	// SHOW

	/**
	 * Retrieves the details of a specific task by its ID.
	 * <p>
	 * Verifies that the authenticated user owns the task.
	 * Returns HTTP 404 (Not Found) if the task does not exist.
	 * Returns HTTP 401 (Unauthorized) if the user is not the owner.
	 * </p>
	 * 
	 * @param id the ID of the task to retrieve
	 * @param auth the authentication object of the current user
	 * @return a {@link ResponseEntity} containing the task or an error status
	 */
	@GetMapping("tasks/{id}")
	public ResponseEntity<?> detail(@PathVariable("id") Long id, Authentication auth) {

		logger.info("Attempting to retrieve task with ID {}...", id);

		Task task = new Task();

		// the provided ID is validated. if is not valid, the exception is
		// caught and a ResponseEntity with HttpStatus 404 (NOT_FOUND) and an
		// error message is returned
		try {
			task = tService.getByID(id);
		} catch (NoSuchElementException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}

		// retrieve current authenticated user and verify that is owner of the task to retrieve.
		// if verification fails (isUserTask return false), a ResponseEntity with HttpStatus 403 (UNAUTHORIZED) and an
		// error message is returned
		User user=uService.getByUsername(auth.getName());

		if (!tService.isUserTask(task, user)) {
			logger.error("user {} attempted to access task with id {}", auth.getName(), id);
			return new ResponseEntity<>("Cannot access task with id " + id + " with current user", HttpStatus.UNAUTHORIZED);
		}

		// a ResponseEntity with HttpStatus 200 (OK) and the task with the
		// specified ID is returned
		logger.info("Task with id {} found. Task description: {}", id,
				task.getDescription());
		return new ResponseEntity<>(task, HttpStatus.OK);

	}

	// CREATE

	/**
	 * Creates a new task for the currently authenticated user.
	 * <p>
	 * Validates the data from the provided {@link DTOTask} object.
	 * Returns HTTP 400 (Bad Request) if the data is invalid.
	 * </p>
	 * 
	 * @param dtoTask the data transfer object containing task details
	 * @param auth the authentication object of the current user
	 * @return a {@link ResponseEntity} containing the created task or an error status
	 */
	@PostMapping("tasks")
	public ResponseEntity<?> create(@RequestBody DTOTaskCreate dtoTask,
			Authentication auth) {
		logger.info("Attempting to create a new task...");
		Task task = new Task();
		// DTO's fields (description, user, status_id) are validated. if any of
		// the field is not valid, the exception is caught and a ResponseEntity
		// with HttpStatus 400
		// (BAD_REQUEST) and an error message is returned
		try {
			User user = uService.getByUsername(auth.getName());
			task = tService.createFromDTO(dtoTask, user);
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

		}
		// a ResponseEntity with HttpStatus 201 (CREATED) and the new task is
		// returned
		logger.info("Task successfully created with ID: {}", task.getId());
		return new ResponseEntity<>(task, HttpStatus.CREATED);
	}
	// UPDATE

	/**
	 * Updates an existing task identified by its ID.
	 * <p>
	 * Only allows updates to the task's status and description.
	 * Prevents changing the status to "deleted" via PATCH; use DELETE endpoint instead.
	 * Verifies task ownership.
	 * Returns HTTP 404 (Not Found) if the task does not exist.
	 * Returns HTTP 400 (Bad Request) for invalid data or if the task is marked deleted.
	 * Returns HTTP 401 (Unauthorized) if the user is not the owner.
	 * </p>
	 * 
	 * @param id the ID of the task to update
	 * @param dto the DTO containing updated data
	 * @param auth the authentication object of the current user
	 * @return a {@link ResponseEntity} containing the updated task or an error status
	 */
	@PatchMapping("tasks/{id}")
	public ResponseEntity<?> update(@PathVariable("id") long id,
			@RequestBody DTOTask dto, Authentication auth) {

		logger.info("Attempting to retrieve task with ID {}...", id);
		Task task = new Task();
		// the provided ID is validated. if is not valid, the exception is
		// caught and a ResponseEntity with HttpStatus 404 (NOT_FOUND) and an
		// error message is returned
		try {
			task = tService.getByID(id);
			logger.info("Successfully retrieved task: {}", task.toString());
		} catch (NoSuchElementException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}

		// retrieve current authenticated user and verify that is owner of the task to retrieve.
		// if verification fails (isUserTask return false), a ResponseEntity with HttpStatus 403 (UNAUTHORIZED) and an
		// error message is returned
		User user=uService.getByUsername(auth.getName());

		if (!tService.isUserTask(task, user)) {
			logger.error("user {} attempted to update task with id {}", auth.getName(), id);
			return new ResponseEntity<>("Cannot update task with id " + id + " with current user", HttpStatus.UNAUTHORIZED);
		}

		// task status is checked. if is DELETED, a a ResponseEntity with
		// HttpStatus 400 (BAD_REQUEST) and an error message is returned
		if (task.isDeleted()) {
			return new ResponseEntity<>(
					"Task has already been marked as deleted and cannot be updated.",
					HttpStatus.BAD_REQUEST);
		}
		// if provided, DTO's status_id is validated. if it is not valid,
		// exception is caught and a ResponseEntity with HttpStatus 400
		// (BAD_REQUEST) and an error message is returned
		if (dto.getStatusID() > 0) {
			try {
				// it's not possible to mark a task as deleted using this method
				// if status is valid, but is DELETED, a ResponseEntity with
				// HttpStatus 400 (BAD_REQUEST) and an error message is returned
				if (dto.getStatusID() == 4) {
					return new ResponseEntity<>(
							"Cannot update task status to 'deleted' via PATCH. Use DELETE instead.",
							HttpStatus.BAD_REQUEST);
				}
				// upon validation, status is updated
				tService.updateStatus(task, dto);
			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage());
				return new ResponseEntity<>(e.getMessage(),
						HttpStatus.BAD_REQUEST);
			}

		}

		// if provided, DTO's description is validated. if is not valid,
		// exception is caught and a ResponseEntity with HttpStatus 400
		// (BAD_REQUEST) and an error message is returned
		if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
			try {
				// upon validation, description is updated
				tService.updateDescription(task, dto);
			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage());
				return new ResponseEntity<>(e.getMessage(),
						HttpStatus.BAD_REQUEST);
			}
		}

		// a ResponseEntity with HttpStatus 200 (OK) and the updated task is
		// returned
		logger.info("Successfully updated task: {}", task.toString());
		return new ResponseEntity<>(task, HttpStatus.OK);

	}

	// DELETE

	/**
	 * Marks a task as deleted by its ID.
	 * <p>
	 * Sets the task status to "deleted" and marks it as deleted.
	 * Verifies that the authenticated user is the owner of the task.
	 * Returns HTTP 404 (Not Found) if the task does not exist.
	 * Returns HTTP 401 (Unauthorized) if the user is not the owner.
	 * </p>
	 * 
	 * @param id the ID of the task to delete
	 * @param auth the authentication object of the current user
	 * @return a {@link ResponseEntity} with a confirmation message or an error status
	 */
	@DeleteMapping("tasks/{id}")
	public ResponseEntity<String> delete(@PathVariable("id") long id, Authentication auth) {

		logger.info("Attempting to delete task with ID: {}", id);
		Task task = new Task();
		// the provided ID is validated. if is not valid, the exception is
		// caught and a ResponseEntity with HttpStatus 404 (NOT_FOUND) and an
		// error message is returned
		try {
			task = tService.getByID(id);
			logger.info("Found task: {}", task);
		} catch (NoSuchElementException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}

		// retrieve current authenticated user and verify that is owner of the task to retrieve.
		// if verification fails (isUserTask return false), a ResponseEntity with HttpStatus 403 (UNAUTHORIZED) and an
		// error message is returned
		User user=uService.getByUsername(auth.getName());

		if (!tService.isUserTask(task, user)) {
			logger.error("user {} attempted to delete task with id {}", auth.getName(), id);
			return new ResponseEntity<>("Cannot delete task with id " + id + " with current user", HttpStatus.UNAUTHORIZED);
		}

		// upon validation, the task with the specified ID is marked ad deleted
		tService.markAsDeleted(task);
		logger.info("Task with ID {} marked as deleted", id);
		// a ResponseEntity with HttpStatus 200 and a confirmation message is
		// returned
		return new ResponseEntity<>(
				"Task with ID " + task.getId() + " successfully deleted",
				HttpStatus.OK);
	}

}
