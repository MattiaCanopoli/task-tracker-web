package com.tasktracker.security.service;

import java.util.Collection;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.tasktracker.dto.DTOUser;
import com.tasktracker.dto.DTOUserUpdate;
import com.tasktracker.exception.FailedRoleUpdateException;
import com.tasktracker.exception.InvalidPasswordException;
import com.tasktracker.exception.UserAlreadyExistsException;
import com.tasktracker.exception.UserNotFoundException;
import com.tasktracker.repository.UserRepo;
import com.tasktracker.security.model.Role;
import com.tasktracker.security.model.User;

/**
 * Service class responsible for handling all business logic related to user
 * management, including creation, retrieval, update, deletion, and role
 * management.
 * <p>
 * This service acts as an intermediary between the {@link UserRepo} data access
 * layer and the application logic, performing validations, password encoding,
 * and role assignments.
 * </p>
 * <p>
 * It also provides methods for checking user roles and permissions.
 * </p>
 * 
 * @author
 */
@Service
public class UserService {

	private final UserRepo uRepo;
	private final RoleService rService;
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
	private static final Logger logger = LoggerFactory
			.getLogger(UserService.class);

	/**
	 * Constructs a new {@link UserService} with the specified user repository
	 * and role service.
	 * 
	 * @param uRepo
	 *            the user repository for database operations
	 * @param rService
	 *            the role service to handle role-related operations
	 */
	public UserService(UserRepo uRepo, RoleService rService) {
		this.uRepo = uRepo;
		this.rService = rService;
	}

	// READ

	/**
	 * Retrieves all users currently stored in the database.
	 * 
	 * @return a list containing all {@link User} entities
	 */
	public List<User> getAllUsers() {
		return uRepo.findAll();
	}

	/**
	 * Retrieves a {@link User} entity from the database by its username.
	 * 
	 * @param username
	 *            the username to find
	 * @return the corresponding {@link User} object
	 * @throws UsernameNotFoundException
	 *             if no user with the given username exists
	 */
	public User getByUsername(String username) {
		Optional<User> user = uRepo.findByUsername(username);
		if (!user.isPresent()) {
			throw new UsernameNotFoundException(
					"Username" + username + " not found");
		}

		return user.get();

	}

	/**
	 * Retrieves the unique identifier (ID) of a user based on their username.
	 * 
	 * @param username
	 *            the username to search for
	 * @return the ID of the user
	 * @throws UsernameNotFoundException
	 *             if the user with the specified username does not exist
	 */
	public long getIdByUsername(String username) {

		User user = this.getByUsername(username);

		return user.getId();

	}

	/**
	 * Retrieves a {@link User} entity by its unique ID.
	 * 
	 * @param id
	 *            the ID of the user to retrieve
	 * @return the {@link User} entity with the specified ID
	 * @throws UserNotFoundException
	 *             if no user with the specified ID exists
	 */
	public User getUserByID(long id) throws UserNotFoundException {

		Optional<User> user = uRepo.findById(id);

		if (!user.isPresent()) {
			throw new UserNotFoundException(
					"User with ID " + id + " cannot be found");
		}

		return user.get();
	}

	// CREATE

	/**
	 * Creates and persists a new user entity in the database based on the data
	 * provided by the {@link DTOUser} object.
	 * <p>
	 * This method performs several validations:
	 * <ul>
	 * <li>Checks if the username is already in use</li>
	 * <li>Checks if the email is already in use</li>
	 * <li>Validates the length of the password (minimum 6, maximum 24
	 * characters)</li>
	 * </ul>
	 * Passwords are securely encoded using BCrypt before being saved.
	 * <p>
	 * The new user is assigned the default role "USER".
	 * 
	 * @param dto
	 *            the data transfer object containing the new user's data
	 * @return the newly created {@link User} entity
	 * @throws UserAlreadyExistsException
	 *             if the username or email is already taken
	 * @throws InvalidPasswordException
	 *             if the password length does not meet the security
	 *             requirements
	 */
	public User saveUser(DTOUser dto) {

		if (uRepo.findByUsername(dto.getUsername()).isPresent()) {
			throw new UserAlreadyExistsException(
					"User: \"" + dto.getUsername() + "\" already exists");
		}

		if (uRepo.findByEmail(dto.getEmail()).isPresent()) {
			throw new UserAlreadyExistsException(
					"Email: \"" + dto.getEmail() + "\" already exists");
		}

		if (dto.getPassword().length() < 6) {
			throw new InvalidPasswordException(
					"Choosen password is too short. Password should be at least 6 characters long");
		}

		if (dto.getPassword().length() > 24) {
			throw new InvalidPasswordException(
					"Choosen password is too long. Password should be at most 24 characters long");
		}

		User user = new User();
		user.setUsername(dto.getUsername());
		user.setEmail(dto.getEmail());

		String password = encoder.encode(dto.getPassword());
		user.setPassword(password);

		user.setRoles(Set.of(rService.getByName("USER")));

		uRepo.save(user);

		return user;

	}

	// UPDATE

