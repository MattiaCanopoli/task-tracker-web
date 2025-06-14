package com.tasktracker.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tasktracker.dto.DTORoleUpdate;
import com.tasktracker.dto.DTOUser;
import com.tasktracker.dto.DTOUserUpdate;
import com.tasktracker.exception.FailedRoleUpdateException;
import com.tasktracker.exception.InvalidPasswordException;
import com.tasktracker.exception.RoleNotFoundException;
import com.tasktracker.exception.UserAlreadyExistsException;
import com.tasktracker.exception.UserNotFoundException;
import com.tasktracker.security.model.User;
import com.tasktracker.security.service.UserService;

/**
 * REST controller for managing {@link User} entities.
 * <p>
 * Provides endpoints to:
 * <ul>
 *   <li>List all users (admin only)</li>
 *   <li>Retrieve user details by ID (admin or owner)</li>
 *   <li>Create a new user</li>
 *   <li>Update user information (self)</li>
 *   <li>Delete a user (admin only)</li>
 *   <li>Add or remove roles from a user (admin only)</li>
 * </ul>
 * Access control ensures that sensitive operations are restricted appropriately.
 * </p>
 *
 * @implSpec
 * This controller delegates user-related business logic to {@link UserService}
 * and handles HTTP responses based on service results and exceptions.
 */
@RestController
@RequestMapping("/user")
public class UserController {

	private static final Logger logger = LoggerFactory
			.getLogger(UserController.class);

	private final UserService uService;

	/**
	 * Constructs a new {@code UserController} with the specified user service.
	 *
	 * @param uService
	 *            the user service to delegate business logic to
	 */
	public UserController(UserService uService) {
		this.uService = uService;
	}

	//READ
	
	/**
	 * Retrieves all users.
	 * 
	 * Only an admin can access this endpoint.
	 *
	 * @return a {@link ResponseEntity} containing the list of all users with
	 *         HTTP status {@code 200 OK}, or a message with status
	 *         {@code 204 No Content} if no users exist
	 */
	@GetMapping
	public ResponseEntity<?> list(Authentication auth) {

		logger.info("{} is attempting to retrieve all users information",
				auth.getName());
		List<User> users = uService.getAllUsers();

		if (users.isEmpty()) {
			logger.warn("No user has been found");
			return new ResponseEntity<>("No users found.",
					HttpStatus.NO_CONTENT);
		}

		logger.info("{} users found", users.size());
		return new ResponseEntity<>(users, HttpStatus.OK);

	}
	
	//SHOW

	/**
	 * Retrieves the details of a user by their ID.
	 * <p>
	 * Only an admin or the user themselves can access this endpoint.
	 * </p>
	 *
	 * @param id
	 *            the ID of the user to retrieve
	 * @param auth
	 *            the authentication object representing the currently
	 *            authenticated user
	 * @return a {@link ResponseEntity} with the user details and HTTP status
	 *         {@code 200 OK} if found, {@code 400 Bad Request} if the user does
	 *         not exist, or {@code 401 Unauthorized} if the caller is not
	 *         authorized
	 */
	@GetMapping("/{id}")
	public ResponseEntity<?> detail(@PathVariable("id") long id,
			Authentication auth) {

		logger.info("User '{}' attempts to retrieve information for user '{}'",
				auth.getName(), id);

		long userId = uService.getIdByUsername(auth.getName());

		if (uService.isAdmin(auth) || userId == id) {

			try {
				User user = uService.getUserByID(id);

				logger.info(
						"{} successfully retrieved information for user '{}'",
						auth.getName(), id);
				return new ResponseEntity<>(user, HttpStatus.OK);

			} catch (UserNotFoundException e) {

				logger.error(e.getMessage());
				return new ResponseEntity<>(e.getMessage(),
						HttpStatus.BAD_REQUEST);
			}
		}

		logger.warn(
				"User '{}' is not authorized to retrieve information for user '{}'",
				auth.getName(), id);
		return new ResponseEntity<>("Unauthorized access",
				HttpStatus.UNAUTHORIZED);

	}
	
	//CREATE

