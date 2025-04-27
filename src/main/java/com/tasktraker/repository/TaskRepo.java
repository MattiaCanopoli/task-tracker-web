package com.tasktraker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasktraker.model.Task;

public interface TaskRepo extends JpaRepository<Task, Long>{

}
