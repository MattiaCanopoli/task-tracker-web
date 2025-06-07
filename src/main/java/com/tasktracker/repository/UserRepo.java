package com.tasktracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasktracker.security.model.User;

public interface UserRepo extends JpaRepository<User, Long> {

	public Optional<User> findByUsername(String username);

	public Optional<User> findByEmail(String email);

	@Override
	public List<User> findAll();

	@Override
	public Optional<User> findById(Long id);

}