	/**
	 * Creates a new user.
	 *
	 * @param user
	 *            the DTO containing user data to create
	 * @return a {@link ResponseEntity} with the created user and HTTP status
	 *         {@code 201 Created}
	 * @throws UserAlreadyExistsException
	 *             if a user with the same username already exists
	 * @throws InvalidPasswordException
	 *             if the provided password length is invalid
	 */
	@PostMapping
	public ResponseEntity<?> create(@Validated @RequestBody DTOUser user) {

		logger.info("Attempting to create a new user. Username: {}, e-mail: {}",
				user.getUsername(), user.getPassword());
		try {

			User newUser = uService.saveUser(user);

			logger.info("New user {} successfully created", user.getUsername());
			return new ResponseEntity<>(newUser, HttpStatus.CREATED);

		} catch (UserAlreadyExistsException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);

		} catch (InvalidPasswordException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	//UPDATE
	
	/**
	 * Updates the currently authenticated user's information.
	 *
	 * @param update the DTO containing updated user information
	 * @param auth   the authentication object representing the current user
	 * @return a {@link ResponseEntity} with the updated user and HTTP status {@code 200 OK},
	 *         or {@code 401 Unauthorized} if the password is invalid
	 */
	@PatchMapping
	public ResponseEntity<?> update(
			@Validated @RequestBody DTOUserUpdate update, Authentication auth) {

		logger.info("{} is attempting to update his information",
				auth.getName());
		try {
			uService.updateUser(update, auth);
		} catch (InvalidPasswordException e) {
			logger.error("Update failed");
			return new ResponseEntity<>(e.getMessage(),
					HttpStatus.UNAUTHORIZED);
		}

		User user = uService.getByUsername(auth.getName());
		logger.info("{} successfully updated his information",
				user.getUsername());
		return new ResponseEntity<>(user, HttpStatus.OK);

	}
	
	/**
	 * Adds a role to a specific user.
	 * <p>
	 * Only an admin can perform this operation.
	 * </p>
	 *
	 * @param id    the ID of the user to modify
	 * @param role  the DTO containing the role to add
	 * @param auth  the authentication object representing the current user
	 * @return a {@link ResponseEntity} with the updated user and HTTP status {@code 200 OK},
	 *         or {@code 400 Bad Request} if the user or role is invalid,
	 *         or {@code 401 Unauthorized} if the caller is not authorized
	 */
	@PatchMapping("/roles/add/{id}")
	public ResponseEntity<?> addRole(@PathVariable("id") long id,@Validated @RequestBody
			DTORoleUpdate role, Authentication auth) {

		if (!uService.isAdmin(auth)) {
			logger.error("{} is not authorized to update roles",
					auth.getName());
			return new ResponseEntity<>(
					"Current user is not authorized to update roles",
					HttpStatus.UNAUTHORIZED);
		}

		User user = new User();

		try {
			user = uService.getUserByID(id);
			uService.addRole(user, role.getRole());

		} catch (UserNotFoundException | RoleNotFoundException | FailedRoleUpdateException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(user, HttpStatus.OK);
	}
	
	/**
	 * Removes a role from a specific user.
	 * <p>
	 * Only an admin can perform this operation.
	 * </p>
	 *
	 * @param id    the ID of the user to modify
	 * @param role  the DTO containing the role to remove
	 * @param auth  the authentication object representing the current user
	 * @return a {@link ResponseEntity} with the updated user and HTTP status {@code 200 OK},
	 *         or {@code 400 Bad Request} if the user or role is invalid,
	 *         or {@code 401 Unauthorized} if the caller is not authorized
	 */
	@PatchMapping("/roles/remove/{id}")
	public ResponseEntity<?> removeRole(@PathVariable("id") long id,@Validated @RequestBody DTORoleUpdate role, Authentication auth){
		
		logger.info("{} is attempting to remove role'{}' from user with id '{}'", auth.getName(),role.getRole(),id);
		
		if (!uService.isAdmin(auth)) {
			logger.error("{} is not authorized to update roles",
					auth.getName());
			return new ResponseEntity<>(
					"Current user is not authorized to update roles",
					HttpStatus.UNAUTHORIZED);
		}

		User user = new User();

		try {
			user = uService.getUserByID(id);
			uService.removeRole(user, role.getRole());

		} catch (UserNotFoundException | RoleNotFoundException | FailedRoleUpdateException   e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			
		}
					
		logger.info("{} successfully removed role {} from {}", auth.getName(), role.getRole(), user.getUsername());
		return new ResponseEntity<>(user, HttpStatus.OK);
	}
	
	//DELETE
	
	/**
	 * Deletes a user by their ID.
	 * <p>
	 * Only an admin can access this endpoint.
	 * </p>
	 *
	 * @param id   the ID of the user to delete
	 * @param auth the authentication object representing the current user
	 * @return a {@link ResponseEntity} with a confirmation message and HTTP status {@code 200 OK},
	 *         or {@code 401 Unauthorized} if the caller is not authorized,
	 *         or {@code 400 Bad Request} if the user does not exist
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable("id") long id,
			Authentication auth) {

		logger.info("{} is attempting to delete user with ID '{}'",
				auth.getName(), id);

		if (!uService.isAdmin(auth)) {
			logger.error("{} is not authorized to delete users",
					auth.getName());
			return new ResponseEntity<>(
					"Current user is not authorized to delete users",
					HttpStatus.UNAUTHORIZED);
		}

		try {
			uService.deleteById(id);
			logger.info("{} successfully deleted user with ID '{}'",
					auth.getName(), id);
			return new ResponseEntity<>("User with ID " + id
					+ " has been deleted by " + auth.getName(), HttpStatus.OK);
		} catch (UserNotFoundException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}
}
