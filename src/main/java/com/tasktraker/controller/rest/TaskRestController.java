package com.tasktraker.controller.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasktraker.model.Task;
import com.tasktraker.service.TaskService;

//@CrossOrigin
@RestController
@RequestMapping("/rest")
public class TaskRestController {

	@Autowired
	TaskService tService;

	@GetMapping("/tasks")
	public ResponseEntity<List<Task>> getTasks(@RequestParam(name = "status", required = false) String status) {

		List<Task> tasks = tService.getTasks();

		if (status != null && !status.isEmpty()) {
			tasks = tService.getByStatus(status);
		}

		if (tasks.size() == 0) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<>(tasks, HttpStatus.OK);

	}

	@GetMapping("/tasks/{id}")
	public ResponseEntity<Task> test(@PathVariable("id") Long id) {

		Task t = tService.getByID(id);

		if (t != null) {
			return new ResponseEntity<>(t, HttpStatus.OK);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);

	}
}
