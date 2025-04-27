package com.tasktraker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasktraker.model.User;

public interface UserRepo extends JpaRepository<User, Long> {

}
