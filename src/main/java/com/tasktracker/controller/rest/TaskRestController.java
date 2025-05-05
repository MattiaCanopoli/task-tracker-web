package com.tasktracker.controller.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

	private final TaskService tService;

	public TaskRestController(TaskService tService) {
		this.tService = tService;
	}

	//READ (multiple)
	/**
	 * Retrieves a list of tasks.
	 * This list can be filtered by status. if status is not provided, all the list are retrieved.
	 * @param status a String representing the status name ("to-do", "in-progress","done")
	 * @return a List of tasks and HttpStatus 200(OK). 
	 * @return HttpStatus 204(NO_CONTENT) if no tasks are retrieved.
	 */
	@GetMapping("/tasks")
	public ResponseEntity<List<Task>> getTasks(
			@RequestParam(name = "status", required = false) String status) {

		List<Task> tasks = tService.getTasks();

		if (status != null && !status.isEmpty()) {

			tasks = tService.getByStatusName(status);

		}

		if (tasks.size() == 0) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<>(tasks, HttpStatus.OK);

	}

	//SHOW
	
	/**
	 * Retrieves the task with the provided ID
	 * 
	 * @param id long of the task to find
	 * @return task with the provided id and HttpStatus 200(OK); 
	 * @return HttpStatus 404(NOT_FOUND) and an error message if the task does not exists.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<?> getSingleTask(@PathVariable("id") Long id) {

		Task t = tService.getByID(id);

		if (t != null) {
			return new ResponseEntity<>(t, HttpStatus.OK);
		}

		return new ResponseEntity<>("task with ID " + id + " not found",HttpStatus.NOT_FOUND);

	}

	//CREATE
	
	/**
	 * Creates and persists a new task.
	 * RequestBody is a DTO containing data ("description", "status_id" and "user") to be passed to the new task.
	 * if any of the field is missing, return HttpStasus 400 (BAD_REQUEST) and an error message.
	 * @param dtoTask RequestBody: a JSON containing "description", "status_id" and "user"
	 * @return HttpStatus 201 (CREATED) and newly created task.
	 * @return HttpStatus 400 (BAD_REQUEST) and error message if JSON doesn't contains all the fields.
	 */
	@PostMapping("save")
	public ResponseEntity<?> store(@RequestBody DTOTask dtoTask) {
		Task task = tService.createFromDTO(dtoTask);

		if (task == null) {
			return new ResponseEntity<>("missing fields.\nFields \"description\", \"status_id\" and \"user\" must be specified.",HttpStatus.BAD_REQUEST);
		}
		tService.save(task);
		return new ResponseEntity<Task>(task, HttpStatus.CREATED);

	}
	//UPDATE
	
	/**
	 * Updates an existing task.
	 * The task to update is retrieved by id, provided via DTO.
	 * If the provided id is not valid, returns and error message.
	 * @param dtoTask RequestBody: a JSON containing "description", "status_id" and "id"
	 * @return HttpStatus 200 (OK) and updated task.
	 * @return HttpStatus 404 (NOT_FOUND) if id provided via JSON is not valid.
	 */
	@PatchMapping("update")
	public ResponseEntity<?> update(@RequestBody DTOTask dtoTask){
		Task task = tService.getByID(dtoTask.getId());
		
		if (task==null) {
			return new ResponseEntity<>("Task with ID " + dtoTask.getId() + " not found", HttpStatus.NOT_FOUND);
		} else {
		
			tService.update(task, dtoTask);
		return new ResponseEntity<Task>(task,HttpStatus.OK);
		
		}
	}

}
