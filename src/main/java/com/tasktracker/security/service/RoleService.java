package com.tasktracker.security.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tasktracker.repository.RoleRepo;
import com.tasktracker.security.model.Role;

@Service
public class RoleService {

	private final RoleRepo rRepo;

	public RoleService(RoleRepo rRepo) {
		this.rRepo=rRepo;
	}


	public Role getByName(String name) {
		Optional<Role> role = rRepo.findRoleByName(name);

		if (!role.isPresent()) {
			throw new IllegalArgumentException("Role not found");
		}

		return role.get();
	}
}
