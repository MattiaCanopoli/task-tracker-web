package com.tasktracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasktracker.model.Status;

public interface StatusRepo extends JpaRepository<Status, Integer> {
	
	public Optional<Status> findByStatusName(String statusName);
	

}
