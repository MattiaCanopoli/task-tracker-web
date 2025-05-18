package com.tasktracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasktracker.security.model.Role;

public interface RoleRepo extends JpaRepository<Role, Integer>{

	public Optional<Role> findRoleByName(String roleName);
}
