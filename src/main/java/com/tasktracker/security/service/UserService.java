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
import com.tasktracker.exception.InvalidPasswordException;
import com.tasktracker.exception.UserAlreadyExistsException;
import com.tasktracker.exception.UserNotFoundException;
import com.tasktracker.repository.UserRepo;
import com.tasktracker.security.model.User;

@Service
public class UserService {

	private final UserRepo uRepo;
	private final RoleService rService;
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
	private static final Logger logger = LoggerFactory
			.getLogger(UserService.class);

	public UserService(UserRepo uRepo, RoleService rService) {
		this.uRepo = uRepo;
		this.rService = rService;
	}

	public long getIdByUsername(String username) {

		User user = this.getByUsername(username);

		return user.getId();

	}

	public User getByUsername(String username) {
		Optional<User> user = uRepo.findByUsername(username);
		if (!user.isPresent()) {
			throw new UsernameNotFoundException(
					"Username" + username + " not found");
		}

		return user.get();

	}

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

	public List<User> getAllUsers() {
		return uRepo.findAll();
	}

	public boolean isAdmin(Authentication auth) {
		
		logger.info("Verifing {} authorities",auth.getName());

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

	public User getUserByID(long id) throws UserNotFoundException {

		Optional<User> user = uRepo.findById(id);

		if (!user.isPresent()) {
			throw new UserNotFoundException(
					"User with ID " + id + " cannot be found");
		}

		return user.get();
	}

	public User updateUser(DTOUserUpdate dto, Authentication auth) {

		User currentUser = this.getByUsername(auth.getName());
		String username = currentUser.getUsername();

		if (!encoder.matches(dto.getCurrentPassword(),
				currentUser.getPassword())) {

			logger.warn("User '{}' provided incorrect current password",username);
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
	
	public void deleteById(long id) {
			User user = this.getUserByID(id);
			uRepo.delete(user);

	}
}