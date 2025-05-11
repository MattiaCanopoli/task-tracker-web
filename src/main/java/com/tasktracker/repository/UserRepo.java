package com.tasktracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasktracker.security.model.User;

public interface UserRepo extends JpaRepository<User, Long> {

}
