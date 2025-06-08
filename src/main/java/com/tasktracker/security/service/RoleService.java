package com.tasktracker.security.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tasktracker.exception.RoleNotFoundException;
import com.tasktracker.repository.RoleRepo;
import com.tasktracker.security.model.Role;

@Service
public class RoleService {

	private final RoleRepo rRepo;
	private static final Logger logger = LoggerFactory
			.getLogger(RoleService.class);

	public RoleService(RoleRepo rRepo) {
		this.rRepo = rRepo;
	}

	public Role getByName(String name) throws RoleNotFoundException {

		logger.info("Attempting to retrieve '{}' role", name.toUpperCase());
		Optional<Role> role = rRepo.findRoleByName(name.toUpperCase());

		if (!role.isPresent()) {
			logger.error("{} cannot be found", name.toUpperCase());
			throw new RoleNotFoundException("Role " + name + " not found");
		}

		logger.info("Role '{}' successfully found", name.toUpperCase());
		return role.get();
	}

	public Role getByID(int id) throws RoleNotFoundException {

		logger.info("Attempting to retrieve role with ID '{}'", id);
		Optional<Role> role = rRepo.findById(id);

		if (!role.isPresent()) {
			logger.error("Role with ID '{}' cannot be found", id);
			throw new RoleNotFoundException(
					"Role with id '" + id + "' not found");
		}
		logger.info("Successfully retrieved role with ID '{}'", id);
		return role.get();

	}
}
