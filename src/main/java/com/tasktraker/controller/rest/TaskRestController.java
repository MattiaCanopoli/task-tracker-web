package com.tasktraker.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
	public ResponseEntity<List<Task>> getTasks(){
		
		List<Task> tasks = tService.getTasks();
		
		if (tasks.size()==0) {
			return new ResponseEntity<>(tasks, HttpStatus.NO_CONTENT);
		}
		
		return new ResponseEntity<>(tasks, HttpStatus.OK);
		
	}

}