	/**
	 * Updates the currently authenticated user's email and/or password.
	 * <p>
	 * Requires the current password for validation before making any changes.
	 * If the new email is provided and not empty, updates the email. If the new
	 * password is provided and not empty, encodes and updates the password.
	 * </p>
	 * 
	 * @param dto
	 *            the {@link DTOUserUpdate} containing the updated fields and
	 *            the current password for validation
	 * @param auth
	 *            the {@link Authentication} object representing the current
	 *            user
	 * @return the updated {@link User} entity
	 * @throws InvalidPasswordException
	 *             if the provided current password does not match the stored
	 *             password
	 */
	public User updateUser(DTOUserUpdate dto, Authentication auth) {

		User currentUser = this.getByUsername(auth.getName());
		String username = currentUser.getUsername();

		if (!encoder.matches(dto.getCurrentPassword(),
				currentUser.getPassword())) {

			logger.warn("User '{}' provided incorrect current password",
					username);
			throw new InvalidPasswordException("Current password is incorrect");
		}

		if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {

			logger.info("User '{}' is updating email from '{}' to '{}'",
					username, currentUser.getEmail(), dto.getEmail());

			currentUser.setEmail(dto.getEmail());
		}

		if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {

			logger.info("User '{}' is changing password", username);

			String newPass = encoder.encode(dto.getNewPassword());
			currentUser.setPassword(newPass);

		}

		uRepo.save(currentUser);

		return currentUser;

	}

	/**
	 * Adds a role to the specified user.
	 * <p>
	 * Checks if the user already has the role; if so, throws an exception to
	 * prevent duplicates.
	 * </p>
	 * 
	 * @param user
	 *            the {@link User} to which the role will be added
	 * @param roleToAdd
	 *            the name of the role to add
	 * @return the updated {@link User} entity with the new role
	 * @throws FailedRoleUpdateException
	 *             if the user already has the specified role
	 */
	public User addRole(User user, String roleToAdd) {

		Set<Role> roles = user.getRoles();
		logger.info("{} roles retrieved. Found {} roles", user.getUsername(),
				roles.size());
		Role role = rService.getByName(roleToAdd);

		if (roles.contains(role)) {
			logger.error(
					"{} already has '{}' role. Impossible to add duplicate roles",
					user.getUsername(), roleToAdd.toUpperCase());
			throw new FailedRoleUpdateException(user.getUsername()
					+ " already has role '" + roleToAdd + "'");
		}
		roles.add(role);
		user.setRoles(roles);
		logger.info("'{}' added to {} roles", roleToAdd.toUpperCase(),
				user.getUsername());

		uRepo.save(user);
		logger.info("{} roles successfully updated", user.getUsername());
		return user;

	}

	/**
	 * Removes a role from the specified user.
	 * <p>
	 * Ensures that a user has at least one role after removal; if the user has
	 * only one role or does not have the specified role, throws an exception.
	 * </p>
	 * 
	 * @param user
	 *            the {@link User} from which the role will be removed
	 * @param roleToRemove
	 *            the name of the role to remove
	 * @return the updated {@link User} entity with the role removed
	 * @throws FailedRoleUpdateException
	 *             if the user has only one role or does not have the role to
	 *             remove
	 */
	public User removeRole(User user, String roleToRemove) {

		Set<Role> roles = user.getRoles();
		logger.info("{} roles retrieved. Found {} roles", user.getUsername(),
				roles.size());

		if (roles.size() <= 1) {
			logger.error("{} has only {} role. Impossible to remove",
					user.getUsername(), roles.size());
			throw new FailedRoleUpdateException(
					"Any user must have at least one role");
		}

		Role role = rService.getByName(roleToRemove);

		if (!roles.contains(role)) {
			logger.error("{} does not have '{}' role.", user.getUsername(),
					roleToRemove.toUpperCase());
			throw new FailedRoleUpdateException(user.getUsername()
					+ " does not have the role '" + role.getName() + "'");
		}

		roles.remove(role);
		user.setRoles(roles);
		logger.info("'{}' removed from {} roles", roleToRemove.toUpperCase(),
				user.getUsername());

		uRepo.save(user);
		logger.info("{} roles successfully updated", user.getUsername());

		return user;

	}

	// DELETE

	/**
	 * Deletes a user from the database by their ID.
	 * 
	 * @param id
	 *            the ID of the user to delete
	 * @throws UserNotFoundException
	 *             if the user with the specified ID does not exist
	 */
	public void deleteById(long id) {
		User user = this.getUserByID(id);
		uRepo.delete(user);

	}

	// VALIDATION

	/**
	 * Checks if the authenticated user has the "ADMIN" role.
	 * 
	 * @param auth
	 *            the {@link Authentication} object representing the current
	 *            user
	 * @return {@code true} if the user has the ADMIN role, {@code false}
	 *         otherwise
	 */
	public boolean isAdmin(Authentication auth) {

		logger.info("Verifing {} authorities", auth.getName());

		Collection<? extends GrantedAuthority> authorities = auth
				.getAuthorities();

		for (GrantedAuthority authority : authorities) {
			String role = authority.getAuthority();

			if (role.equals("ADMIN")) {

				logger.info("{} is ADMIN", auth.getName());
				return true;
			}
		}

		logger.warn("{} is not ADMIN", auth.getName());
		return false;
	}

}