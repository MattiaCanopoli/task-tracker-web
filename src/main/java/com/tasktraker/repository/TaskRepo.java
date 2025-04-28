package com.tasktraker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasktraker.model.Status;
import com.tasktraker.model.Task;

public interface TaskRepo extends JpaRepository<Task, Long>{
	
	//public List<Task> findByStatusContains(Status status);
}
