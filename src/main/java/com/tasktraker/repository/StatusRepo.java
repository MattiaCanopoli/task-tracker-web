package com.tasktraker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasktraker.model.Status;

public interface StatusRepo extends JpaRepository<Status, Integer> {
	
	public Optional<Status> findByStatusName(String statusName);
	

}
