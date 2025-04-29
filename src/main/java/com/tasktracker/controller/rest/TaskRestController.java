package com.tasktracker.controller.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

	//READ (single)
	@GetMapping("/{id}")
	public ResponseEntity<Task> getSingleTask(@PathVariable("id") Long id) {

		Task t = tService.getByID(id);

		if (t != null) {
			return new ResponseEntity<>(t, HttpStatus.OK);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);

	}
	@PostMapping("save")
	public Task store(@RequestBody Task task) {
		Task newTask = task;
		tService.save(newTask);
		return newTask;
		
	}
}
