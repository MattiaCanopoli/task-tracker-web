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
import com.tasktracker.model.Task;
import com.tasktracker.security.model.User;
import com.tasktracker.security.service.UserService;
import com.tasktracker.service.TaskService;

//@CrossOrigin
@RestController
@RequestMapping("/rest")
public class TaskRestController {

	private static final Logger logger = LoggerFactory
			.getLogger(TaskRestController.class);
	private final TaskService tService;
	private final UserService uService;

	public TaskRestController(TaskService tService, UserService uService) {
		this.tService = tService;
		this.uService = uService;
	}

	// READ (multiple)

	/**
	 * Retrieves a list of tasks based on the provided status. If no status is
	 * provided, all tasks are returned.
	 * <p>
	 * If a status is provided, it is used to filter tasks by their current
	 * status. If the status is invalid, a {@link BadRequestException} is
	 * returned with an appropriate error message.
	 * </p>
	 * <p>
	 * If no tasks are found, a {@link HttpStatus#NO_CONTENT} response is
	 * returned. Otherwise, the list of tasks is returned with a
	 * {@link HttpStatus#OK} status.
	 * </p>
	 *
	 * @param status
	 *            the status of the tasks to retrieve (optional)
	 * @return a {@link ResponseEntity} containing the list of tasks or an error
	 *         message if an invalid status is provided
	 * @throws IllegalArgumentException
	 *             if the provided status does not correspond to an existing
	 *             status
	 */
	@GetMapping("tasks")
	public ResponseEntity<?> list(
			@RequestParam(name = "status", required = false) String status,
			Authentication auth) {
		List<Task> tasks;
		// TODO: filter password by user and status
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
	 * Retrieves the details of a {@link Task} with the specified ID.
	 * <p>
	 * Attempts to retrieve the {@link Task} from the database using the
	 * provided ID. If no task is found with the given ID, a
	 * {@link HttpStatus#NOT_FOUND} response is returned with an appropriate
	 * error message.
	 * </p>
	 * <p>
	 * If the task is found, the ownership is verified against the currently authenticated user.
	 * If the user is not the owner, a {@link HttpStatus#UNAUTHORIZED} response is returned.
	 * </p>
	 * <p>
	 * If the task exists and ownership is verified, the task details are returned in the response
	 * with a {@link HttpStatus#OK} status.
	 * </p>
	 *
	 * @param id
	 *            the ID of the {@link Task} to retrieve
	 * @param auth
	 *            the authentication object representing the current user
	 * @return a {@link ResponseEntity} containing the {@link Task} details if
	 *         found and authorized, or an error message otherwise
	 * @throws NoSuchElementException
	 *             if the task with the provided ID does not exist
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
	 * Creates a new {@link Task} based on the provided {@link DTOTask} data.
	 * <p>
	 * Attempts to create a new task by mapping the fields from the provided
	 * DTO. If the DTO contains invalid or missing fields, an
	 * {@link HttpStatus#BAD_REQUEST} response is returned with an appropriate
	 * error message.
	 * </p>
	 * <p>
	 * If the task is created successfully, the task details are returned in the
	 * response with a {@link HttpStatus#CREATED} status.
	 * </p>
	 *
	 * @param dtoTask
	 *            the {@link DTOTask} object containing the task data to be
	 *            validated and persisted. The request body should be a json
	 *            with the format "{"description":"task description string", "status_id":int}"
	 * @return a {@link ResponseEntity} containing the newly created
	 *         {@link Task} if successful, or an error message if the creation
	 *         fails
	 * @throws IllegalArgumentException
	 *             if the data in the DTO is invalid or incomplete
	 */
	@PostMapping("tasks")
	public ResponseEntity<?> create(@RequestBody DTOTask dtoTask,
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
	 * Updates the specified {@link Task} entity based on the provided
	 * {@link DTOTask} data.
	 * <p>
	 * The task is first retrieved by its ID. If the task does not exist, a
	 * {@link HttpStatus#NOT_FOUND} response is returned. If the task has
	 * already been marked as deleted, a {@link HttpStatus#BAD_REQUEST} response
	 * is returned, indicating that the task cannot be updated.
	 * </p>
	 * <p>
	 * Before performing any update, the ownership of the task is verified against the
	 * currently authenticated user. If the user is not the owner, a
	 * {@link HttpStatus#UNAUTHORIZED} response is returned.
	 * </p>
	 * <p>
	 * If the {@link DTOTask} contains a status update, the task's status will
	 * be updated. However, if the status is set to "deleted" (status ID 4), an
	 * error message is returned, as tasks marked as deleted cannot be updated
	 * through a PATCH request.
	 * </p>
	 * <p>
	 * If the {@link DTOTask} contains a non-null description, the task's
	 * description will be updated.
	 * </p>
	 * <p>
	 * Upon successful update, the task is returned in the response with an
	 * {@link HttpStatus#OK} status.
	 * </p>
	 *
	 * @param id
	 *            the ID of the task to be updated
	 * @param dto
	 *            the {@link DTOTask} object containing the updated data (status
	 *            or description)
	 * @param auth
	 *            the authentication object representing the current user
	 * @return a {@link ResponseEntity} containing the updated {@link Task} if
	 *         successful, or an error message if any validation fails or if the user is not authorized
	 * @throws NoSuchElementException
	 *             if no task with the provided ID is found
	 * @throws IllegalArgumentException
	 *             if the status ID or description in the {@link DTOTask} is
	 *             invalid
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
	 * Marks the {@link Task} with the specified ID as deleted.
	 * <p>
	 * The task is first retrieved from the database using the provided ID. If
	 * no task with the given ID exists, a {@link HttpStatus#NOT_FOUND} response
	 * is returned.
	 * </p>
	 * <p>
	 * Before marking the task as deleted, the controller verifies that the
	 * currently authenticated user is the owner of the task. If the user is not
	 * the owner, a {@link HttpStatus#UNAUTHORIZED} response is returned.
	 * </p>
	 * <p>
	 * If the task is found and the ownership check passes, it is marked as deleted by:
	 * setting its {@code status} to "DELETED" (ID 4),
	 * its {@code deletedAt} timestamp to the current time, and
	 * its {@code isDeleted} flag to {@code true}. The task is then persisted with the updated values.
	 * </p>
	 *
	 * @param id
	 *            the ID of the {@link Task} to be marked as deleted
	 * @param auth
	 *            the authentication object representing the current user
	 * @return a {@link ResponseEntity} containing a success message if the task
	 *         was deleted, or an error message with
	 *         {@link HttpStatus#NOT_FOUND} if the task does not exist,
	 *         or {@link HttpStatus#UNAUTHORIZED} if the user is not the owner
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
