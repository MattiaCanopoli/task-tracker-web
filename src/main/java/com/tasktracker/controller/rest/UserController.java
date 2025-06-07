package com.tasktracker.controller.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tasktracker.dto.DTOUser;
import com.tasktracker.exception.InvalidPasswordLengthException;
import com.tasktracker.exception.UserAlreadyExistsException;
import com.tasktracker.security.model.User;
import com.tasktracker.security.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

	private final UserService uService;

	public UserController(UserService uService) {
		this.uService = uService;
	}

	// READ

	@GetMapping
	public ResponseEntity<?> getUsers() {

		List<User> users = uService.getAllUsers();

		if (users.isEmpty()) {
			return new ResponseEntity<String>("No users found",
					HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<List<User>>(users, HttpStatus.OK);

	}

	// SHOW

	@GetMapping("/{id}")
	public ResponseEntity<?> detail(@PathVariable("id") long id,
			Authentication auth) {

		long userId = uService.getIdByUsername(auth.getName());

		if (uService.isAdmin(auth) || userId == id) {

			User user = uService.getUserByID(id);

			return new ResponseEntity<User>(user, HttpStatus.OK);

		}

		return new ResponseEntity<>("not auth", HttpStatus.UNAUTHORIZED);

	}

	@PostMapping
	public ResponseEntity<?> createUser(@Validated @RequestBody DTOUser user) {

		try {

			User newUser = uService.saveUser(user);
			return new ResponseEntity<User>(newUser, HttpStatus.CREATED);

		} catch (UserAlreadyExistsException e) {
			return new ResponseEntity<String>(e.getMessage(),
					HttpStatus.CONFLICT);

		} catch (InvalidPasswordLengthException e) {
			return new ResponseEntity<String>(e.getMessage(),
					HttpStatus.BAD_REQUEST);
		}

	}
}
