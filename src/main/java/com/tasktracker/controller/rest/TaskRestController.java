package com.tasktracker.controller.rest;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.tasktracker.service.TaskService;

//@CrossOrigin
@RestController
@RequestMapping("/rest")
public class TaskRestController {

	private static final Logger logger = LoggerFactory
			.getLogger(TaskRestController.class);
	private final TaskService tService;

	public TaskRestController(TaskService tService) {
		this.tService = tService;
	}

	// READ (multiple)
	/**
	 * Retrieves a list of tasks. This list can be filtered by status name. If
	 * the status name is not provided, all tasks are retrieved.
	 * 
	 * @param status
	 *            (optional) Valid values:
	 *            "to-do","in-progress","done","deleted"
	 * @return a List of tasks and HttpStatus 200(OK) or HttpStatus
	 *         204(NO_CONTENT) if no tasks are retrieved.
	 */
	@GetMapping("/tasks")
	public ResponseEntity<?> list(
			@RequestParam(name = "status", required = false) String status) {
		List<Task> tasks;

		if (status != null && !status.isEmpty()) {
			logger.info("Attempting to retrieve tasks with \"{}\" status",
					status);

			try {
				tasks = tService.getByStatusName(status);

			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage());

				return new ResponseEntity<>(e.getMessage(),
						HttpStatus.BAD_REQUEST);
			}

		} else {
			tasks = tService.getTasks();
			logger.info("Attempting to retrieve all tasks");
		}

		if (tasks.size() == 0) {
			logger.warn("No tasks found");
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		logger.info("Found " + tasks.size() + " tasks");
		return new ResponseEntity<>(tasks, HttpStatus.OK);

	}

	// SHOW

	/**
	 * Retrieves the task with the provided ID
	 * 
	 * @param id
	 *            long of the task to find
	 * @return HttpStatus 200(OK) and task having the provided id, or HttpStatus
	 *         404(NOT_FOUND) and an error message if the task is not found.
	 */
	@GetMapping("tasks/{id}")
	public ResponseEntity<?> detail(@PathVariable("id") Long id) {

		logger.info("Attempting to retrieve task with id {}", id);

		Task t = new Task();

		try {
			t = tService.getByID(id);
		} catch (NoSuchElementException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}

		logger.info("Task with id {} found. Task description: {}", id,
				t.getDescription());
		return new ResponseEntity<>(t, HttpStatus.OK);

	}

	// CREATE

	/**
	 * Creates and persists a new task. The request body must contain a DTO with
	 * the following fields:"description", "status_id" and "user" to be passed
	 * to the new task. If any required field is missing, returns HttpStatus 400
	 * (BAD_REQUEST) and an error message.
	 * 
	 * @param dtoTask
	 *            RequestBody: a JSON containing "description", "status_id" and
	 *            "user"
	 * @return HttpStatus 201 (CREATED) and newly created task or HttpStatus 400
	 *         (BAD_REQUEST) and error message if JSON does not contain all the
	 *         fields.
	 */
	@PostMapping("/tasks")
	public ResponseEntity<?> create(@RequestBody DTOTask dtoTask) {
		Task task = tService.createFromDTO(dtoTask);

		if (task == null) {
			return new ResponseEntity<>(
					"Missing fields.\nFields \"description\", \"status_id\" and \"user\" must be specified.",
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<Task>(task, HttpStatus.CREATED);

	}
	// UPDATE

	/**
	 * Updates an existing task. The task to update is retrieved by id, provided
	 * via DTO. If the provided id is not valid, returns an error message.
	 * 
	 * @param dtoTask
	 *            RequestBody: a JSON containing "description", "status_id" and
	 *            "id"
	 * @return HttpStatus 200 (OK) and updated task or HttpStatus 404
	 *         (NOT_FOUND) if task is not found.
	 */
	@PatchMapping("tasks/{id}")
	public ResponseEntity<?> update(@PathVariable("id") long id,
			@RequestBody DTOTask dtoTask) {
		Task task = tService.getByID(id);

		if (task == null) {
			return new ResponseEntity<>(
					"Task with ID " + dtoTask.getId() + " not found",
					HttpStatus.NOT_FOUND);
		} else {

			tService.update(task, dtoTask);
			return new ResponseEntity<Task>(task, HttpStatus.OK);

		}
	}
	// DELETE

	/**
	 * Marks the task having the provided ID as "DELETED".
	 * 
	 * @param id
	 *            ID of the task to be deleted
	 * @return HttpStatus 200 (OK) and confirmation message or HttpStatus 404
	 *         (NOT_FOUND) and error message if task is not found.
	 */
	@DeleteMapping("tasks/{id}")
	public ResponseEntity<String> delete(@PathVariable("id") long id) {

		String message = "Task with ID " + id + " not found";

		Task task = tService.getByID(id);

		if (task != null) {
			message = tService.markAsDeleted(task);
			return new ResponseEntity<String>(message, HttpStatus.OK);
		}

		return new ResponseEntity<String>(message, HttpStatus.NOT_FOUND);

	}

}
